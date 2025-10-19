package com.mahdisamavat.core.model

import kotlinx.serialization.Serializable

/**
 * Domain model for GPS location data.
 * 
 * Immutable value object used across all architecture layers.
 * Serializable for IPC and persistence. Includes accuracy
 * classification and formatting utilities.
 * 
 * @property id Unique identifier (0 if not persisted)
 * @property latitude Decimal degrees
 * @property longitude Decimal degrees
 * @property accuracy Horizontal accuracy in meters
 * @property altitude Meters above sea level (optional)
 * @property bearing Direction of travel in degrees (optional)
 * @property speed Speed in meters/second (optional)
 * @property provider GPS, Network, or Fused
 * @property timestamp Unix milliseconds when captured
 */
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

        /**
         * Creates domain model from Android framework Location.
         * Extracts all available GPS data.
         * 
         * @param androidLocation Framework location from FusedLocationProvider
         * @return Domain Location model
         */
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
