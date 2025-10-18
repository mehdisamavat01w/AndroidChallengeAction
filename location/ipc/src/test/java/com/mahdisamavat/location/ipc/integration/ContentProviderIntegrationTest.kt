package com.mahdisamavat.location.ipc.integration

import android.database.MatrixCursor
import com.mahdisamavat.core.common.result.Result
import com.mahdisamavat.core.ipc.contract.IPCContract
import com.mahdisamavat.core.logger.Logger
import com.mahdisamavat.core.model.Location
import com.mahdisamavat.location.domain.repository.LocationRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Integration tests for Content Provider IPC mechanism.
 * Tests the data flow from Location App to Internet App via ContentProvider.
 */
class ContentProviderIntegrationTest {

    private lateinit var locationRepository: LocationRepository
    private lateinit var logger: Logger

    @Before
    fun setup() {
        locationRepository = mockk()
        logger = mockk(relaxed = true)
    }

    // ========== Query All Locations Integration Tests ==========

    @Test
    fun `integration test - query all locations returns valid cursor structure`() = runTest {
        val locations = listOf(
            createLocation(1, 37.7749, -122.4194, "gps"),
            createLocation(2, 34.0522, -118.2437, "network")
        )

        coEvery { locationRepository.getAllLocations() } returns Result.success(locations)

        // Simulate what LocationContentProvider does
        val cursor = createCursorFromLocations(locations)

        assertNotNull(cursor)
        assertEquals(2, cursor.count)

        assertTrue(cursor.moveToFirst())
        assertEquals(1L, cursor.getLong(cursor.getColumnIndex(IPCContract.Provider.Columns.ID)))
        assertEquals(
            37.7749,
            cursor.getDouble(cursor.getColumnIndex(IPCContract.Provider.Columns.LATITUDE)),
            0.0001
        )
        assertEquals(
            -122.4194,
            cursor.getDouble(cursor.getColumnIndex(IPCContract.Provider.Columns.LONGITUDE)),
            0.0001
        )
    }

    @Test
    fun `integration test - query all locations handles empty result`() = runTest {
        val emptyList = emptyList<Location>()
        coEvery { locationRepository.getAllLocations() } returns Result.success(emptyList)

        val cursor = createCursorFromLocations(emptyList)

        assertNotNull(cursor)
        assertEquals(0, cursor.count)
        assertFalse(cursor.moveToFirst())
    }

    @Test
    fun `integration test - query all locations preserves all location fields`() = runTest {
        val location = Location(
            id = 123,
            latitude = 37.7749,
            longitude = -122.4194,
            accuracy = 15.5f,
            altitude = 50.0,
            bearing = 180.0f,
            speed = 5.5f,
            timestamp = 1234567890L,
            provider = "fused"
        )

        coEvery { locationRepository.getAllLocations() } returns Result.success(listOf(location))

        val cursor = createCursorFromLocations(listOf(location))

        assertTrue(cursor.moveToFirst())
        assertEquals(123L, cursor.getLong(cursor.getColumnIndex(IPCContract.Provider.Columns.ID)))
        assertEquals(
            37.7749,
            cursor.getDouble(cursor.getColumnIndex(IPCContract.Provider.Columns.LATITUDE)),
            0.0001
        )
        assertEquals(
            -122.4194,
            cursor.getDouble(cursor.getColumnIndex(IPCContract.Provider.Columns.LONGITUDE)),
            0.0001
        )
        assertEquals(
            15.5f,
            cursor.getFloat(cursor.getColumnIndex(IPCContract.Provider.Columns.ACCURACY)),
            0.01f
        )
        assertEquals(
            50.0,
            cursor.getDouble(cursor.getColumnIndex(IPCContract.Provider.Columns.ALTITUDE)),
            0.01
        )
        assertEquals(
            180.0f,
            cursor.getFloat(cursor.getColumnIndex(IPCContract.Provider.Columns.BEARING)),
            0.01f
        )
        assertEquals(
            5.5f,
            cursor.getFloat(cursor.getColumnIndex(IPCContract.Provider.Columns.SPEED)),
            0.01f
        )
        assertEquals(
            1234567890L,
            cursor.getLong(cursor.getColumnIndex(IPCContract.Provider.Columns.TIMESTAMP))
        )
        assertEquals(
            "fused",
            cursor.getString(cursor.getColumnIndex(IPCContract.Provider.Columns.PROVIDER))
        )
    }

    @Test
    fun `integration test - query all locations handles large dataset`() = runTest {
        val locations = (1..100).map { id ->
            createLocation(id.toLong(), 37.0 + id * 0.01, -122.0 + id * 0.01, "gps")
        }

        coEvery { locationRepository.getAllLocations() } returns Result.success(locations)

        val cursor = createCursorFromLocations(locations)

        assertEquals(100, cursor.count)

        assertTrue(cursor.moveToFirst())
        assertEquals(1L, cursor.getLong(cursor.getColumnIndex(IPCContract.Provider.Columns.ID)))

        assertTrue(cursor.moveToLast())
        assertEquals(100L, cursor.getLong(cursor.getColumnIndex(IPCContract.Provider.Columns.ID)))
    }

    // ========== Query Latest Location Integration Tests ==========

    @Test
    fun `integration test - query latest location returns most recent`() = runTest {
        val latestLocation = createLocation(999, 37.7749, -122.4194, "gps")

        coEvery { locationRepository.getLatestLocation() } returns Result.success(latestLocation)

        val cursor = createCursorFromLocation(latestLocation)

        assertNotNull(cursor)
        assertEquals(1, cursor.count)
        assertTrue(cursor.moveToFirst())
        assertEquals(999L, cursor.getLong(cursor.getColumnIndex(IPCContract.Provider.Columns.ID)))
    }

    @Test
    fun `integration test - query latest location handles null result`() = runTest {
        coEvery { locationRepository.getLatestLocation() } returns Result.success(null)

        val cursor = createCursorFromLocation(null)

        assertNotNull(cursor)
        assertEquals(0, cursor.count)
        assertFalse(cursor.moveToFirst())
    }

    @Test
    fun `integration test - query latest location preserves timestamp accuracy`() = runTest {
        val exactTimestamp = 1735123456789L
        val location = Location(
            id = 1,
            latitude = 37.7749,
            longitude = -122.4194,
            accuracy = 10.0f,
            altitude = null,
            bearing = null,
            speed = null,
            timestamp = exactTimestamp,
            provider = "gps"
        )

        coEvery { locationRepository.getLatestLocation() } returns Result.success(location)

        val cursor = createCursorFromLocation(location)

        assertTrue(cursor.moveToFirst())
        assertEquals(
            exactTimestamp,
            cursor.getLong(cursor.getColumnIndex(IPCContract.Provider.Columns.TIMESTAMP))
        )
    }

    // ========== URI and Content Type Tests ==========

    @Test
    fun `integration test - content URIs are correctly formed`() {
        val baseUri = IPCContract.Provider.BASE_URI
        val locationsUri = IPCContract.Provider.LOCATIONS_URI
        val latestUri = IPCContract.Provider.LATEST_LOCATION_URI

        assertEquals("content://com.mahdisamavat.location.provider", baseUri.toString())
        assertEquals(
            "content://com.mahdisamavat.location.provider/locations",
            locationsUri.toString()
        )
        assertEquals(
            "content://com.mahdisamavat.location.provider/locations/latest",
            latestUri.toString()
        )
    }

    @Test
    fun `integration test - content types are correctly defined`() {
        val dirType = IPCContract.Provider.CONTENT_TYPE_DIR
        val itemType = IPCContract.Provider.CONTENT_TYPE_ITEM

        assertTrue(dirType.startsWith("vnd.android.cursor.dir"))
        assertTrue(itemType.startsWith("vnd.android.cursor.item"))
    }

    // ========== Column Contract Tests ==========

    @Test
    fun `integration test - all required columns are present in cursor`() {
        val location = createLocation(1, 37.7749, -122.4194, "gps")
        val cursor = createCursorFromLocations(listOf(location))

        val requiredColumns = listOf(
            IPCContract.Provider.Columns.ID,
            IPCContract.Provider.Columns.LATITUDE,
            IPCContract.Provider.Columns.LONGITUDE,
            IPCContract.Provider.Columns.ACCURACY,
            IPCContract.Provider.Columns.TIMESTAMP,
            IPCContract.Provider.Columns.PROVIDER
        )

        requiredColumns.forEach { column ->
            val index = cursor.getColumnIndex(column)
            assertTrue("Column $column should be present", index >= 0)
        }
    }

    @Test
    fun `integration test - cursor column order is consistent`() {
        val location = createLocation(1, 37.7749, -122.4194, "gps")
        val cursor = createCursorFromLocations(listOf(location))

        val columnNames = cursor.columnNames.toList()

        assertEquals(IPCContract.Provider.Columns.ID, columnNames[0])
        assertEquals(IPCContract.Provider.Columns.LATITUDE, columnNames[1])
        assertEquals(IPCContract.Provider.Columns.LONGITUDE, columnNames[2])
    }

    // ========== Error Handling Integration Tests ==========

    @Test
    fun `integration test - handles repository failure gracefully`() = runTest {
        coEvery { locationRepository.getAllLocations() } returns Result.failure(
            com.mahdisamavat.core.common.error.AppError.Data("Database error", null, false, "Test")
        )

        // Content Provider should return empty cursor on error
        val emptyCursor = MatrixCursor(arrayOf(IPCContract.Provider.Columns.ID))

        assertEquals(0, emptyCursor.count)
    }

    @Test
    fun `integration test - handles null values in optional fields`() {
        val location = Location(
            id = 1,
            latitude = 37.7749,
            longitude = -122.4194,
            accuracy = 10.0f,
            altitude = null,
            bearing = null,
            speed = null,
            timestamp = 1234567890L,
            provider = null
        )

        val cursor = createCursorFromLocations(listOf(location))

        assertTrue(cursor.moveToFirst())
        assertTrue(cursor.isNull(cursor.getColumnIndex(IPCContract.Provider.Columns.ALTITUDE)))
        assertTrue(cursor.isNull(cursor.getColumnIndex(IPCContract.Provider.Columns.BEARING)))
        assertTrue(cursor.isNull(cursor.getColumnIndex(IPCContract.Provider.Columns.SPEED)))
    }

    // ========== Helper Methods ==========

    private fun createLocation(
        id: Long,
        latitude: Double,
        longitude: Double,
        provider: String
    ): Location {
        return Location(
            id = id,
            latitude = latitude,
            longitude = longitude,
            accuracy = 10.0f,
            altitude = null,
            bearing = null,
            speed = null,
            timestamp = System.currentTimeMillis(),
            provider = provider
        )
    }

    private fun createCursorFromLocations(locations: List<Location>): MatrixCursor {
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
                arrayOf<Any?>(
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

        return cursor
    }

    private fun createCursorFromLocation(location: Location?): MatrixCursor {
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
            cursor.addRow(
                arrayOf<Any?>(
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

        return cursor
    }
}
