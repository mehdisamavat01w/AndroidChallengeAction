package com.mahdisamavat.location.ipc.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import com.mahdisamavat.core.common.result.Result
import com.mahdisamavat.core.ipc.contract.IPCContract
import com.mahdisamavat.core.logger.Logger
import com.mahdisamavat.location.domain.repository.LocationRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking


class LocationContentProvider : ContentProvider() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface LocationContentProviderEntryPoint {
        fun locationRepository(): LocationRepository
        fun logger(): Logger
    }

    private var locationRepository: LocationRepository? = null
    private var logger: Logger? = null

    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(IPCContract.Provider.AUTHORITY, IPCContract.Provider.PATH_LOCATIONS, LOCATIONS)
        addURI(IPCContract.Provider.AUTHORITY, IPCContract.Provider.PATH_LATEST, LATEST)
    }

    companion object {
        private const val TAG = "LocationContentProvider"
        private const val LOCATIONS = 1
        private const val LATEST = 2
    }

    private fun ensureInitialized() {
        if (locationRepository == null || logger == null) {
            val context = context ?: throw IllegalStateException("Context is null")
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                LocationContentProviderEntryPoint::class.java
            )
            locationRepository = entryPoint.locationRepository()
            logger = entryPoint.logger()
            logger?.i(TAG, "LocationContentProvider lazy initialized")
        }
    }

    override fun onCreate(): Boolean {
        val context = context ?: return false

        try {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                LocationContentProviderEntryPoint::class.java
            )

            locationRepository = entryPoint.locationRepository()
            logger = entryPoint.logger()

            logger?.i(TAG, "LocationContentProvider created")
            return true
        } catch (e: Exception) {
            return true
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        ensureInitialized()
        logger?.i(TAG, "Query received: $uri")

        val callingPackage = callingPackage
        logger?.d(TAG, "Calling package: $callingPackage")

        return when (uriMatcher.match(uri)) {
            LOCATIONS -> queryAllLocations()
            LATEST -> queryLatestLocation()
            else -> {
                logger?.w(TAG, "Unknown URI: $uri")
                null
            }
        }
    }

    private fun queryAllLocations(): Cursor {
        logger?.i(TAG, "Querying all locations")

        return runBlocking {
            val result = locationRepository!!.getAllLocations()

            when (result) {
                is Result.Success -> {
                    val locations = result.data
                    logger?.i(TAG, "Retrieved ${locations.size} locations")

                    val cursor = MatrixCursor(
                        arrayOf(
                            IPCContract.Provider.Columns.ID,
                            IPCContract.Provider.Columns.LATITUDE,
                            IPCContract.Provider.Columns.LONGITUDE,
                            IPCContract.Provider.Columns.ACCURACY,
                            IPCContract.Provider.Columns.ALTITUDE,
                            IPCContract.Provider.Columns.BEARING,
                            IPCContract.Provider.Columns.SPEED,
                            IPCContract.Provider.Columns.TIMESTAMP,
                            IPCContract.Provider.Columns.PROVIDER,
                            IPCContract.Provider.Columns.IS_ENCRYPTED
                        )
                    )

                    locations.forEach { location ->
                        cursor.addRow(
                            arrayOf(
                                location.id,
                                location.latitude,
                                location.longitude,
                                location.accuracy,
                                location.altitude,
                                location.bearing,
                                location.speed,
                                location.timestamp,
                                location.provider,
                                0
                            )
                        )
                    }

                    cursor
                }

                is Result.Failure -> {
                    logger?.e(TAG, "Failed to query locations: ${result.error.message}")
                    MatrixCursor(arrayOf(IPCContract.Provider.Columns.ID))
                }

                else -> {
                    logger?.w(TAG, "Unexpected result type")
                    MatrixCursor(arrayOf(IPCContract.Provider.Columns.ID))
                }
            }
        }
    }


    private fun queryLatestLocation(): Cursor {
        logger?.i(TAG, "Querying latest location")

        return runBlocking {
            val result = locationRepository!!.getLatestLocation()

            when (result) {
                is Result.Success -> {
                    val location = result.data

                    val cursor = MatrixCursor(
                        arrayOf(
                            IPCContract.Provider.Columns.ID,
                            IPCContract.Provider.Columns.LATITUDE,
                            IPCContract.Provider.Columns.LONGITUDE,
                            IPCContract.Provider.Columns.ACCURACY,
                            IPCContract.Provider.Columns.ALTITUDE,
                            IPCContract.Provider.Columns.BEARING,
                            IPCContract.Provider.Columns.SPEED,
                            IPCContract.Provider.Columns.TIMESTAMP,
                            IPCContract.Provider.Columns.PROVIDER,
                            IPCContract.Provider.Columns.IS_ENCRYPTED
                        )
                    )

                    if (location != null) {
                        logger?.i(TAG, "Retrieved latest location: id=${location.id}")
                        cursor.addRow(
                            arrayOf(
                                location.id,
                                location.latitude,
                                location.longitude,
                                location.accuracy,
                                location.altitude,
                                location.bearing,
                                location.speed,
                                location.timestamp,
                                location.provider,
                                0
                            )
                        )
                    } else {
                        logger?.i(TAG, "No locations found")
                    }

                    cursor
                }

                is Result.Failure -> {
                    logger?.e(TAG, "Failed to query latest location: ${result.error.message}")
                    MatrixCursor(arrayOf(IPCContract.Provider.Columns.ID))
                }

                else -> {
                    logger?.w(TAG, "Unexpected result type")
                    MatrixCursor(arrayOf(IPCContract.Provider.Columns.ID))
                }
            }
        }
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            LOCATIONS -> IPCContract.Provider.CONTENT_TYPE_DIR
            LATEST -> IPCContract.Provider.CONTENT_TYPE_ITEM
            else -> null
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0
}
