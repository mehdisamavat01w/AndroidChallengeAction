package com.mahdisamavat.location.domain.repository

import com.mahdisamavat.core.common.result.Result
import com.mahdisamavat.core.model.Location
import kotlinx.coroutines.flow.Flow

interface LocationRepository {

    fun getAllLocationsFlow(): Flow<Result<List<Location>>>
    

    suspend fun getAllLocations(): Result<List<Location>>
    

    suspend fun getLatestLocation(): Result<Location?>

    suspend fun storeLocation(location: Location): Result<Long>
    

    suspend fun storeAndroidLocation(androidLocation: android.location.Location): Result<Long>
    

    suspend fun getLocationCount(): Result<Int>
    

    fun getLocationCountFlow(): Flow<Result<Int>>
    

    suspend fun deleteAllLocations(): Result<Unit>
    

    suspend fun deleteLocationsBefore(beforeTimestamp: Long): Result<Int>
    

    suspend fun getRecentLocations(lastMinutes: Int): Result<List<Location>>
}
