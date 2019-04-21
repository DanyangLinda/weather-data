package weatherData

import java.io.{BufferedWriter, FileWriter}

import javax.imageio.ImageIO

import scala.reflect.io.File

object imageData {
  val earthImageCsv: String = "input/earthImage.csv"
  val originalEarthImage: String = "input/gebco_08_rev_elev_21600x10800.png"

  def convertEarthImageToCsv(): Unit = {
    val earthImage = ImageIO.read(File(originalEarthImage).jfile)
    val height = earthImage.getHeight
    val width = earthImage.getWidth
    val outputFile = new BufferedWriter(new FileWriter(earthImageCsv))
    for(x <- 0 until width) {
      for(y <- 0 until height) {
        val greyValue = earthImage.getRGB(x,y) & 0xFF
        outputFile.write(x+","+y+","+greyValue+"\n")
      }
    }
    outputFile.flush()
    outputFile.close()
  }
}
