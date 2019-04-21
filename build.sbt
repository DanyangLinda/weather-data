name := "weather-data"

version := "0.1"

scalaVersion := "2.11.12"

libraryDependencies += "junit" % "junit" % "4.10" % Test
libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.6" % Test
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.3.2",
  "org.apache.spark" %% "spark-sql" % "2.3.2"
)