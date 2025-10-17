package com.mahdisamavat.location.ipc

import com.mahdisamavat.core.ipc.contract.IPCContract
import com.mahdisamavat.core.ipc.model.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CommandHandlingTest {

    @Test
    fun `IPC contract defines correct command types`() {
        assertEquals("START_SERVICE", IPCContract.Broadcast.COMMAND_START_SERVICE)
        assertEquals("STOP_SERVICE", IPCContract.Broadcast.COMMAND_STOP_SERVICE)
        assertEquals("GET_ALL_LOCATIONS", IPCContract.Broadcast.COMMAND_GET_ALL_LOCATIONS)
        assertEquals("GET_LATEST_LOCATION", IPCContract.Broadcast.COMMAND_GET_LATEST_LOCATION)
    }

    @Test
    fun `IPC contract defines broadcast action`() {
        assertEquals(
            "com.mahdisamavat.location.action.COMMAND",
            IPCContract.Broadcast.ACTION_COMMAND
        )
        assertEquals(
            "com.mahdisamavat.internet.action.RESPONSE",
            IPCContract.Broadcast.ACTION_RESPONSE
        )
    }

    @Test
    fun `IPC contract defines extra keys`() {
        assertEquals("extra_command_type", IPCContract.Broadcast.EXTRA_COMMAND_TYPE)
        assertEquals("extra_response", IPCContract.Broadcast.EXTRA_RESPONSE)
        assertEquals("extra_timestamp", IPCContract.Broadcast.EXTRA_TIMESTAMP)
    }

    @Test
    fun `service state response has correct structure`() {
        val response = Response.ServiceState(
            isRunning = true,
            message = "Service started"
        )

        assertEquals("SERVICE_STATE", response.getTypeName())
        assertEquals(true, response.isRunning)
        assertEquals("Service started", response.message)
        assertNotNull(response.timestamp)
    }

    @Test
    fun `error response has correct structure`() {
        val response = Response.Error(
            message = "Command failed",
            errorCode = IPCContract.ErrorCodes.SERVICE_NOT_AVAILABLE
        )

        assertEquals("ERROR", response.getTypeName())
        assertEquals("Command failed", response.message)
        assertEquals(IPCContract.ErrorCodes.SERVICE_NOT_AVAILABLE, response.errorCode)
        assertNotNull(response.timestamp)
    }

    @Test
    fun `success response has correct structure`() {
        val response = Response.Success("Operation completed")

        assertEquals("SUCCESS", response.getTypeName())
        assertEquals("Operation completed", response.message)
        assertNotNull(response.timestamp)
    }

    @Test
    fun `error codes are defined correctly`() {
        assertEquals("SERVICE_NOT_AVAILABLE", IPCContract.ErrorCodes.SERVICE_NOT_AVAILABLE)
        assertEquals("PERMISSION_DENIED", IPCContract.ErrorCodes.PERMISSION_DENIED)
        assertEquals("INVALID_COMMAND", IPCContract.ErrorCodes.INVALID_COMMAND)
        assertEquals("UNKNOWN_ERROR", IPCContract.ErrorCodes.UNKNOWN_ERROR)
        assertEquals("TIMEOUT", IPCContract.ErrorCodes.TIMEOUT)
        assertEquals("ENCRYPTION_FAILED", IPCContract.ErrorCodes.ENCRYPTION_FAILED)
    }

    @Test
    fun `content provider authority is defined correctly`() {
        assertEquals("com.mahdisamavat.location.provider", IPCContract.Provider.AUTHORITY)
    }

    @Test
    fun `content provider paths are defined`() {
        assertEquals("locations", IPCContract.Provider.PATH_LOCATIONS)
        assertEquals("latest", IPCContract.Provider.PATH_LATEST)
    }

    @Test
    fun `content provider column names are defined`() {
        assertEquals("id", IPCContract.Provider.Columns.ID)
        assertEquals("latitude", IPCContract.Provider.Columns.LATITUDE)
        assertEquals("longitude", IPCContract.Provider.Columns.LONGITUDE)
        assertEquals("accuracy", IPCContract.Provider.Columns.ACCURACY)
        assertEquals("timestamp", IPCContract.Provider.Columns.TIMESTAMP)
        assertEquals("provider", IPCContract.Provider.Columns.PROVIDER)
        assertEquals("altitude", IPCContract.Provider.Columns.ALTITUDE)
        assertEquals("bearing", IPCContract.Provider.Columns.BEARING)
        assertEquals("speed", IPCContract.Provider.Columns.SPEED)
    }
}
