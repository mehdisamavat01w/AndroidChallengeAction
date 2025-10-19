package com.mahdisamavat.location.domain.repository

import com.mahdisamavat.core.common.result.Result
import com.mahdisamavat.core.model.Location
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for location data operations.
 * 
 * Defines persistence operations following Clean Architecture.
 * Implemented by data layer, used by domain use cases.
 * All operations return Result for error handling.
 */
interface LocationRepository {

    /**
     * Observes all locations reactively.
     * @return Flow emitting location list on database changes
     */
    fun getAllLocationsFlow(): Flow<Result<List<Location>>>
    

    /**
     * Fetches all locations as snapshot.
     * @return Result with location list or error
     */
    suspend fun getAllLocations(): Result<List<Location>>
    

    /**
     * Retrieves most recent location.
     * @return Result with location or null if none exist
     */
    suspend fun getLatestLocation(): Result<Location?>

    /**
     * Persists location with encryption.
     * @param location Domain model to store
     * @return Result with generated ID or error
     */
    suspend fun storeLocation(location: Location): Result<Long>
    

    suspend fun storeAndroidLocation(androidLocation: android.location.Location): Result<Long>
    

    suspend fun getLocationCount(): Result<Int>
    

    fun getLocationCountFlow(): Flow<Result<Int>>
    

    suspend fun deleteAllLocations(): Result<Unit>
    

    suspend fun deleteLocationsBefore(beforeTimestamp: Long): Result<Int>
    

    suspend fun getRecentLocations(lastMinutes: Int): Result<List<Location>>
}
