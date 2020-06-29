package xevenition.com.runage.model

import com.google.gson.annotations.SerializedName

data class FirestoreLocation(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double
)