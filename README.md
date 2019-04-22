# weather-data 
## Environment
1. Scala 2.11.12
2. Sbt 1.2.8
3. Spark 2.3.2 (spark is embeded lib, so you don't need to install it)

## Getting started
1. Download the project. Please take a look at the pre-generated data as exmaple
```
git clone git clone https://github.com/DanyangLinda/weather-data.git
```
2. Run the code. The output data is written in "weather-data/data-<timestamp>/part-00000".
```
cd weather-data
sbt run
```
3. Spark is configured to run in local mode with 1 core and 500 MB memory. It will take about 10 mininutes to finish for your first time to run the code, as it will take a few mininutes to convert the earth image to earthImage.csv of 3.2 GB.
  
## Task
The requirement is to genrate weather data from reasonable number of positions in the following format
```
Location|Position|Local Date Time|Conditions|Temperature|Pressure|Humidity
Sydney|-33.86,151.21,39|2015-12-23T05:02:12Z|Rain|+12.5|1004.3|97
Melbourne|-37.83,144.98,7|2015-12-24T15:30:55Z|Snow|-5.3|998.4|55
Adelaide|-34.92,138.62,48|2016-01-03T12:35:37Z|Sunny|+39.4|1114.1|12
```

## Solution design
As a candidate for Data Engineer - Big Data and Software Engineering, I would like to use this case to show my skill of using Spark and Scala to manipulate big dataset. 

### Construct geography dataset
The earth image at [Visible Earth](https://visibleearth.nasa.gov/view.php?id=73934) contains elevation information of all positions on Earth. To make the image data can be easily fed in to Spark, the code will convert it into earthImage.csv as follows
```
3,6386,13
3,6387,38
3,6388,22
3,6389,0
3,6390,22
3,6391,13
```

Each line consists of three numbers. The first and second numbers are the x and y coordinate of a pixel. The third number is the grey value of the pixel. As the resolution of the earth image is 21600 by 10800, the generated csv file contains 233,280,000 lines representing 21600*10800 pixels of the Earth image. 

However the earth image deson't have location lables. So I employ another dataset from [Simple Maps](https://simplemaps.com/data/world-cities). The dataset (weather-data/input/worldcities.csv) I used is the free version with about 13000 entries of city name, latitude and longitude as follows:
```
"Malisheve","42.4822","20.7458"
"Sydney","-33.9200","151.1852"
"Beijing","39.9289","116.3883"
```

Spark is used to combine the two datasets to contrct complete location and position dataset, where location, latitude and longitude comes from worldcities.csv and elevation comes from earthImage.csv (I roughly calculate elevation based on grey value of pixels with the formular `greyValue*(8848/255)` where maximum gray value is 255 and I assume the highest elevation is 8848 metres). 

Using [Equirectangular Projection](https://www.tandfonline.com/doi/pdf/10.1080/10095020.2015.1017913) to map longitude and latitude to x and y coordinates of the pixels, I can use Spark can join two datasets to construct the following geography dataset:
```
Location,Position(latitude, longitude, elevation)
```

Note: 
2. elevation = grayValue*(8848/255)
1. In the implemenation, I don't use "join" function to combine the two datasets directly cause that will result in shuffling more than 3 GB data. As world cities dataset (about 1 MB) is much smaller than earth image dataset (about 3 GB), I decided to broadcast the samll dataset to avoid shuffle. 

### Generate weather data
Weather data is generated based on geography data set from previouse process. Weather data consists of 5 parts: local date time, conditions (snow, sunny or rain), temperature, pressure and humidity. 

#### Local date time
Time offset can be roughly 

#### Temperature


#### Condition


#### Pressure


#### Humidity


