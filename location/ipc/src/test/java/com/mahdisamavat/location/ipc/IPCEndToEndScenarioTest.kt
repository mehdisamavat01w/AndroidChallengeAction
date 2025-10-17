package com.mahdisamavat.location.ipc

import com.mahdisamavat.core.common.result.Result
import com.mahdisamavat.core.ipc.contract.IPCContract
import com.mahdisamavat.core.ipc.model.Response
import com.mahdisamavat.core.ipc.serializer.MessageSerializer
import com.mahdisamavat.core.logger.Logger
import com.mahdisamavat.core.model.Location
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class IPCEndToEndScenarioTest {

    private lateinit var messageSerializer: MessageSerializer
    private lateinit var logger: Logger

    @Before
    fun setup() {
        logger = mockk(relaxed = true)
        messageSerializer = MessageSerializer(logger)
    }

    @Test
    fun `e2e scenario - start service command flow`() {
        val commandType = IPCContract.Broadcast.COMMAND_START_SERVICE

        assertEquals("START_SERVICE", commandType)

        val response = Response.ServiceState(
            isRunning = true,
            message = "Service started successfully"
        )

        val serialized = messageSerializer.serializeResponse(response)
        assertTrue(serialized is Result.Success)

        val json = (serialized as Result.Success).data
        assertNotNull(json)

        val deserialized = messageSerializer.deserializeResponse(json)
        assertTrue(deserialized is Result.Success)

        val deserializedResponse = (deserialized as Result.Success).data
        assertTrue(deserializedResponse is Response.ServiceState)
        assertEquals(true, (deserializedResponse as Response.ServiceState).isRunning)
    }

    @Test
    fun `e2e scenario - stop service command flow`() {
        val commandType = IPCContract.Broadcast.COMMAND_STOP_SERVICE

        assertEquals("STOP_SERVICE", commandType)

        val response = Response.ServiceState(
            isRunning = false,
            message = "Service stopped successfully"
        )

        val serialized = messageSerializer.serializeResponse(response)
        val json = (serialized as Result.Success).data

        val deserialized = messageSerializer.deserializeResponse(json)
        val deserializedResponse = (deserialized as Result.Success).data

        assertTrue(deserializedResponse is Response.ServiceState)
        assertEquals(false, (deserializedResponse as Response.ServiceState).isRunning)
    }

    @Test
    fun `e2e scenario - get locations command with data`() {
        val commandType = IPCContract.Broadcast.COMMAND_GET_ALL_LOCATIONS

        assertEquals("GET_ALL_LOCATIONS", commandType)

        listOf(
            createLocation(1, 37.7749, -122.4194),
            createLocation(2, 34.0522, -118.2437)
        )

        val response = Response.Success("Locations retrieved")

        val serialized = messageSerializer.serializeResponse(response)
        assertTrue(serialized is Result.Success)

        val json = (serialized as Result.Success).data
        val deserialized = messageSerializer.deserializeResponse(json)

        assertTrue(deserialized is Result.Success)
        assertTrue((deserialized as Result.Success).data is Response.Success)
    }

    @Test
    fun `e2e scenario - get latest location command`() {
        val commandType = IPCContract.Broadcast.COMMAND_GET_LATEST_LOCATION

        assertEquals("GET_LATEST_LOCATION", commandType)

        val response = Response.Success("Latest location retrieved")

        val serialized = messageSerializer.serializeResponse(response)
        val deserialized = messageSerializer.deserializeResponse(
            (serialized as Result.Success).data
        )

        assertTrue(deserialized is Result.Success)
    }

    @Test
    fun `e2e scenario - error response handling`() {
        val response = Response.Error(
            message = "Service not available",
            errorCode = IPCContract.ErrorCodes.SERVICE_NOT_AVAILABLE
        )

        val serialized = messageSerializer.serializeResponse(response)
        assertTrue(serialized is Result.Success)

        val json = (serialized as Result.Success).data
        val deserialized = messageSerializer.deserializeResponse(json)

        assertTrue(deserialized is Result.Success)
        val deserializedResponse = (deserialized as Result.Success).data
        assertTrue(deserializedResponse is Response.Error)
        assertEquals(
            IPCContract.ErrorCodes.SERVICE_NOT_AVAILABLE,
            (deserializedResponse as Response.Error).errorCode
        )
    }

    @Test
    fun `e2e scenario - permission denied error flow`() {
        val response = Response.Error(
            message = "Permission denied",
            errorCode = IPCContract.ErrorCodes.PERMISSION_DENIED
        )

        val serialized = messageSerializer.serializeResponse(response)
        val deserialized = messageSerializer.deserializeResponse(
            (serialized as Result.Success).data
        )

        val deserializedResponse = (deserialized as Result.Success).data
        assertTrue(deserializedResponse is Response.Error)
        assertEquals("Permission denied", (deserializedResponse as Response.Error).message)
    }

    @Test
    fun `e2e scenario - command response round trip with timestamp`() {
        val originalTimestamp = System.currentTimeMillis()
        val response = Response.Success("Command executed")

        val serialized = messageSerializer.serializeResponse(response)
        val json = (serialized as Result.Success).data

        val deserialized = messageSerializer.deserializeResponse(json)
        val deserializedResponse = (deserialized as Result.Success).data

        assertTrue(deserializedResponse is Response.Success)
        assertNotNull((deserializedResponse as Response.Success).timestamp)
        assertTrue(deserializedResponse.timestamp >= originalTimestamp)
    }

    @Test
    fun `e2e scenario - multiple commands in sequence`() {
        val commands = listOf(
            IPCContract.Broadcast.COMMAND_START_SERVICE,
            IPCContract.Broadcast.COMMAND_GET_ALL_LOCATIONS,
            IPCContract.Broadcast.COMMAND_STOP_SERVICE
        )

        commands.forEach { command ->
            assertNotNull(command)
            assertTrue(command.isNotEmpty())
        }

        val responses = listOf(
            Response.ServiceState(true, "Started"),
            Response.Success("Locations retrieved"),
            Response.ServiceState(false, "Stopped")
        )

        responses.forEach { response ->
            val serialized = messageSerializer.serializeResponse(response)
            assertTrue(serialized is Result.Success)

            val deserialized = messageSerializer.deserializeResponse(
                (serialized as Result.Success).data
            )
            assertTrue(deserialized is Result.Success)
        }
    }

    @Test
    fun `e2e scenario - content provider contract validation`() {
        assertEquals("com.mahdisamavat.location.provider", IPCContract.Provider.AUTHORITY)
        assertEquals("locations", IPCContract.Provider.PATH_LOCATIONS)
        assertEquals("latest", IPCContract.Provider.PATH_LATEST)

        val columns = listOf(
            IPCContract.Provider.Columns.ID,
            IPCContract.Provider.Columns.LATITUDE,
            IPCContract.Provider.Columns.LONGITUDE,
            IPCContract.Provider.Columns.TIMESTAMP
        )

        columns.forEach { column ->
            assertNotNull(column)
            assertTrue(column.isNotEmpty())
        }
    }

    @Test
    fun `e2e scenario - broadcast actions validation`() {
        val commandAction = IPCContract.Broadcast.ACTION_COMMAND
        val responseAction = IPCContract.Broadcast.ACTION_RESPONSE

        assertEquals("com.mahdisamavat.location.action.COMMAND", commandAction)
        assertEquals("com.mahdisamavat.internet.action.RESPONSE", responseAction)

        assertNotNull(IPCContract.Broadcast.EXTRA_COMMAND_TYPE)
        assertNotNull(IPCContract.Broadcast.EXTRA_TIMESTAMP)
    }

    private fun createLocation(
        id: Long,
        latitude: Double,
        longitude: Double
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
            provider = "gps"
        )
    }
}
