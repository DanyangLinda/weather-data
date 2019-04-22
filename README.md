# weather-data

## Environment
1. scala 2.11.12
2. sbt 1.2.8
3. spark 2.3.2 (spark is embeded in the code so you don't need to install it)

## Getting started
1. Download the project. Take a look at the pre-generated data as exmaple "weather-data/data-example/part-00000"
```
git clone git clone https://github.com/DanyangLinda/weather-data.git
```
2. Run the code. The output data is in "weather-data/data-<timestamp>/part-00000".
```
cd weather-data
sbt run
```
3. Spark is configured to run in local mode with 1 core and 500 MB memory. It will take about 10 mininutes to finish for your first time of running the code, as it will take a few mininutes to convert the earth image to a csv file (earthImage.csv) of 3.2 GB.

## Solution design

  
