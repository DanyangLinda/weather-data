package weatherData

import java.time.{Instant, LocalDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter

import scala.concurrent.duration.{DAYS, Duration}
import scala.util.Random

object weatherData {
  val snow = "Snow"
  val sunny = "Sunny"
  val rain = "Rain"
  val conditionsBelowZero = Array(snow, sunny)
  val conditionsAboveZero = Array(rain, sunny)

  val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

  //constants in Barometric formula which is used to calculate pressure. Here we use see level as calculation base.
  //the data come from https://en.wikipedia.org/wiki/Barometric_formula
  val MOLAR_MASS : Double = 0.0289644
  val GRAVITY_ACC: Double = 9.80665
  val TEMPERATURE_LAPSE: Double = -0.0065
  val STANDARD_TEMPERATURE: Double = 288.15
  val STATIC_PRESSURE = 101325.0
  val UNIVERSAL_GAS_CONSTANT: Double= 8.3144598
  val EXPONENT: Double = GRAVITY_ACC*MOLAR_MASS/(UNIVERSAL_GAS_CONSTANT*TEMPERATURE_LAPSE)


  def generateWeatherData(location: String, position: Position): String = {
    val localDateTime = generateLocalDateTime(position.longitude)
    val temperature = generateTemperature(position.latitude, position.elevation, localDateTime)
    val condition = generateCondition(temperature)
    val humidity = generateHumidity(condition)
    val pressure = generatePressure(position.elevation)

    val temperatureSign = if(temperature>0) "+" else ""
    val temperatureStr = temperatureSign + f"$temperature%1.1f"
    val pressureStr = f"$pressure%1.1f"

    List(location, position.toString, localDateTime.format(dateTimeFormatter),
      condition, temperatureStr, pressureStr, humidity).mkString("|")
  }

  //generate random local date time within the last 10 years based on longitude
  def generateLocalDateTime(longitude: Double, milliSeconds: Long = Duration(365*10, DAYS).toMillis): LocalDateTime = {
    val instant = Instant.ofEpochMilli(System.currentTimeMillis() - (milliSeconds*Random.nextDouble()).toLong)

    val offSet = Math.round(Math.abs(longitude)/15.0).toInt.toString

    val direction = if(longitude > 0) "+" else "-"

    val offSetId = direction+offSet

    instant.atOffset(ZoneOffset.of(offSetId)).toLocalDateTime
  }


  def generateTemperature(latitude: Double, elevation: Int, localDateTime: LocalDateTime): Double = {
    val baseTemp = Math.cos(2 * latitude) * 30
    val elevationTemp = - elevation * 0.0006

    val month = localDateTime.getMonthValue
    val direction = if(latitude >= 0) 1 else -1
    val seasonTemp = direction * Math.sin(30 * month - 120) * 10

    val hourOfDay = localDateTime.getHour
    val dayNightTemp = Math.cos(15 * hourOfDay - 180) * 5

    baseTemp+seasonTemp+elevationTemp+dayNightTemp
  }

  def generateCondition(temperature: Double): String = {
    val index = Random.nextInt(1)

    if(temperature < 0 ) {
      conditionsBelowZero(index)
    } else {
      conditionsAboveZero(index)
    }
  }

  def generatePressure(elevation: Int): Double = {
    STATIC_PRESSURE * Math.pow(STANDARD_TEMPERATURE/(STANDARD_TEMPERATURE + TEMPERATURE_LAPSE*elevation), EXPONENT) / 100
  }

  def generateHumidity(condition: String): Int = {
    if(condition.equals(snow)) {
      Math.round(Random.nextDouble()*40).toInt
    } else if(condition.equals(rain)) {
      Math.round(30+Random.nextDouble()*70).toInt
    } else {
      Math.round(Random.nextDouble()*100).toInt
    }
  }
}
