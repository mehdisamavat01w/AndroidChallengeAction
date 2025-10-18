package com.mahdisamavat.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val altitude: Double? = null,
    val bearing: Float? = null,
    val speed: Float? = null,
    val provider: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) {

    fun hasAltitude(): Boolean = altitude != null

    fun hasBearing(): Boolean = bearing != null

    fun hasSpeed(): Boolean = speed != null

    fun getCoordinatesString(): String {
        return String.format("%.6f, %.6f", latitude, longitude)
    }

    fun getAccuracyString(): String {
        return String.format("±%.1fm", accuracy)
    }
    

    fun isHighAccuracy(): Boolean = accuracy < 20f

    fun isMediumAccuracy(): Boolean = accuracy in 20f..50f

    fun isLowAccuracy(): Boolean = accuracy > 50f

    fun getAccuracyLevel(): String = when {
        isHighAccuracy() -> "High"
        isMediumAccuracy() -> "Medium"
        else -> "Low"
    }
    
    companion object {

        fun from(androidLocation: android.location.Location): Location {
            return Location(
                latitude = androidLocation.latitude,
                longitude = androidLocation.longitude,
                accuracy = androidLocation.accuracy,
                altitude = if (androidLocation.hasAltitude()) androidLocation.altitude else null,
                bearing = if (androidLocation.hasBearing()) androidLocation.bearing else null,
                speed = if (androidLocation.hasSpeed()) androidLocation.speed else null,
                provider = androidLocation.provider,
                timestamp = androidLocation.time
            )
        }
    }
}
