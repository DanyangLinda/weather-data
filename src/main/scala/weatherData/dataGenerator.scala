package weatherData

import java.io.{BufferedWriter, FileWriter}
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime

import org.apache.spark.SparkConf
import javax.imageio.ImageIO
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.{Row, SparkSession}

import scala.collection.mutable.ListBuffer
import scala.reflect.io.File
import scala.util.Try

case class Position(latitude: Double, longitude:Double, elevation:Int) {
  override def toString: String =latitude.toString + "," + longitude.toString + ","+ elevation.toString
}

object dataGenerator {
  @transient lazy val config: SparkConf = new SparkConf().setMaster("local").setAppName("CBA Weather Data Challenge")
  @transient lazy val spark: SparkSession = SparkSession.builder().config(config).getOrCreate()

  val earthImageCsvFile: String = "input/earthImage.csv"
  val worldCityFile: String = "input/worldcities.csv"

  val imageCentralX: Int = 21600/2
  val imageCentralY: Int = 10800/2
  val lngScale: Double = 21600/360
  val latScale: Double = 10800/180
  val altScale: Double = 8848/255

  type Lat = Double
  type Lng = Double
  type Location = String
  type PixelCoordinate = (Int, Int)
  type Pixel = (Int, Int, Int)
  type City = (Location, Lat, Lng)

  def main(args: Array[String]): Unit = {
    //check if earth image csv file exists
    if(!File(earthImageCsvFile).exists) {
      if(!args.isEmpty) {
        convertEarthImageToCsv(args(0))
      } else {
        println("Earth image csv file doesn't exist. " +
          "Please specify the path of the original earth image to generate csv file.")
        System.exit(1)
      }
    }

    val dataFolder = "data-"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYYMMdd_HHmmss"))

    val worldCities = spark.read.format("csv").option("header", value = true).load(worldCityFile).rdd
      .mapPartitions[(PixelCoordinate, City)](toPixelCity)
      .collectAsMap()
      .toMap

    //Note: worldCities dataset is much smaller than earthImage dataset, so here broadcast small RDD to avoid shuffle.
    val broadcastCities: Broadcast[Map[PixelCoordinate, City]] = spark.sparkContext.broadcast(worldCities)

    val geographyRdd = spark.read.csv(earthImageCsvFile).rdd
      .mapPartitions[(Location, Position)](toGeography(_, broadcastCities))

    geographyRdd.map(v => weatherData.generateWeatherData(v._1, v._2))
      .coalesce(1)
      .saveAsTextFile(dataFolder)

    spark.stop()

  }

  def toPixelCity(partition:Iterator[Row]): Iterator[(PixelCoordinate, City)] = {
    var pixelCityList = new ListBuffer[(PixelCoordinate, City)]
    for(row <- partition) {
      Try{
        val lng = row.getAs[String]("lng").toDouble
        val lat = row.getAs[String]("lat").toDouble
        val x = (lng * lngScale + imageCentralX).toInt
        val y = (-lat * latScale + imageCentralY).toInt
        pixelCityList += (((x,y),(row.getAs[String]("city_ascii"), lat, lng)))
      }
    }
    pixelCityList.iterator
  }

  def toGeography(partition: Iterator[Row], broadcastCities: Broadcast[Map[PixelCoordinate, City]]):
  Iterator[(Location, Position)] = {
    var geographyData = new ListBuffer[(Location, Position)]()
    for(row <- partition) {
      Try {
        val coordinate = (row.getString(0).toInt, row.getString(1).toInt)
        val v = broadcastCities.value.get(coordinate)
        if (v.isDefined) {
          val l = v.get
          geographyData += ((l._1, Position(l._2, l._3, Math.round(row.getString(2).toInt*altScale).toInt)))
        }
      }
    }
    geographyData.iterator
  }

  def convertEarthImageToCsv(earthImagePath: String): Unit = {
    val earthImage = ImageIO.read(File(earthImagePath).jfile)
    val height = earthImage.getHeight
    val width = earthImage.getWidth
    val outputFile = new BufferedWriter(new FileWriter(earthImageCsvFile))
    for(x <- 0 until width) {
      for(y <- 0 until height) {
        val greyValue = earthImage.getRGB(x,y) & 0xFF
        outputFile.write(x+","+y+","+greyValue+"\n")
      }
    }
    outputFile.close()
  }
}

