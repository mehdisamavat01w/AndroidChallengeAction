package com.mahdisamavat.core.ipc.serializer

import com.mahdisamavat.core.common.result.Result
import com.mahdisamavat.core.ipc.contract.IPCContract
import com.mahdisamavat.core.ipc.model.Response
import com.mahdisamavat.core.logger.Logger
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MessageSerializerTest {

    private lateinit var serializer: MessageSerializer
    private lateinit var logger: Logger

    @Before
    fun setup() {
        logger = mockk(relaxed = true)
        serializer = MessageSerializer(logger)
    }

    @Test
    fun `serialize and deserialize ServiceState response`() {
        val response = Response.ServiceState(
            isRunning = true,
            message = "Service started"
        )

        val result = serializer.serializeResponse(response)

        assertTrue(result is Result.Success)
        val json = (result as Result.Success).data

        val deserializeResult = serializer.deserializeResponse(json)
        assertTrue(deserializeResult is Result.Success)

        val deserialized = (deserializeResult as Result.Success).data
        assertTrue(deserialized is Response.ServiceState)
        assertEquals(true, (deserialized as Response.ServiceState).isRunning)
        assertEquals("Service started", deserialized.message)
    }

    @Test
    fun `serialize and deserialize Error response`() {
        val response = Response.Error(
            message = "Something went wrong",
            errorCode = IPCContract.ErrorCodes.SERVICE_NOT_AVAILABLE
        )

        val result = serializer.serializeResponse(response)

        assertTrue(result is Result.Success)
        val json = (result as Result.Success).data

        val deserializeResult = serializer.deserializeResponse(json)
        assertTrue(deserializeResult is Result.Success)

        val deserialized = (deserializeResult as Result.Success).data
        assertTrue(deserialized is Response.Error)
        assertEquals("Something went wrong", (deserialized as Response.Error).message)
        assertEquals(IPCContract.ErrorCodes.SERVICE_NOT_AVAILABLE, deserialized.errorCode)
    }

    @Test
    fun `serialize and deserialize Success response`() {
        val response = Response.Success("Operation completed")

        val result = serializer.serializeResponse(response)

        assertTrue(result is Result.Success)
        val json = (result as Result.Success).data

        val deserializeResult = serializer.deserializeResponse(json)
        assertTrue(deserializeResult is Result.Success)

        val deserialized = (deserializeResult as Result.Success).data
        assertTrue(deserialized is Response.Success)
        assertEquals("Operation completed", (deserialized as Response.Success).message)
    }

    @Test
    fun `serialize response preserves timestamp`() {
        System.currentTimeMillis()
        val response = Response.Success("Test message")

        val result = serializer.serializeResponse(response)

        assertTrue(result is Result.Success)
        val json = (result as Result.Success).data
        assertTrue(json.contains("timestamp"))
    }

    @Test
    fun `serialize Error with error code`() {
        val response = Response.Error(
            message = "Test error",
            errorCode = IPCContract.ErrorCodes.INVALID_COMMAND
        )

        val result = serializer.serializeResponse(response)

        assertTrue(result is Result.Success)
        val json = (result as Result.Success).data

        val deserializeResult = serializer.deserializeResponse(json)
        assertTrue(deserializeResult is Result.Success)

        val deserialized = (deserializeResult as Result.Success).data
        assertTrue(deserialized is Response.Error)
        assertEquals(
            IPCContract.ErrorCodes.INVALID_COMMAND,
            (deserialized as Response.Error).errorCode
        )
    }

    @Test
    fun `serialize Error without error code`() {
        val response = Response.Error(
            message = "Test error",
            errorCode = null
        )

        val result = serializer.serializeResponse(response)

        assertTrue(result is Result.Success)
        val json = (result as Result.Success).data

        val deserializeResult = serializer.deserializeResponse(json)
        assertTrue(deserializeResult is Result.Success)

        val deserialized = (deserializeResult as Result.Success).data
        assertTrue(deserialized is Response.Error)
        assertEquals(null, (deserialized as Response.Error).errorCode)
    }

    @Test
    fun `deserialize invalid JSON returns failure`() {
        val invalidJson = "{invalid json"

        val result = serializer.deserializeResponse(invalidJson)

        assertTrue(result is Result.Failure)
    }

    @Test
    fun `deserialize empty string returns failure`() {
        val result = serializer.deserializeResponse("")

        assertTrue(result is Result.Failure)
    }

    @Test
    fun `getTypeName returns correct type for responses`() {
        val successResponse = Response.Success("test")
        val errorResponse = Response.Error("error", null)
        val serviceStateResponse = Response.ServiceState(true, "running")

        assertEquals("SUCCESS", successResponse.getTypeName())
        assertEquals("ERROR", errorResponse.getTypeName())
        assertEquals("SERVICE_STATE", serviceStateResponse.getTypeName())
    }
}
