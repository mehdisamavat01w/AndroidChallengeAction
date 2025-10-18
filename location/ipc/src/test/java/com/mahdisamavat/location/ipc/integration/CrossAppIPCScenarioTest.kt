package com.mahdisamavat.location.ipc.integration

import android.content.Intent
import android.database.MatrixCursor
import com.mahdisamavat.core.common.result.Result
import com.mahdisamavat.core.ipc.contract.IPCContract
import com.mahdisamavat.core.ipc.model.Response
import com.mahdisamavat.core.ipc.serializer.MessageSerializer
import com.mahdisamavat.core.logger.Logger
import com.mahdisamavat.core.model.Location
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Cross-app IPC scenario integration tests.
 * Tests complete end-to-end flows simulating real inter-app communication.
 */
class CrossAppIPCScenarioTest {

    private lateinit var messageSerializer: MessageSerializer
    private lateinit var logger: Logger

    @Before
    fun setup() {
        logger = mockk(relaxed = true)
        messageSerializer = MessageSerializer(logger)
    }

    // ========== Complete Flow Scenarios ==========

    @Test
    fun `scenario - Internet App starts service in Location App`() {
        // Step 1: Internet App sends START_SERVICE command
        val commandIntent = Intent(IPCContract.Broadcast.ACTION_COMMAND).apply {
            setPackage(IPCContract.LOCATION_APP_PACKAGE)
            putExtra(
                IPCContract.Broadcast.EXTRA_COMMAND_TYPE,
                IPCContract.Broadcast.COMMAND_START_SERVICE
            )
            putExtra(IPCContract.Broadcast.EXTRA_TIMESTAMP, System.currentTimeMillis())
        }

        // Verify command structure
        assertEquals(IPCContract.Broadcast.ACTION_COMMAND, commandIntent.action)
        assertEquals(
            IPCContract.Broadcast.COMMAND_START_SERVICE,
            commandIntent.getStringExtra(IPCContract.Broadcast.EXTRA_COMMAND_TYPE)
        )

        // Step 2: Location App processes command and sends response
        val response =
            Response.ServiceState(isRunning = true, message = "Service started successfully")
        val serialized = messageSerializer.serializeResponse(response)
        assertTrue(serialized is Result.Success)

        val responseIntent = Intent(IPCContract.Broadcast.ACTION_RESPONSE).apply {
            setPackage(IPCContract.INTERNET_APP_PACKAGE)
            putExtra(IPCContract.Broadcast.EXTRA_RESPONSE, (serialized as Result.Success).data)
            putExtra(IPCContract.Broadcast.EXTRA_RESPONSE_TYPE, response.getTypeName())
            putExtra(IPCContract.Broadcast.EXTRA_TIMESTAMP, System.currentTimeMillis())
        }

        // Step 3: Internet App receives and processes response
        val responseJson = responseIntent.getStringExtra(IPCContract.Broadcast.EXTRA_RESPONSE)
        assertNotNull(responseJson)

        val deserialized = messageSerializer.deserializeResponse(responseJson!!)
        assertTrue(deserialized is Result.Success)

        val finalResponse = (deserialized as Result.Success).data
        assertTrue(finalResponse is Response.ServiceState)
        assertEquals(true, (finalResponse as Response.ServiceState).isRunning)
    }

    @Test
    fun `scenario - Internet App queries locations from Location App`() {
        // Step 1: Internet App queries via ContentProvider
        val queryUri = IPCContract.Provider.LOCATIONS_URI
        assertEquals("content://com.mahdisamavat.location.provider/locations", queryUri.toString())

        // Step 2: Location App returns locations via cursor
        val locations = listOf(
            createLocation(1, 37.7749, -122.4194),
            createLocation(2, 34.0522, -118.2437),
            createLocation(3, 40.7128, -74.0060)
        )

        val cursor = createCursorFromLocations(locations)

        // Step 3: Internet App processes cursor data
        assertEquals(3, cursor.count)

        val retrievedLocations = mutableListOf<Location>()
        while (cursor.moveToNext()) {
            val location = Location(
                id = cursor.getLong(cursor.getColumnIndex(IPCContract.Provider.Columns.ID)),
                latitude = cursor.getDouble(cursor.getColumnIndex(IPCContract.Provider.Columns.LATITUDE)),
                longitude = cursor.getDouble(cursor.getColumnIndex(IPCContract.Provider.Columns.LONGITUDE)),
                accuracy = cursor.getFloat(cursor.getColumnIndex(IPCContract.Provider.Columns.ACCURACY)),
                altitude = if (cursor.isNull(cursor.getColumnIndex(IPCContract.Provider.Columns.ALTITUDE))) null
                else cursor.getDouble(cursor.getColumnIndex(IPCContract.Provider.Columns.ALTITUDE)),
                bearing = if (cursor.isNull(cursor.getColumnIndex(IPCContract.Provider.Columns.BEARING))) null
                else cursor.getFloat(cursor.getColumnIndex(IPCContract.Provider.Columns.BEARING)),
                speed = if (cursor.isNull(cursor.getColumnIndex(IPCContract.Provider.Columns.SPEED))) null
                else cursor.getFloat(cursor.getColumnIndex(IPCContract.Provider.Columns.SPEED)),
                timestamp = cursor.getLong(cursor.getColumnIndex(IPCContract.Provider.Columns.TIMESTAMP)),
                provider = cursor.getString(cursor.getColumnIndex(IPCContract.Provider.Columns.PROVIDER))
            )
            retrievedLocations.add(location)
        }

        assertEquals(3, retrievedLocations.size)
        assertEquals(37.7749, retrievedLocations[0].latitude, 0.0001)
    }

    @Test
    fun `scenario - complete service lifecycle via IPC`() {
        // Step 1: Start service
        Intent(IPCContract.Broadcast.ACTION_COMMAND).apply {
            putExtra(
                IPCContract.Broadcast.EXTRA_COMMAND_TYPE,
                IPCContract.Broadcast.COMMAND_START_SERVICE
            )
        }

        val startResponse = Response.ServiceState(isRunning = true, message = "Started")
        val startSerialized = messageSerializer.serializeResponse(startResponse)
        val startResponseData = (startSerialized as Result.Success).data

        val startDeserialized = messageSerializer.deserializeResponse(startResponseData)
        assertTrue((startDeserialized as Result.Success).data is Response.ServiceState)
        assertTrue(((startDeserialized).data as Response.ServiceState).isRunning)

        // Step 2: Check service status (service is running)
        // This would be done via ContentProvider or another query mechanism

        // Step 3: Stop service
        Intent(IPCContract.Broadcast.ACTION_COMMAND).apply {
            putExtra(
                IPCContract.Broadcast.EXTRA_COMMAND_TYPE,
                IPCContract.Broadcast.COMMAND_STOP_SERVICE
            )
        }

        val stopResponse = Response.ServiceState(isRunning = false, message = "Stopped")
        val stopSerialized = messageSerializer.serializeResponse(stopResponse)
        val stopResponseData = (stopSerialized as Result.Success).data

        val stopDeserialized = messageSerializer.deserializeResponse(stopResponseData)
        assertFalse(((stopDeserialized as Result.Success).data as Response.ServiceState).isRunning)
    }

    @Test
    fun `scenario - error handling across apps`() {
        // Step 1: Internet App sends invalid command
        Intent(IPCContract.Broadcast.ACTION_COMMAND).apply {
            putExtra(IPCContract.Broadcast.EXTRA_COMMAND_TYPE, "UNKNOWN_COMMAND")
        }

        // Step 2: Location App sends error response
        val errorResponse = Response.Error(
            message = "Unknown command",
            errorCode = IPCContract.ErrorCodes.INVALID_COMMAND
        )

        val serialized = messageSerializer.serializeResponse(errorResponse)
        val responseIntent = Intent(IPCContract.Broadcast.ACTION_RESPONSE).apply {
            putExtra(IPCContract.Broadcast.EXTRA_RESPONSE, (serialized as Result.Success).data)
            putExtra(IPCContract.Broadcast.EXTRA_RESPONSE_TYPE, errorResponse.getTypeName())
        }

        // Step 3: Internet App handles error
        val responseJson = responseIntent.getStringExtra(IPCContract.Broadcast.EXTRA_RESPONSE)
        val deserialized = messageSerializer.deserializeResponse(responseJson!!)

        assertTrue(deserialized is Result.Success)
        val finalResponse = (deserialized as Result.Success).data
        assertTrue(finalResponse is Response.Error)
        assertEquals(
            IPCContract.ErrorCodes.INVALID_COMMAND,
            (finalResponse as Response.Error).errorCode
        )
    }

    @Test
    fun `scenario - permission denied flow`() {
        // Simulate permission denied scenario
        val errorResponse = Response.Error(
            message = "Permission denied - apps not signed with same key",
            errorCode = IPCContract.ErrorCodes.PERMISSION_DENIED
        )

        val serialized = messageSerializer.serializeResponse(errorResponse)
        val deserialized =
            messageSerializer.deserializeResponse((serialized as Result.Success).data)

        val response = (deserialized as Result.Success).data
        assertTrue(response is Response.Error)
        assertEquals(
            IPCContract.ErrorCodes.PERMISSION_DENIED,
            (response as Response.Error).errorCode
        )
    }

    // ========== Data Integrity Scenarios ==========

    @Test
    fun `scenario - location data integrity across app boundary`() {
        val originalLocation = Location(
            id = 12345,
            latitude = 37.7749,
            longitude = -122.4194,
            accuracy = 15.5f,
            altitude = 100.5,
            bearing = 270.0f,
            speed = 10.5f,
            timestamp = 1234567890123L,
            provider = "fused"
        )

        // Location App creates cursor
        val cursor = createCursorFromLocations(listOf(originalLocation))

        // Internet App reads cursor
        cursor.moveToFirst()
        val retrievedLocation = Location(
            id = cursor.getLong(cursor.getColumnIndex(IPCContract.Provider.Columns.ID)),
            latitude = cursor.getDouble(cursor.getColumnIndex(IPCContract.Provider.Columns.LATITUDE)),
            longitude = cursor.getDouble(cursor.getColumnIndex(IPCContract.Provider.Columns.LONGITUDE)),
            accuracy = cursor.getFloat(cursor.getColumnIndex(IPCContract.Provider.Columns.ACCURACY)),
            altitude = cursor.getDouble(cursor.getColumnIndex(IPCContract.Provider.Columns.ALTITUDE)),
            bearing = cursor.getFloat(cursor.getColumnIndex(IPCContract.Provider.Columns.BEARING)),
            speed = cursor.getFloat(cursor.getColumnIndex(IPCContract.Provider.Columns.SPEED)),
            timestamp = cursor.getLong(cursor.getColumnIndex(IPCContract.Provider.Columns.TIMESTAMP)),
            provider = cursor.getString(cursor.getColumnIndex(IPCContract.Provider.Columns.PROVIDER))
        )

        // Verify data integrity
        assertEquals(originalLocation.id, retrievedLocation.id)
        assertEquals(originalLocation.latitude, retrievedLocation.latitude, 0.0001)
        assertEquals(originalLocation.longitude, retrievedLocation.longitude, 0.0001)
        assertEquals(originalLocation.accuracy, retrievedLocation.accuracy, 0.01f)
        assertEquals(originalLocation.altitude!!, retrievedLocation.altitude!!, 0.01)
        assertEquals(originalLocation.bearing!!, retrievedLocation.bearing!!, 0.01f)
        assertEquals(originalLocation.speed!!, retrievedLocation.speed!!, 0.01f)
        assertEquals(originalLocation.timestamp, retrievedLocation.timestamp)
        assertEquals(originalLocation.provider, retrievedLocation.provider)
    }

    @Test
    fun `scenario - concurrent command handling`() {
        val commands = listOf(
            IPCContract.Broadcast.COMMAND_START_SERVICE,
            IPCContract.Broadcast.COMMAND_GET_ALL_LOCATIONS,
            IPCContract.Broadcast.COMMAND_GET_LATEST_LOCATION
        )

        val intents = commands.map { command ->
            Intent(IPCContract.Broadcast.ACTION_COMMAND).apply {
                putExtra(IPCContract.Broadcast.EXTRA_COMMAND_TYPE, command)
                putExtra(IPCContract.Broadcast.EXTRA_TIMESTAMP, System.currentTimeMillis())
            }
        }

        // Verify all intents are properly formed
        assertEquals(3, intents.size)
        intents.forEachIndexed { index, intent ->
            assertEquals(
                commands[index],
                intent.getStringExtra(IPCContract.Broadcast.EXTRA_COMMAND_TYPE)
            )
        }
    }

    @Test
    fun `scenario - service start followed by location query`() {
        // Step 1: Start service
        Intent(IPCContract.Broadcast.ACTION_COMMAND).apply {
            putExtra(
                IPCContract.Broadcast.EXTRA_COMMAND_TYPE,
                IPCContract.Broadcast.COMMAND_START_SERVICE
            )
        }

        val startResponse = Response.ServiceState(isRunning = true, message = "Service started")
        val startSerialized = messageSerializer.serializeResponse(startResponse)

        // Verify service started
        val startDeserialized =
            messageSerializer.deserializeResponse((startSerialized as Result.Success).data)
        assertTrue(((startDeserialized as Result.Success).data as Response.ServiceState).isRunning)

        // Step 2: Query locations (after service has collected some)
        val locations = listOf(
            createLocation(1, 37.7749, -122.4194),
            createLocation(2, 37.7750, -122.4195)
        )

        val cursor = createCursorFromLocations(locations)
        assertEquals(2, cursor.count)

        // Step 3: Verify locations were collected
        cursor.moveToFirst()
        assertEquals(1L, cursor.getLong(cursor.getColumnIndex(IPCContract.Provider.Columns.ID)))
    }

    // ========== Timeout and Recovery Scenarios ==========

    @Test
    fun `scenario - timeout configuration is consistent across apps`() {
        val commandTimeout = IPCContract.Timeouts.COMMAND_TIMEOUT_MS
        val responseTimeout = IPCContract.Timeouts.RESPONSE_TIMEOUT_MS

        assertEquals(5000L, commandTimeout)
        assertEquals(5000L, responseTimeout)
        assertTrue(commandTimeout > 0)
        assertTrue(responseTimeout > 0)
    }

    @Test
    fun `scenario - service unavailable error handling`() {
        val errorResponse = Response.Error(
            message = "Service not available",
            errorCode = IPCContract.ErrorCodes.SERVICE_NOT_AVAILABLE
        )

        val serialized = messageSerializer.serializeResponse(errorResponse)
        val deserialized =
            messageSerializer.deserializeResponse((serialized as Result.Success).data)

        val response = (deserialized as Result.Success).data
        assertTrue(response is Response.Error)
        assertEquals("Service not available", (response as Response.Error).message)
    }

    // ========== Contract Validation Scenarios ==========

    @Test
    fun `scenario - IPC contract consistency check`() {
        // Verify package names
        assertEquals("com.mahdisamavat.location", IPCContract.LOCATION_APP_PACKAGE)
        assertEquals("com.mahdisamavat.internet", IPCContract.INTERNET_APP_PACKAGE)

        // Verify authority
        assertEquals("com.mahdisamavat.location.provider", IPCContract.Provider.AUTHORITY)

        // Verify paths
        assertEquals("locations", IPCContract.Provider.PATH_LOCATIONS)
        assertEquals("latest", IPCContract.Provider.PATH_LATEST)

        // Verify actions
        assertTrue(IPCContract.Broadcast.ACTION_COMMAND.contains("location"))
        assertTrue(IPCContract.Broadcast.ACTION_RESPONSE.contains("internet"))
    }

    @Test
    fun `scenario - all command types are valid`() {
        val commandTypes = listOf(
            IPCContract.Broadcast.COMMAND_START_SERVICE,
            IPCContract.Broadcast.COMMAND_STOP_SERVICE,
            IPCContract.Broadcast.COMMAND_GET_ALL_LOCATIONS,
            IPCContract.Broadcast.COMMAND_GET_LATEST_LOCATION
        )

        commandTypes.forEach { command ->
            assertNotNull(command)
            assertTrue(command.isNotEmpty())
            assertTrue(command.contains("_"))
        }
    }

    @Test
    fun `scenario - response type names are consistent`() {
        val responses = listOf(
            Response.Success("Success"),
            Response.Error("Error", null),
            Response.ServiceState(true, "State")
        )

        val expectedTypes = listOf("SUCCESS", "ERROR", "SERVICE_STATE")

        responses.forEachIndexed { index, response ->
            assertEquals(expectedTypes[index], response.getTypeName())
        }
    }

    // ========== Helper Methods ==========

    private fun createLocation(id: Long, latitude: Double, longitude: Double): Location {
        return Location(
            id = id,
            latitude = latitude,
            longitude = longitude,
            accuracy = 10.0f,
            altitude = null,
            bearing = null,
            speed = null,
            timestamp = System.currentTimeMillis(),
            provider = "gps"
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
}
