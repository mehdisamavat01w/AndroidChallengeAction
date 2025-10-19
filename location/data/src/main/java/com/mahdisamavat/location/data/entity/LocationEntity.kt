package com.mahdisamavat.location.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mahdisamavat.core.model.Location


/**
 * Room entity for GPS location persistence.
 * 
 * Encrypted via SQLCipher with AES-256-GCM using KeyStore key.
 * Maps between data layer and domain model.
 * 
 * @property id Auto-generated primary key
 * @property latitude Decimal degrees
 * @property longitude Decimal degrees
 * @property accuracy Meters of precision
 * @property timestamp Unix milliseconds for sorting
 */
@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    
    @ColumnInfo(name = "latitude")
    val latitude: Double,
    
    @ColumnInfo(name = "longitude")
    val longitude: Double,
    
    @ColumnInfo(name = "accuracy")
    val accuracy: Float,
    
    @ColumnInfo(name = "altitude")
    val altitude: Double? = null,
    
    @ColumnInfo(name = "bearing")
    val bearing: Float? = null,
    
    @ColumnInfo(name = "speed")
    val speed: Float? = null,
    
    @ColumnInfo(name = "provider")
    val provider: String? = null,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long
) {

    /**
     * Converts entity to domain model for business logic layer.
     * @return Location domain model
     */
    fun toDomainModel(): Location {
        return Location(
            id = id,
            latitude = latitude,
            longitude = longitude,
            accuracy = accuracy,
            altitude = altitude,
            bearing = bearing,
            speed = speed,
            provider = provider,
            timestamp = timestamp
        )
    }
    
    companion object {

        /**
         * Creates entity from domain model for persistence.
         * @param location Domain model from presentation/domain layer
         * @return Entity ready for Room insertion
         */
        fun fromDomainModel(location: Location): LocationEntity {
            return LocationEntity(
                id = location.id,
                latitude = location.latitude,
                longitude = location.longitude,
                accuracy = location.accuracy,
                altitude = location.altitude,
                bearing = location.bearing,
                speed = location.speed,
                provider = location.provider,
                timestamp = location.timestamp
            )
        }
        

        /**
         * Creates entity from Android framework location.
         * Extracts GPS data from FusedLocationProviderClient result.
         * 
         * @param androidLocation Framework location with GPS coordinates
         * @return Entity ready for encrypted storage
         */
        fun fromAndroidLocation(androidLocation: android.location.Location): LocationEntity {
            return LocationEntity(
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
