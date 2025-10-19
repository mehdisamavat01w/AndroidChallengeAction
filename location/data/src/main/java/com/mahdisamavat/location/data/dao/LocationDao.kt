package com.mahdisamavat.location.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mahdisamavat.location.data.entity.LocationEntity
import kotlinx.coroutines.flow.Flow


/**
 * Room DAO for encrypted location storage operations.
 * 
 * Provides suspend functions for single queries and Flow for
 * reactive UI updates. All data encrypted via SQLCipher AES-256.
 */
@Dao
interface LocationDao {

    /**
     * Observes all locations reactively, newest first.
     * Emits updates on database changes.
     * 
     * @return Flow emitting location list on each change
     */
    @Query("SELECT * FROM locations ORDER BY timestamp DESC")
    fun getAllLocationsFlow(): Flow<List<LocationEntity>>
    

    /**
     * Fetches all locations as one-time snapshot.
     * @return List ordered by timestamp descending
     */
    @Query("SELECT * FROM locations ORDER BY timestamp DESC")
    suspend fun getAllLocations(): List<LocationEntity>
    

    /**
     * Returns most recent location by timestamp.
     * @return Latest location or null if table empty
     */
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
    

    /**
     * Observes total location count reactively.
     * @return Flow emitting count on table changes
     */
    @Query("SELECT COUNT(*) FROM locations")
    fun getLocationCountFlow(): Flow<Int>
    

    /**
     * Inserts location, replacing on ID conflict.
     * @return Generated row ID
     */
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
    

    /**
     * Removes locations older than specified timestamp.
     * Useful for data retention policies.
     * 
     * @param beforeTimestamp Unix timestamp in milliseconds
     * @return Count of deleted rows
     */
    @Query("DELETE FROM locations WHERE timestamp < :beforeTimestamp")
    suspend fun deleteLocationsBefore(beforeTimestamp: Long): Int

    @Query("SELECT MIN(timestamp) FROM locations")
    suspend fun getOldestTimestamp(): Long?
    

    @Query("SELECT MAX(timestamp) FROM locations")
    suspend fun getNewestTimestamp(): Long?
}
