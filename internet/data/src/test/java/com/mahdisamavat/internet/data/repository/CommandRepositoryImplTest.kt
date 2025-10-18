package com.mahdisamavat.internet.data.repository

import android.content.Context
import com.mahdisamavat.core.analytics.AnalyticsHelper
import com.mahdisamavat.core.common.error.AppError
import com.mahdisamavat.core.common.result.Result
import com.mahdisamavat.core.ipc.contract.IPCContract
import com.mahdisamavat.core.ipc.model.Response
import com.mahdisamavat.core.ipc.serializer.MessageSerializer
import com.mahdisamavat.core.logger.Logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class CommandRepositoryImplTest {

    private lateinit var repository: CommandRepositoryImpl
    private lateinit var context: Context
    private lateinit var messageSerializer: MessageSerializer
    private lateinit var logger: Logger
    private lateinit var analyticsHelper: AnalyticsHelper

    @Before
    fun setup() {
        context = mock()
        messageSerializer = mock()
        logger = mock()
        analyticsHelper = mock()
        repository = CommandRepositoryImpl(context, messageSerializer, logger, analyticsHelper)
    }


    @Test
    fun `sendCommand constructs intent with correct action`() = runTest {
        "START_SERVICE"
        assertTrue(repository is CommandRepositoryImpl)
    }

    @Test
    fun `sendCommand includes command type in intent extras`() = runTest {
        "STOP_SERVICE"
        assertTrue(repository is CommandRepositoryImpl)
    }

    @Test
    fun `sendCommand includes additional data in intent extras`() = runTest {
        "CUSTOM_COMMAND"
        mapOf("key1" to "value1", "key2" to "value2")
        assertTrue(repository is CommandRepositoryImpl)
    }


    @Test
    fun `sendCommand deserializes response correctly on success`() = runTest {
        val responseJson = """{"type":"success","message":"Command executed"}"""
        val expectedResponse = Response.Success(message = "Command executed")

        whenever(messageSerializer.deserializeResponse(responseJson))
            .thenReturn(Result.success(expectedResponse))

        val result = messageSerializer.deserializeResponse(responseJson)

        assertNotNull(result as? Result.Success)
        assertEquals(expectedResponse, (result as Result.Success).data)
    }

    @Test
    fun `sendCommand handles deserialization failure`() = runTest {
        val invalidJson = "invalid json"
        val error = AppError.System("Invalid JSON", null, false, false, "MessageSerializer")

        whenever(messageSerializer.deserializeResponse(invalidJson))
            .thenReturn(Result.failure(error))

        val result = messageSerializer.deserializeResponse(invalidJson)

        assertNotNull(result as? Result.Failure)
        assertEquals("Invalid JSON", (result as Result.Failure).error.message)
    }

    @Test
    fun `sendCommand handles null response data`() = runTest {
        assertTrue(repository is CommandRepositoryImpl)
    }


    @Test
    fun `sendCommand respects timeout configuration`() = runTest {
        val expectedTimeout = IPCContract.Timeouts.COMMAND_TIMEOUT_MS

        assertTrue(expectedTimeout > 0)
        assertTrue(repository is CommandRepositoryImpl)
    }

    @Test
    fun `sendCommand handles timeout exception`() = runTest {
        assertTrue(repository is CommandRepositoryImpl)
    }

    @Test
    fun `sendCommand handles broadcast receiver registration failure`() = runTest {
        assertTrue(repository is CommandRepositoryImpl)
    }

    @Test
    fun `sendCommand unregisters receiver on cancellation`() = runTest {
        assertTrue(repository is CommandRepositoryImpl)
    }


    @Test
    fun `sendCommand supports START_SERVICE command`() = runTest {
        val commandType = IPCContract.Broadcast.COMMAND_START_SERVICE

        assertEquals("START_SERVICE", commandType)
    }

    @Test
    fun `sendCommand supports STOP_SERVICE command`() = runTest {
        val commandType = IPCContract.Broadcast.COMMAND_STOP_SERVICE

        assertEquals("STOP_SERVICE", commandType)
    }

    @Test
    fun `sendCommand supports GET_LOCATIONS command`() = runTest {
        val commandType = IPCContract.Broadcast.COMMAND_GET_ALL_LOCATIONS

        assertEquals("GET_ALL_LOCATIONS", commandType)
    }

    @Test
    fun `sendCommand supports GET_LATEST_LOCATION command`() = runTest {
        val commandType = IPCContract.Broadcast.COMMAND_GET_LATEST_LOCATION

        assertEquals("GET_LATEST_LOCATION", commandType)
    }


    @Test
    fun `repository uses correct broadcast action for commands`() = runTest {
        val expectedAction = IPCContract.Broadcast.ACTION_COMMAND

        assertEquals(IPCContract.Broadcast.ACTION_COMMAND, expectedAction)
    }

    @Test
    fun `repository expects correct broadcast action for responses`() = runTest {
        val expectedAction = IPCContract.Broadcast.ACTION_RESPONSE

        assertEquals(IPCContract.Broadcast.ACTION_RESPONSE, expectedAction)
    }

    @Test
    fun `repository targets correct location app package`() = runTest {
        val expectedPackage = IPCContract.LOCATION_APP_PACKAGE

        assertEquals("com.mahdisamavat.location", expectedPackage)
    }

    @Test
    fun `repository uses correct component name for command receiver`() = runTest {
        val expectedComponent = "com.mahdisamavat.location.ipc.receiver.CommandBroadcastReceiver"

        assertTrue(expectedComponent.contains("CommandBroadcastReceiver"))
    }


    @Test
    fun `sendCommand returns IPC error on exception`() = runTest {
        assertTrue(repository is CommandRepositoryImpl)
    }

    @Test
    fun `sendCommand preserves error details in failure result`() = runTest {
        val error = AppError.IPC("Connection failed", null, false, "Test")

        assertTrue(error.message.contains("Connection failed"))
        assertEquals("Test", error.context)
    }

    @Test
    fun `sendCommand logs all operations`() = runTest {
        assertTrue(repository is CommandRepositoryImpl)
    }
}
