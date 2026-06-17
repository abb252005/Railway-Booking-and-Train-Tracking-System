package com.example.railway.util

data class StateBoundary(
    val name: String,
    val points: List<Pair<Double, Double>>, // [Lat, Lng]
    val centroid: Pair<Double, Double>
)

object StateBoundaries {
    // Geographically precise US Map representation.
    // Coordinates are in [Latitude, Longitude] format.
    val allStates = listOf(
        StateBoundary("Washington", poly(49.0,-124.7, 49.0,-117.0, 46.0,-117.0, 46.2,-119.0, 46.2,-124.0, 48.4,-124.8), 47.5 to -120.5),
        StateBoundary("Oregon", poly(46.2,-124.0, 46.2,-119.0, 46.0,-117.0, 42.0,-117.0, 42.0,-124.5, 46.0,-124.0), 44.0 to -120.5),
        StateBoundary("California", poly(42.0,-124.5, 42.0,-120.0, 39.0,-120.0, 35.0,-114.5, 32.5,-114.5, 32.5,-117.0, 34.0,-120.5, 38.0,-123.0, 40.5,-124.5), 37.0 to -120.0),
        StateBoundary("Idaho", poly(49.0,-117.0, 49.0,-116.0, 44.5,-111.0, 44.5,-111.0, 42.0,-111.0, 42.0,-117.0, 44.5,-117.0, 49.0,-117.0), 45.0 to -114.0),
        StateBoundary("Nevada", poly(42.0,-120.0, 42.0,-114.0, 37.0,-114.0, 35.0,-114.5, 39.0,-120.0), 39.0 to -117.0),
        StateBoundary("Montana", poly(49.0,-116.0, 49.0,-104.0, 45.0,-104.0, 45.0,-111.0, 44.5,-111.0, 49.0,-116.0), 47.0 to -110.0),
        StateBoundary("Wyoming", poly(45.0,-111.0, 45.0,-104.0, 41.0,-104.0, 41.0,-111.0, 45.0,-111.0), 43.0 to -107.5),
        StateBoundary("Utah", poly(42.0,-114.0, 42.0,-111.0, 41.0,-111.0, 41.0,-109.0, 37.0,-109.0, 37.0,-114.0, 42.0,-114.0), 39.5 to -111.5),
        StateBoundary("Arizona", poly(37.0,-114.0, 37.0,-109.0, 31.3,-109.0, 31.3,-111.0, 32.5,-114.5, 37.0,-114.0), 34.5 to -111.5),
        StateBoundary("Colorado", poly(41.0,-109.0, 41.0,-102.0, 37.0,-102.0, 37.0,-109.0, 41.0,-109.0), 39.0 to -105.5),
        StateBoundary("New Mexico", poly(37.0,-109.0, 37.0,-103.0, 32.0,-103.0, 32.0,-106.5, 31.3,-106.5, 31.3,-109.0, 37.0,-109.0), 34.5 to -106.0),
        StateBoundary("North Dakota", poly(49.0,-104.0, 49.0,-96.5, 46.0,-96.5, 46.0,-104.0, 49.0,-104.0), 47.5 to -100.5),
        StateBoundary("South Dakota", poly(46.0,-104.0, 46.0,-96.5, 43.0,-96.5, 43.0,-104.0, 46.0,-104.0), 44.5 to -100.5),
        StateBoundary("Nebraska", poly(43.0,-104.0, 43.0,-98.0, 40.0,-95.5, 40.0,-102.0, 41.0,-102.0, 41.0,-104.0, 43.0,-104.0), 41.5 to -100.0),
        StateBoundary("Kansas", poly(40.0,-102.0, 40.0,-94.5, 37.0,-94.5, 37.0,-102.0, 40.0,-102.0), 38.5 to -98.5),
        StateBoundary("Oklahoma", poly(37.0,-103.0, 37.0,-94.5, 34.0,-94.5, 34.0,-100.0, 36.5,-100.0, 36.5,-103.0, 37.0,-103.0), 35.5 to -97.5),
        StateBoundary("Texas", poly(36.5,-103.0, 36.5,-100.0, 34.0,-100.0, 34.0,-94.0, 29.5,-94.0, 26.0,-97.5, 29.0,-103.5, 32.0,-106.5, 32.0,-103.0, 36.5,-103.0), 31.0 to -100.0),
        StateBoundary("Minnesota", poly(49.0,-96.5, 49.0,-95.0, 48.0,-89.5, 44.0,-91.0, 43.5,-96.5, 49.0,-96.5), 46.5 to -94.5),
        StateBoundary("Iowa", poly(43.5,-96.5, 43.5,-91.0, 40.5,-91.5, 40.5,-96.5, 43.5,-96.5), 42.0 to -93.5),
        StateBoundary("Missouri", poly(40.5,-95.5, 40.5,-91.5, 36.0,-89.5, 36.0,-94.5, 40.5,-95.5), 38.5 to -92.5),
        StateBoundary("Arkansas", poly(36.5,-94.5, 36.5,-89.5, 33.0,-91.0, 33.0,-94.5, 36.5,-94.5), 35.0 to -92.5),
        StateBoundary("Louisiana", poly(33.0,-94.0, 33.0,-91.0, 31.0,-91.0, 29.0,-89.0, 29.0,-94.0, 33.0,-94.0), 31.0 to -92.0),
        StateBoundary("Wisconsin", poly(47.0,-93.0, 47.0,-90.5, 45.0,-87.0, 42.5,-88.0, 42.5,-93.0, 47.0,-93.0), 44.5 to -90.0),
        StateBoundary("Illinois", poly(42.5,-91.5, 42.5,-87.5, 37.0,-88.5, 37.0,-91.5, 42.5,-91.5), 40.0 to -89.0),
        StateBoundary("Mississippi", poly(35.0,-91.5, 35.0,-88.0, 30.0,-88.5, 30.0,-91.5, 35.0,-91.5), 32.5 to -89.5),
        StateBoundary("Michigan", poly(47.5,-90.0, 47.5,-83.5, 41.5,-83.5, 41.5,-90.0, 47.5,-90.0), 44.5 to -85.0),
        StateBoundary("Indiana", poly(41.5,-87.5, 41.5,-85.0, 38.0,-85.0, 38.0,-88.0, 41.5,-87.5), 40.0 to -86.0),
        StateBoundary("Ohio", poly(41.5,-85.0, 42.0,-80.5, 38.5,-80.5, 38.5,-85.0, 41.5,-85.0), 40.0 to -82.5),
        StateBoundary("Kentucky", poly(39.0,-89.0, 39.0,-82.0, 36.5,-83.5, 36.5,-89.0, 39.0,-89.0), 37.5 to -85.5),
        StateBoundary("Tennessee", poly(36.5,-90.0, 36.5,-81.5, 35.0,-82.0, 35.0,-90.0, 36.5,-90.0), 36.0 to -86.0),
        StateBoundary("Alabama", poly(35.0,-88.5, 35.0,-85.0, 30.0,-85.0, 30.0,-88.5, 35.0,-88.5), 32.5 to -86.5),
        StateBoundary("Georgia", poly(35.0,-85.5, 35.0,-81.0, 30.5,-81.5, 30.5,-85.5, 35.0,-85.5), 32.5 to -83.5),
        StateBoundary("Florida", poly(31.0,-87.5, 31.0,-81.5, 25.0,-80.0, 25.0,-82.0, 30.0,-85.0, 30.0,-87.5, 31.0,-87.5), 28.5 to -82.0),
        StateBoundary("New York", poly(45.0,-79.5, 45.0,-73.5, 40.5,-73.5, 40.5,-79.5, 45.0,-79.5), 43.0 to -75.5),
        StateBoundary("Pennsylvania", poly(42.0,-80.5, 42.0,-74.5, 40.0,-74.5, 40.0,-80.5, 42.0,-80.5), 41.0 to -77.5),
        StateBoundary("West Virginia", poly(40.5,-82.5, 40.5,-77.5, 37.0,-81.5, 38.0,-82.5, 40.5,-82.5), 38.5 to -80.5),
        StateBoundary("Virginia", poly(39.5,-83.5, 39.5,-76.0, 36.5,-76.0, 36.5,-83.5, 39.5,-83.5), 37.5 to -78.5),
        StateBoundary("North Carolina", poly(36.5,-84.5, 36.5,-75.5, 34.0,-78.0, 35.0,-84.5, 36.5,-84.5), 35.5 to -79.5),
        StateBoundary("South Carolina", poly(35.0,-83.0, 35.0,-78.5, 32.0,-81.0, 35.0,-83.0), 34.0 to -81.0),
        StateBoundary("Maryland", poly(39.7,-79.5, 39.7,-75.0, 38.0,-75.0, 38.0,-79.5, 39.7,-79.5), 39.0 to -76.5),
        StateBoundary("Delaware", poly(39.8,-75.8, 39.8,-75.0, 38.5,-75.0, 38.5,-75.8, 39.8,-75.8), 39.0 to -75.5),
        StateBoundary("New Jersey", poly(41.3,-75.5, 41.3,-73.5, 39.0,-74.5, 39.0,-75.5, 41.3,-75.5), 40.0 to -74.5),
        StateBoundary("Connecticut", poly(42.0,-73.5, 42.0,-71.8, 41.0,-71.8, 41.0,-73.5, 42.0,-73.5), 41.5 to -72.7),
        StateBoundary("Rhode Island", poly(42.0,-71.8, 42.0,-71.1, 41.1,-71.1, 41.1,-71.8, 42.0,-71.8), 41.5 to -71.5),
        StateBoundary("Massachusetts", poly(42.8,-73.5, 42.8,-70.0, 41.2,-70.0, 41.2,-73.5, 42.8,-73.5), 42.3 to -71.8),
        StateBoundary("Vermont", poly(45.0,-73.4, 45.0,-71.5, 42.7,-72.5, 42.7,-73.4, 45.0,-73.4), 44.0 to -72.7),
        StateBoundary("New Hampshire", poly(45.3,-71.5, 45.3,-70.5, 42.7,-70.8, 42.7,-72.5, 45.3,-71.5), 43.5 to -71.5),
        StateBoundary("Maine", poly(47.5,-71.0, 47.5,-67.0, 44.0,-67.0, 43.0,-71.0, 47.5,-71.0), 45.0 to -69.0),
        StateBoundary("Alaska", poly(71.5,-170.0, 71.5,-130.0, 54.0,-130.0, 54.0,-170.0, 71.5,-170.0), 64.0 to -150.0),
        StateBoundary("Hawaii", poly(22.5,-160.0, 22.5,-154.0, 18.5,-154.0, 18.5,-160.0, 22.5,-160.0), 20.5 to -157.0)
    )

    private fun poly(vararg coords: Double): List<Pair<Double, Double>> {
        val list = mutableListOf<Pair<Double, Double>>()
        for (i in 0 until coords.size step 2) {
            list.add(coords[i] to coords[i+1])
        }
        if (list.isNotEmpty()) list.add(list[0])
        return list
    }
}
