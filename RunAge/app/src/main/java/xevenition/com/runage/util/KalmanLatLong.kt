package xevenition.com.runage.util

class KalmanLatLong(private var Q_metres_per_second: Float) {
    private val minAccuracy = 1f
    private var timeStampMilliseconds: Long = 0
    private var lat = 0.0
    private var lng = 0.0
    private var variance // P matrix. Negative means object uninitialised.
            : Float

    // NB: units irrelevant, as long as same units used
    // throughout
    var consecutiveRejectCount: Int

    fun getLat(): Double {
        return lat
    }

    fun getLng(): Double {
        return lng
    }

    // / <summary>
    // / Kalman filter processing for latitude and longitude
    // / </summary>
    // / <param name="lat_measurement_degrees">new measurement of
    // latitude</param>
    // / <param name="lng_measurement">new measurement of longitude</param>
    // / <param name="accuracy">measurement of 1 standard deviation error in
    // metres</param>
    // / <param name="TimeStamp_milliseconds">time of measurement</param>
    // / <returns>new state</returns>
    fun process(
        lat_measurement: Double,
        lng_measurement: Double,
        acc: Float,
        TimeStamp_milliseconds: Long,
        Q_metres_per_second: Float
    ) {
        var accuracy = acc
        this.Q_metres_per_second = Q_metres_per_second
        if (accuracy < minAccuracy) accuracy = minAccuracy
        if (variance < 0) {
            // if variance < 0, object is uninitialised, so initialise with
            // current values
            this.timeStampMilliseconds = TimeStamp_milliseconds
            lat = lat_measurement
            lng = lng_measurement
            variance = accuracy * accuracy
        } else {
            // else apply Kalman filter methodology
            val timeIncMilliseconds = (TimeStamp_milliseconds
                    - this.timeStampMilliseconds)
            if (timeIncMilliseconds > 0) {
                // time has moved on, so the uncertainty in the current position
                // increases
                variance += (timeIncMilliseconds * Q_metres_per_second
                        * Q_metres_per_second) / 1000
                this.timeStampMilliseconds = TimeStamp_milliseconds
                // TO DO: USE VELOCITY INFORMATION HERE TO GET A BETTER ESTIMATE
                // OF CURRENT POSITION
            }

            // Kalman gain matrix K = Covarariance * Inverse(Covariance +
            // MeasurementVariance)
            // NB: because K is dimensionless, it doesn't matter that variance
            // has different units to lat and lng
            val k = variance / (variance + accuracy * accuracy)
            // apply K
            lat += k * (lat_measurement - lat)
            lng += k * (lng_measurement - lng)
            // new Covarariance matrix is (IdentityMatrix - K) * Covarariance
            variance *= (1 - k)
        }
    }

    init {
        variance = -1f
        consecutiveRejectCount = 0
    }
}