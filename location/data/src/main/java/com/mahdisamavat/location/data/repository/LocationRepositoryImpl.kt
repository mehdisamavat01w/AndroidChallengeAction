package com.mahdisamavat.location.data.repository

import androidx.annotation.VisibleForTesting
import com.mahdisamavat.core.analytics.AnalyticsHelper
import com.mahdisamavat.core.analytics.PerformanceMonitor
import com.mahdisamavat.core.analytics.logError
import com.mahdisamavat.core.common.error.AppError
import com.mahdisamavat.core.common.result.Result
import com.mahdisamavat.location.domain.AppDispatchers
import com.mahdisamavat.location.domain.Dispatcher
import com.mahdisamavat.core.model.Location
import com.mahdisamavat.core.logger.Logger
import com.mahdisamavat.location.domain.repository.LocationRepository
import com.mahdisamavat.location.data.dao.LocationDao
import com.mahdisamavat.location.data.entity.LocationEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class LocationRepositoryImpl @Inject constructor(
    private val locationDao: LocationDao,
    private val logger: Logger,
    private val analyticsHelper: AnalyticsHelper,
    @Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : LocationRepository {
    
    private val performanceMonitor = PerformanceMonitor(analyticsHelper)
    
    companion object {
        private const val TAG = "LocationRepository"
    }
    
    override fun getAllLocationsFlow(): Flow<Result<List<Location>>> {
        logger.d(TAG, "Getting all locations as Flow")
        
        return locationDao.getAllLocationsFlow()
            .map { entities ->
                val locations = entities.map { it.toDomainModel() }
                logger.d(TAG, "Emitting ${locations.size} locations")
                Result.success(locations)
            }
            .catch { exception ->
                logger.e(TAG, "Error in getAllLocationsFlow", exception)
                emit(Result.failure(AppError.Data("Failed to get locations: ${exception.message}", exception, true, "LocationRepository")))
            }
            .flowOn(ioDispatcher)
    }
    
    override suspend fun getAllLocations(): Result<List<Location>> = withContext(ioDispatcher) {
        val startTime = System.currentTimeMillis()
        try {
            logger.d(TAG, "Getting all locations")
            
            val entities = locationDao.getAllLocations()
            val locations = entities.map { it.toDomainModel() }
            val duration = System.currentTimeMillis() - startTime
            
            performanceMonitor.checkDatabasePerformance("getAllLocations", duration, locations.size)
            logger.i(TAG, "Retrieved ${locations.size} locations from database in ${duration}ms")
            Result.success(locations)
        } catch (e: Exception) {
            logger.e(TAG, "Failed to get all locations", e)
            analyticsHelper.logError(e)
            Result.failure(AppError.Data("Failed to get locations: ${e.message}", e, true, "LocationRepository"))
        }
    }
    
    override suspend fun getLatestLocation(): Result<Location?> = withContext(ioDispatcher) {
        val startTime = System.currentTimeMillis()
        try {
            logger.d(TAG, "Getting latest location")
            
            val entity = locationDao.getLatestLocation()
            val location = entity?.toDomainModel()
            val duration = System.currentTimeMillis() - startTime
            
            performanceMonitor.checkDatabasePerformance("getLatestLocation", duration, if (location != null) 1 else 0)
            
            if (location != null) {
                logger.i(TAG, "Retrieved latest location: id=${location.id}, timestamp=${location.timestamp} in ${duration}ms")
            } else {
                logger.d(TAG, "No locations found in database")
            }
            
            Result.success(location)
        } catch (e: Exception) {
            logger.e(TAG, "Failed to get latest location", e)
            analyticsHelper.logError(e)
            Result.failure(AppError.Data("Failed to get latest location: ${e.message}", e, true, "LocationRepository"))
        }
    }
    
    override suspend fun storeLocation(location: Location): Result<Long> = withContext(ioDispatcher) {
        val startTime = System.currentTimeMillis()
        try {
            logger.d(TAG, "Storing location: lat=${location.latitude}, lng=${location.longitude}")
            
            val entity = LocationEntity.fromDomainModel(location)
            val id = locationDao.insertLocation(entity)
            val duration = System.currentTimeMillis() - startTime
            
            performanceMonitor.checkDatabasePerformance("storeLocation", duration, 1)
            performanceMonitor.checkEncryptionPerformance("store", duration)
            logger.i(TAG, "Location stored successfully with id=$id in ${duration}ms")
            Result.success(id)
        } catch (e: Exception) {
            logger.e(TAG, "Failed to store location", e)
            analyticsHelper.logError(e)
            Result.failure(AppError.Data("Failed to store location: ${e.message}", e, true, "LocationRepository"))
        }
    }
    
    override suspend fun storeAndroidLocation(androidLocation: android.location.Location): Result<Long> = withContext(ioDispatcher) {
        val startTime = System.currentTimeMillis()
        try {
            logger.d(TAG, "Storing Android location: lat=${androidLocation.latitude}, lng=${androidLocation.longitude}")
            
            val entity = LocationEntity.fromAndroidLocation(androidLocation)
            val id = locationDao.insertLocation(entity)
            val duration = System.currentTimeMillis() - startTime
            
            performanceMonitor.checkDatabasePerformance("storeAndroidLocation", duration, 1)
            performanceMonitor.checkEncryptionPerformance("store", duration)
            logger.i(TAG, "Android location stored successfully with id=$id in ${duration}ms")
            Result.success(id)
        } catch (e: Exception) {
            logger.e(TAG, "Failed to store Android location", e)
            analyticsHelper.logError(e)
            Result.failure(AppError.Data("Failed to store location: ${e.message}", e, true, "LocationRepository"))
        }
    }
    
    override suspend fun getLocationCount(): Result<Int> = withContext(ioDispatcher) {
        try {
            val count = locationDao.getLocationCount()
            logger.d(TAG, "Location count: $count")
            Result.success(count)
        } catch (e: Exception) {
            logger.e(TAG, "Failed to get location count", e)
            analyticsHelper.logError(e)
            Result.failure(AppError.Data("Failed to get count: ${e.message}", e, true, "LocationRepository"))
        }
    }
    
    override fun getLocationCountFlow(): Flow<Result<Int>> {
        logger.d(TAG, "Getting location count as Flow")
        
        return locationDao.getLocationCountFlow()
            .map { count ->
                logger.d(TAG, "Emitting location count: $count")
                Result.success(count)
            }
            .catch { exception ->
                logger.e(TAG, "Error in getLocationCountFlow", exception)
                emit(Result.failure(AppError.Data("Failed to get count: ${exception.message}", exception, true, "LocationRepository")))
            }
            .flowOn(ioDispatcher)
    }
    
    @VisibleForTesting
    override suspend fun deleteAllLocations(): Result<Unit> = withContext(ioDispatcher) {
        try {
            logger.i(TAG, "Deleting all locations")
            
            locationDao.deleteAllLocations()
            
            logger.i(TAG, "All locations deleted successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.e(TAG, "Failed to delete all locations", e)
            analyticsHelper.logError(e)
            Result.failure(AppError.Data("Failed to delete locations: ${e.message}", e, true, "LocationRepository"))
        }
    }
    
    override suspend fun deleteLocationsBefore(beforeTimestamp: Long): Result<Int> = withContext(ioDispatcher) {
        try {
            logger.i(TAG, "Deleting locations before timestamp: $beforeTimestamp")
            
            val deletedCount = locationDao.deleteLocationsBefore(beforeTimestamp)
            
            logger.i(TAG, "Deleted $deletedCount old locations")
            Result.success(deletedCount)
        } catch (e: Exception) {
            logger.e(TAG, "Failed to delete old locations", e)
            analyticsHelper.logError(e)
            Result.failure(AppError.Data("Failed to delete old locations: ${e.message}", e, true, "LocationRepository"))
        }
    }
    
    override suspend fun getRecentLocations(lastMinutes: Int): Result<List<Location>> = withContext(ioDispatcher) {
        try {
            logger.d(TAG, "Getting locations from last $lastMinutes minutes")
            
            val sinceTimestamp = System.currentTimeMillis() - (lastMinutes * 60_000L)
            val entities = locationDao.getLocationsSince(sinceTimestamp)
            val locations = entities.map { it.toDomainModel() }
            
            logger.i(TAG, "Retrieved ${locations.size} locations from last $lastMinutes minutes")
            Result.success(locations)
        } catch (e: Exception) {
            logger.e(TAG, "Failed to get recent locations", e)
            analyticsHelper.logError(e)
            Result.failure(AppError.Data("Failed to get recent locations: ${e.message}", e, true, "LocationRepository"))
        }
    }
}
