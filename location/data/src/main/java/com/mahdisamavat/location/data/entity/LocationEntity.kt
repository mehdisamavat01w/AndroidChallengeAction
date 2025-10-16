package com.mahdisamavat.location.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mahdisamavat.core.model.Location


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
