package com.mahdisamavat.location.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mahdisamavat.location.data.entity.LocationEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface LocationDao {

    @Query("SELECT * FROM locations ORDER BY timestamp DESC")
    fun getAllLocationsFlow(): Flow<List<LocationEntity>>
    

    @Query("SELECT * FROM locations ORDER BY timestamp DESC")
    suspend fun getAllLocations(): List<LocationEntity>
    

    @Query("SELECT * FROM locations ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestLocation(): LocationEntity?
    

    @Query("SELECT * FROM locations WHERE id = :locationId")
    suspend fun getLocationById(locationId: Long): LocationEntity?
    

    @Query("SELECT * FROM locations WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    suspend fun getLocationsBetween(startTime: Long, endTime: Long): List<LocationEntity>
    

    @Query("SELECT * FROM locations WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    suspend fun getLocationsSince(sinceTimestamp: Long): List<LocationEntity>
    

    @Query("SELECT COUNT(*) FROM locations")
    suspend fun getLocationCount(): Int
    

    @Query("SELECT COUNT(*) FROM locations")
    fun getLocationCountFlow(): Flow<Int>
    

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationEntity): Long
    

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locations: List<LocationEntity>): List<Long>
    

    @Update
    suspend fun updateLocation(location: LocationEntity)
    

    @Delete
    suspend fun deleteLocation(location: LocationEntity)
    

    @Query("DELETE FROM locations")
    suspend fun deleteAllLocations()
    

    @Query("DELETE FROM locations WHERE timestamp < :beforeTimestamp")
    suspend fun deleteLocationsBefore(beforeTimestamp: Long): Int

    @Query("SELECT MIN(timestamp) FROM locations")
    suspend fun getOldestTimestamp(): Long?
    

    @Query("SELECT MAX(timestamp) FROM locations")
    suspend fun getNewestTimestamp(): Long?
}
