package com.mahdisamavat.internet.data.repository

import android.content.Context
import com.mahdisamavat.core.common.error.AppError
import com.mahdisamavat.core.common.result.Result
import com.mahdisamavat.core.ipc.contract.IPCContract
import com.mahdisamavat.core.logger.Logger
import com.mahdisamavat.core.model.Location
import com.mahdisamavat.domain.repository.LocationQueryRepository


class LocationQueryRepositoryImpl(
    private val context: Context,
    private val logger: Logger
) : LocationQueryRepository {

    companion object {
        private const val TAG = "LocationQueryRepository"
    }

    override suspend fun getAllLocations(): Result<List<Location>> {
        return try {
            logger.i(TAG, "Querying all locations from Location App")

            val cursor = context.contentResolver.query(
                IPCContract.Provider.LOCATIONS_URI,
                null,
                null,
                null,
                null
            )

            if (cursor == null) {
                logger.e(TAG, "Cursor is null - Location App may not be installed or accessible")
                return Result.failure(
                    AppError.IPC(
                        "Failed to query locations - app not accessible",
                        null,
                        false,
                        "LocationQueryRepository"
                    )
                )
            }

            val locations = mutableListOf<Location>()

            cursor.use {
                val idIndex = it.getColumnIndex(IPCContract.Provider.Columns.ID)
                val latIndex = it.getColumnIndex(IPCContract.Provider.Columns.LATITUDE)
                val lngIndex = it.getColumnIndex(IPCContract.Provider.Columns.LONGITUDE)
                val accIndex = it.getColumnIndex(IPCContract.Provider.Columns.ACCURACY)
                val altIndex = it.getColumnIndex(IPCContract.Provider.Columns.ALTITUDE)
                val bearIndex = it.getColumnIndex(IPCContract.Provider.Columns.BEARING)
                val speedIndex = it.getColumnIndex(IPCContract.Provider.Columns.SPEED)
                val timeIndex = it.getColumnIndex(IPCContract.Provider.Columns.TIMESTAMP)
                val provIndex = it.getColumnIndex(IPCContract.Provider.Columns.PROVIDER)

                logger.d(TAG, "Cursor contains ${it.count} locations")

                while (it.moveToNext()) {
                    try {
                        val location = Location(
                            id = if (idIndex >= 0) it.getLong(idIndex) else 0,
                            latitude = if (latIndex >= 0) it.getDouble(latIndex) else 0.0,
                            longitude = if (lngIndex >= 0) it.getDouble(lngIndex) else 0.0,
                            accuracy = if (accIndex >= 0) it.getFloat(accIndex) else 0f,
                            altitude = if (altIndex >= 0 && !it.isNull(altIndex)) it.getDouble(
                                altIndex
                            ) else null,
                            bearing = if (bearIndex >= 0 && !it.isNull(bearIndex)) it.getFloat(
                                bearIndex
                            ) else null,
                            speed = if (speedIndex >= 0 && !it.isNull(speedIndex)) it.getFloat(
                                speedIndex
                            ) else null,
                            provider = if (provIndex >= 0) it.getString(provIndex) else null,
                            timestamp = if (timeIndex >= 0) it.getLong(timeIndex) else 0
                        )
                        locations.add(location)
                    } catch (e: Exception) {
                        logger.w(TAG, "Failed to parse location from cursor", e)
                    }
                }
            }

            logger.i(TAG, "Successfully retrieved ${locations.size} locations")
            Result.success(locations)

        } catch (e: SecurityException) {
            logger.e(TAG, "Permission denied - apps not signed with same key", e)
            Result.failure(
                AppError.Security(
                    "Permission denied: ${e.message}",
                    e,
                    false,
                    "LocationQueryRepository"
                )
            )
        } catch (e: Exception) {
            logger.e(TAG, "Failed to query all locations", e)
            Result.failure(
                AppError.IPC(
                    "Failed to query locations: ${e.message}",
                    e,
                    true,
                    "LocationQueryRepository"
                )
            )
        }
    }

    override suspend fun getLatestLocation(): Result<Location?> {
        return try {
            logger.i(TAG, "Querying latest location from Location App")

            val cursor = context.contentResolver.query(
                IPCContract.Provider.LATEST_LOCATION_URI,
                null,
                null,
                null,
                null
            )

            if (cursor == null) {
                logger.e(TAG, "Cursor is null - Location App may not be installed or accessible")
                return Result.failure(
                    AppError.IPC(
                        "Failed to query location - app not accessible",
                        null,
                        false,
                        "LocationQueryRepository"
                    )
                )
            }

            var location: Location? = null

            cursor.use {
                if (it.moveToFirst()) {
                    val idIndex = it.getColumnIndex(IPCContract.Provider.Columns.ID)
                    val latIndex = it.getColumnIndex(IPCContract.Provider.Columns.LATITUDE)
                    val lngIndex = it.getColumnIndex(IPCContract.Provider.Columns.LONGITUDE)
                    val accIndex = it.getColumnIndex(IPCContract.Provider.Columns.ACCURACY)
                    val altIndex = it.getColumnIndex(IPCContract.Provider.Columns.ALTITUDE)
                    val bearIndex = it.getColumnIndex(IPCContract.Provider.Columns.BEARING)
                    val speedIndex = it.getColumnIndex(IPCContract.Provider.Columns.SPEED)
                    val timeIndex = it.getColumnIndex(IPCContract.Provider.Columns.TIMESTAMP)
                    val provIndex = it.getColumnIndex(IPCContract.Provider.Columns.PROVIDER)

                    location = Location(
                        id = if (idIndex >= 0) it.getLong(idIndex) else 0,
                        latitude = if (latIndex >= 0) it.getDouble(latIndex) else 0.0,
                        longitude = if (lngIndex >= 0) it.getDouble(lngIndex) else 0.0,
                        accuracy = if (accIndex >= 0) it.getFloat(accIndex) else 0f,
                        altitude = if (altIndex >= 0 && !it.isNull(altIndex)) it.getDouble(altIndex) else null,
                        bearing = if (bearIndex >= 0 && !it.isNull(bearIndex)) it.getFloat(bearIndex) else null,
                        speed = if (speedIndex >= 0 && !it.isNull(speedIndex)) it.getFloat(
                            speedIndex
                        ) else null,
                        provider = if (provIndex >= 0) it.getString(provIndex) else null,
                        timestamp = if (timeIndex >= 0) it.getLong(timeIndex) else 0
                    )

                    logger.i(TAG, "Successfully retrieved latest location: id=${location?.id}")
                } else {
                    logger.i(TAG, "No locations found")
                }
            }

            Result.success(location)

        } catch (e: SecurityException) {
            logger.e(TAG, "Permission denied - apps not signed with same key", e)
            Result.failure(
                AppError.Security(
                    "Permission denied: ${e.message}",
                    e,
                    false,
                    "LocationQueryRepository"
                )
            )
        } catch (e: Exception) {
            logger.e(TAG, "Failed to query latest location", e)
            Result.failure(
                AppError.IPC(
                    "Failed to query latest location: ${e.message}",
                    e,
                    true,
                    "LocationQueryRepository"
                )
            )
        }
    }
}
