package com.mahdisamavat.location.ipc.integration

import android.content.Intent
import com.mahdisamavat.core.common.result.Result
import com.mahdisamavat.core.ipc.contract.IPCContract
import com.mahdisamavat.core.ipc.model.Response
import com.mahdisamavat.core.ipc.serializer.MessageSerializer
import com.mahdisamavat.core.logger.Logger
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Integration tests for Broadcast-based IPC mechanism.
 * Tests command/response flow between Internet App and Location App.
 */
class BroadcastIPCIntegrationTest {

    private lateinit var messageSerializer: MessageSerializer
    private lateinit var logger: Logger

    @Before
    fun setup() {
        logger = mockk(relaxed = true)
        messageSerializer = MessageSerializer(logger)
    }

    // ========== Command Intent Structure Tests ==========

    @Test
    fun `integration test - START_SERVICE command intent is properly formed`() {
        val intent = Intent(IPCContract.Broadcast.ACTION_COMMAND).apply {
            setPackage(IPCContract.LOCATION_APP_PACKAGE)
            putExtra(
                IPCContract.Broadcast.EXTRA_COMMAND_TYPE,
                IPCContract.Broadcast.COMMAND_START_SERVICE
            )
            putExtra(IPCContract.Broadcast.EXTRA_TIMESTAMP, System.currentTimeMillis())
        }

        assertEquals(IPCContract.Broadcast.ACTION_COMMAND, intent.action)
        assertEquals(IPCContract.LOCATION_APP_PACKAGE, intent.`package`)
        assertEquals(
            IPCContract.Broadcast.COMMAND_START_SERVICE,
            intent.getStringExtra(IPCContract.Broadcast.EXTRA_COMMAND_TYPE)
        )
        assertTrue(intent.hasExtra(IPCContract.Broadcast.EXTRA_TIMESTAMP))
    }

    @Test
    fun `integration test - STOP_SERVICE command intent is properly formed`() {
        val intent = Intent(IPCContract.Broadcast.ACTION_COMMAND).apply {
            setPackage(IPCContract.LOCATION_APP_PACKAGE)
            putExtra(
                IPCContract.Broadcast.EXTRA_COMMAND_TYPE,
                IPCContract.Broadcast.COMMAND_STOP_SERVICE
            )
            putExtra(IPCContract.Broadcast.EXTRA_TIMESTAMP, System.currentTimeMillis())
        }

        assertEquals(
            IPCContract.Broadcast.COMMAND_STOP_SERVICE,
            intent.getStringExtra(IPCContract.Broadcast.EXTRA_COMMAND_TYPE)
        )
        assertEquals(IPCContract.Broadcast.ACTION_COMMAND, intent.action)
    }

    @Test
    fun `integration test - GET_ALL_LOCATIONS command intent is properly formed`() {
        val intent = Intent(IPCContract.Broadcast.ACTION_COMMAND).apply {
            setPackage(IPCContract.LOCATION_APP_PACKAGE)
            putExtra(
                IPCContract.Broadcast.EXTRA_COMMAND_TYPE,
                IPCContract.Broadcast.COMMAND_GET_ALL_LOCATIONS
            )
            putExtra(IPCContract.Broadcast.EXTRA_TIMESTAMP, System.currentTimeMillis())
        }

        assertEquals(
            IPCContract.Broadcast.COMMAND_GET_ALL_LOCATIONS,
            intent.getStringExtra(IPCContract.Broadcast.EXTRA_COMMAND_TYPE)
        )
    }

    @Test
    fun `integration test - GET_LATEST_LOCATION command intent is properly formed`() {
        val intent = Intent(IPCContract.Broadcast.ACTION_COMMAND).apply {
            setPackage(IPCContract.LOCATION_APP_PACKAGE)
            putExtra(
                IPCContract.Broadcast.EXTRA_COMMAND_TYPE,
                IPCContract.Broadcast.COMMAND_GET_LATEST_LOCATION
            )
            putExtra(IPCContract.Broadcast.EXTRA_TIMESTAMP, System.currentTimeMillis())
        }

        assertEquals(
            IPCContract.Broadcast.COMMAND_GET_LATEST_LOCATION,
            intent.getStringExtra(IPCContract.Broadcast.EXTRA_COMMAND_TYPE)
        )
    }

    // ========== Response Intent Structure Tests ==========

    @Test
    fun `integration test - ServiceState response intent is properly formed`() {
        val response = Response.ServiceState(isRunning = true, message = "Service started")
        val serialized = messageSerializer.serializeResponse(response)
        assertTrue(serialized is Result.Success)

        val intent = Intent(IPCContract.Broadcast.ACTION_RESPONSE).apply {
            setPackage(IPCContract.INTERNET_APP_PACKAGE)
            putExtra(IPCContract.Broadcast.EXTRA_RESPONSE, (serialized as Result.Success).data)
            putExtra(IPCContract.Broadcast.EXTRA_RESPONSE_TYPE, response.getTypeName())
            putExtra(IPCContract.Broadcast.EXTRA_TIMESTAMP, System.currentTimeMillis())
        }

        assertEquals(IPCContract.Broadcast.ACTION_RESPONSE, intent.action)
        assertEquals(IPCContract.INTERNET_APP_PACKAGE, intent.`package`)
        assertTrue(intent.hasExtra(IPCContract.Broadcast.EXTRA_RESPONSE))
        assertEquals(
            "SERVICE_STATE",
            intent.getStringExtra(IPCContract.Broadcast.EXTRA_RESPONSE_TYPE)
        )
    }

    @Test
    fun `integration test - Success response intent is properly formed`() {
        val response = Response.Success(message = "Operation successful")
        val serialized = messageSerializer.serializeResponse(response)

        val intent = Intent(IPCContract.Broadcast.ACTION_RESPONSE).apply {
            setPackage(IPCContract.INTERNET_APP_PACKAGE)
            putExtra(IPCContract.Broadcast.EXTRA_RESPONSE, (serialized as Result.Success).data)
            putExtra(IPCContract.Broadcast.EXTRA_RESPONSE_TYPE, response.getTypeName())
        }

        assertEquals("SUCCESS", intent.getStringExtra(IPCContract.Broadcast.EXTRA_RESPONSE_TYPE))
    }

    @Test
    fun `integration test - Error response intent is properly formed`() {
        val response = Response.Error(
            message = "Service not available",
            errorCode = IPCContract.ErrorCodes.SERVICE_NOT_AVAILABLE
        )
        val serialized = messageSerializer.serializeResponse(response)

        val intent = Intent(IPCContract.Broadcast.ACTION_RESPONSE).apply {
            setPackage(IPCContract.INTERNET_APP_PACKAGE)
            putExtra(IPCContract.Broadcast.EXTRA_RESPONSE, (serialized as Result.Success).data)
            putExtra(IPCContract.Broadcast.EXTRA_RESPONSE_TYPE, response.getTypeName())
        }

        assertEquals("ERROR", intent.getStringExtra(IPCContract.Broadcast.EXTRA_RESPONSE_TYPE))
    }

    // ========== Command-Response Round Trip Tests ==========

    @Test
    fun `integration test - START_SERVICE command to ServiceState response round trip`() {
        // Command sent from Internet App
        val commandIntent = Intent(IPCContract.Broadcast.ACTION_COMMAND).apply {
            putExtra(
                IPCContract.Broadcast.EXTRA_COMMAND_TYPE,
                IPCContract.Broadcast.COMMAND_START_SERVICE
            )
        }

        val commandType = commandIntent.getStringExtra(IPCContract.Broadcast.EXTRA_COMMAND_TYPE)
        assertEquals(IPCContract.Broadcast.COMMAND_START_SERVICE, commandType)

        // Response sent from Location App
        val response = Response.ServiceState(isRunning = true, message = "Service started")
        val serialized = messageSerializer.serializeResponse(response)
        assertTrue(serialized is Result.Success)

        val responseJson = (serialized as Result.Success).data

        // Deserialize in Internet App
        val deserialized = messageSerializer.deserializeResponse(responseJson)
        assertTrue(deserialized is Result.Success)

        val deserializedResponse = (deserialized as Result.Success).data
        assertTrue(deserializedResponse is Response.ServiceState)
        assertEquals(true, (deserializedResponse as Response.ServiceState).isRunning)
    }

    @Test
    fun `integration test - STOP_SERVICE command to ServiceState response round trip`() {
        val commandIntent = Intent(IPCContract.Broadcast.ACTION_COMMAND).apply {
            putExtra(
                IPCContract.Broadcast.EXTRA_COMMAND_TYPE,
                IPCContract.Broadcast.COMMAND_STOP_SERVICE
            )
        }

        assertEquals(
            IPCContract.Broadcast.COMMAND_STOP_SERVICE,
            commandIntent.getStringExtra(IPCContract.Broadcast.EXTRA_COMMAND_TYPE)
        )

        val response = Response.ServiceState(isRunning = false, message = "Service stopped")
        val serialized = messageSerializer.serializeResponse(response)
        val responseJson = (serialized as Result.Success).data

        val deserialized = messageSerializer.deserializeResponse(responseJson)
        val deserializedResponse = (deserialized as Result.Success).data

        assertTrue(deserializedResponse is Response.ServiceState)
        assertEquals(false, (deserializedResponse as Response.ServiceState).isRunning)
    }

    @Test
    fun `integration test - command with error response round trip`() {
        Intent(IPCContract.Broadcast.ACTION_COMMAND).apply {
            putExtra(IPCContract.Broadcast.EXTRA_COMMAND_TYPE, "INVALID_COMMAND")
        }

        val response = Response.Error(
            message = "Unknown command",
            errorCode = IPCContract.ErrorCodes.INVALID_COMMAND
        )

        val serialized = messageSerializer.serializeResponse(response)
        val responseJson = (serialized as Result.Success).data

        val deserialized = messageSerializer.deserializeResponse(responseJson)
        val deserializedResponse = (deserialized as Result.Success).data

        assertTrue(deserializedResponse is Response.Error)
        assertEquals("Unknown command", (deserializedResponse as Response.Error).message)
        assertEquals(IPCContract.ErrorCodes.INVALID_COMMAND, deserializedResponse.errorCode)
    }

    // ========== Timestamp Verification Tests ==========

    @Test
    fun `integration test - command timestamp is preserved`() {
        val originalTimestamp = System.currentTimeMillis()

        val intent = Intent(IPCContract.Broadcast.ACTION_COMMAND).apply {
            putExtra(
                IPCContract.Broadcast.EXTRA_COMMAND_TYPE,
                IPCContract.Broadcast.COMMAND_START_SERVICE
            )
            putExtra(IPCContract.Broadcast.EXTRA_TIMESTAMP, originalTimestamp)
        }

        val retrievedTimestamp = intent.getLongExtra(IPCContract.Broadcast.EXTRA_TIMESTAMP, 0)
        assertEquals(originalTimestamp, retrievedTimestamp)
    }

    @Test
    fun `integration test - response timestamp is preserved`() {
        val response = Response.Success("Success")
        val serialized = messageSerializer.serializeResponse(response)

        val originalTimestamp = System.currentTimeMillis()
        val intent = Intent(IPCContract.Broadcast.ACTION_RESPONSE).apply {
            putExtra(IPCContract.Broadcast.EXTRA_RESPONSE, (serialized as Result.Success).data)
            putExtra(IPCContract.Broadcast.EXTRA_TIMESTAMP, originalTimestamp)
        }

        assertEquals(
            originalTimestamp,
            intent.getLongExtra(IPCContract.Broadcast.EXTRA_TIMESTAMP, 0)
        )
    }

    // ========== Error Code Handling Tests ==========

    @Test
    fun `integration test - all error codes are properly transmitted`() {
        val errorCodes = listOf(
            IPCContract.ErrorCodes.PERMISSION_DENIED,
            IPCContract.ErrorCodes.SERVICE_NOT_AVAILABLE,
            IPCContract.ErrorCodes.ENCRYPTION_FAILED,
            IPCContract.ErrorCodes.DECRYPTION_FAILED,
            IPCContract.ErrorCodes.INVALID_COMMAND,
            IPCContract.ErrorCodes.TIMEOUT,
            IPCContract.ErrorCodes.UNKNOWN_ERROR
        )

        errorCodes.forEach { errorCode ->
            val response = Response.Error(message = "Error occurred", errorCode = errorCode)
            val serialized = messageSerializer.serializeResponse(response)
            val responseJson = (serialized as Result.Success).data

            val deserialized = messageSerializer.deserializeResponse(responseJson)
            val deserializedResponse = (deserialized as Result.Success).data

            assertTrue(deserializedResponse is Response.Error)
            assertEquals(errorCode, (deserializedResponse as Response.Error).errorCode)
        }
    }

    // ========== Package Targeting Tests ==========

    @Test
    fun `integration test - command targets correct package`() {
        val intent = Intent(IPCContract.Broadcast.ACTION_COMMAND).apply {
            setPackage(IPCContract.LOCATION_APP_PACKAGE)
        }

        assertEquals("com.mahdisamavat.location", intent.`package`)
    }

    @Test
    fun `integration test - response targets correct package`() {
        val intent = Intent(IPCContract.Broadcast.ACTION_RESPONSE).apply {
            setPackage(IPCContract.INTERNET_APP_PACKAGE)
        }

        assertEquals("com.mahdisamavat.internet", intent.`package`)
    }

    // ========== Intent Action Tests ==========

    @Test
    fun `integration test - command action is correctly set`() {
        val action = IPCContract.Broadcast.ACTION_COMMAND

        assertEquals("com.mahdisamavat.location.action.COMMAND", action)
        assertTrue(action.contains("location"))
        assertTrue(action.contains("COMMAND"))
    }

    @Test
    fun `integration test - response action is correctly set`() {
        val action = IPCContract.Broadcast.ACTION_RESPONSE

        assertEquals("com.mahdisamavat.internet.action.RESPONSE", action)
        assertTrue(action.contains("internet"))
        assertTrue(action.contains("RESPONSE"))
    }

    // ========== Message Serialization Integration Tests ==========

    @Test
    fun `integration test - complex response data survives serialization round trip`() {
        val response = Response.ServiceState(
            isRunning = true,
            message = "Service started successfully with all features enabled"
        )

        val serialized = messageSerializer.serializeResponse(response)
        assertTrue(serialized is Result.Success)

        val json = (serialized as Result.Success).data
        assertNotNull(json)
        assertTrue(json.isNotEmpty())

        val deserialized = messageSerializer.deserializeResponse(json)
        assertTrue(deserialized is Result.Success)

        val deserializedResponse = (deserialized as Result.Success).data
        assertTrue(deserializedResponse is Response.ServiceState)
        assertEquals(true, (deserializedResponse as Response.ServiceState).isRunning)
        assertEquals(
            "Service started successfully with all features enabled",
            deserializedResponse.message
        )
    }

    @Test
    fun `integration test - multiple sequential commands maintain integrity`() {
        val commands = listOf(
            IPCContract.Broadcast.COMMAND_START_SERVICE,
            IPCContract.Broadcast.COMMAND_GET_ALL_LOCATIONS,
            IPCContract.Broadcast.COMMAND_GET_LATEST_LOCATION,
            IPCContract.Broadcast.COMMAND_STOP_SERVICE
        )

        commands.forEach { command ->
            val intent = Intent(IPCContract.Broadcast.ACTION_COMMAND).apply {
                putExtra(IPCContract.Broadcast.EXTRA_COMMAND_TYPE, command)
            }

            assertEquals(command, intent.getStringExtra(IPCContract.Broadcast.EXTRA_COMMAND_TYPE))
            assertNotNull(intent.action)
        }
    }

    // ========== Timeout Configuration Tests ==========

    @Test
    fun `integration test - command timeout is properly configured`() {
        val timeout = IPCContract.Timeouts.COMMAND_TIMEOUT_MS

        assertTrue(timeout > 0)
        assertEquals(5000L, timeout)
    }

    @Test
    fun `integration test - response timeout is properly configured`() {
        val timeout = IPCContract.Timeouts.RESPONSE_TIMEOUT_MS

        assertTrue(timeout > 0)
        assertEquals(5000L, timeout)
    }

    // ========== Intent Extra Validation Tests ==========

    @Test
    fun `integration test - all required command extras are present`() {
        val intent = Intent(IPCContract.Broadcast.ACTION_COMMAND).apply {
            putExtra(
                IPCContract.Broadcast.EXTRA_COMMAND_TYPE,
                IPCContract.Broadcast.COMMAND_START_SERVICE
            )
            putExtra(IPCContract.Broadcast.EXTRA_TIMESTAMP, System.currentTimeMillis())
        }

        assertTrue(intent.hasExtra(IPCContract.Broadcast.EXTRA_COMMAND_TYPE))
        assertTrue(intent.hasExtra(IPCContract.Broadcast.EXTRA_TIMESTAMP))
        assertNotNull(intent.getStringExtra(IPCContract.Broadcast.EXTRA_COMMAND_TYPE))
    }

    @Test
    fun `integration test - all required response extras are present`() {
        val response = Response.Success("Success")
        val serialized = messageSerializer.serializeResponse(response)

        val intent = Intent(IPCContract.Broadcast.ACTION_RESPONSE).apply {
            putExtra(IPCContract.Broadcast.EXTRA_RESPONSE, (serialized as Result.Success).data)
            putExtra(IPCContract.Broadcast.EXTRA_RESPONSE_TYPE, response.getTypeName())
            putExtra(IPCContract.Broadcast.EXTRA_TIMESTAMP, System.currentTimeMillis())
        }

        assertTrue(intent.hasExtra(IPCContract.Broadcast.EXTRA_RESPONSE))
        assertTrue(intent.hasExtra(IPCContract.Broadcast.EXTRA_RESPONSE_TYPE))
        assertTrue(intent.hasExtra(IPCContract.Broadcast.EXTRA_TIMESTAMP))
    }

    @Test
    fun `integration test - missing command type is detectable`() {
        val intent = Intent(IPCContract.Broadcast.ACTION_COMMAND).apply {
            putExtra(IPCContract.Broadcast.EXTRA_TIMESTAMP, System.currentTimeMillis())
        }

        assertFalse(intent.hasExtra(IPCContract.Broadcast.EXTRA_COMMAND_TYPE))
        val commandType = intent.getStringExtra(IPCContract.Broadcast.EXTRA_COMMAND_TYPE)
        assertTrue(commandType == null || commandType.isEmpty())
    }
}
