package weatherData

import java.time._

import org.scalatest.FunSuite

class weatherDataTest extends FunSuite {

  test("generateHumidity should return value between 0 and 40 when condition is snow") {
    val humidity = weatherData.generateHumidity(weatherData.snow)
    assert(humidity >=0 && humidity <=40)
  }

  test("generateHumidity should return value between 30 and 100 when condition is rain") {
    val humidity = weatherData.generateHumidity(weatherData.rain)
    assert(humidity >=30 && humidity <=100)
  }

  test("generateHumidity should return value between 0 and 100 when condition is sunny") {
    val humidity = weatherData.generateHumidity(weatherData.sunny)
    assert(humidity >=0 && humidity <=100)
  }

  test("generatePressure should return static pressure for sea level") {
    val pressure = weatherData.generatePressure(0)
    assert(pressure == weatherData.STATIC_PRESSURE/100)
  }

  test("generateTemperature should return value between -50 and +50 Centigrade") {
    val localDateTime = LocalDateTime.now()
    val latitude: Double = -33.9200
    val elevation: Int = 100
    val temperature = weatherData.generateTemperature(latitude, elevation, localDateTime)
    assert(temperature<=55 && temperature >= -55)
  }

  test("generateCondition shouldn't emit Rain when temperature is below zero") {
    val temperature: Double = -23
    val condition = weatherData.generateCondition(temperature)
    assert(!condition.equals(weatherData.rain) && weatherData.conditionsBelowZero.contains(condition))
  }

  test("generateCondition shouldn't emit Snow when temperature is above zero") {
    val temperature: Double = 23
    val condition = weatherData.generateCondition(temperature)
    assert(weatherData.conditionsAboveZero.contains(condition))
    assert(!condition.equals(weatherData.snow))
  }

  test("generateLocalDateTime should generate a date time before current local date time for Sydney") {
    val sydneyLongitude: Double = 151.1852
    val returnedSydneyTime: LocalDateTime = weatherData.generateLocalDateTime(sydneyLongitude)
    val currentSydneyTime: LocalDateTime = Instant.ofEpochMilli(System.currentTimeMillis())
      .atOffset(ZoneOffset.of("+10")).toLocalDateTime
    assert(returnedSydneyTime.isBefore(currentSydneyTime))
  }

}
