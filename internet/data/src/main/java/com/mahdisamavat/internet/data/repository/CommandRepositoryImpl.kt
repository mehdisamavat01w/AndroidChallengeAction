package com.mahdisamavat.internet.data.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.mahdisamavat.core.common.error.AppError
import com.mahdisamavat.core.common.result.Result
import com.mahdisamavat.core.ipc.contract.IPCContract
import com.mahdisamavat.core.ipc.model.Response
import com.mahdisamavat.core.ipc.serializer.MessageSerializer
import com.mahdisamavat.core.logger.Logger
import com.mahdisamavat.domain.repository.CommandRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume

class CommandRepositoryImpl(
    private val context: Context,
    private val messageSerializer: MessageSerializer,
    private val logger: Logger
) : CommandRepository {

    companion object {
        private const val TAG = "CommandRepository"
    }


    override suspend fun sendCommand(
        commandType: String,
        data: Map<String, String>?
    ): Result<Response> {
        return try {
            logger.d(TAG, "Sending command: $commandType")

            val response = withTimeout(IPCContract.Timeouts.COMMAND_TIMEOUT_MS) {
                suspendCancellableCoroutine { continuation ->
                    val receiver = object : BroadcastReceiver() {
                        override fun onReceive(context: Context, intent: Intent) {
                            logger.d(TAG, "Response received")

                            val responseJson =
                                intent.getStringExtra(IPCContract.Broadcast.EXTRA_RESPONSE)
                            if (responseJson != null) {
                                val result = messageSerializer.deserializeResponse(responseJson)

                                when (result) {
                                    is Result.Success -> {
                                        logger.i(TAG, "Response deserialized successfully")
                                        continuation.resume(Result.success(result.data))
                                    }

                                    is Result.Failure -> {
                                        logger.e(TAG, "Failed to deserialize response")
                                        continuation.resume(Result.failure(result.error))
                                    }

                                    else -> {
                                        continuation.resume(
                                            Result.failure(
                                                AppError.IPC(
                                                    "Unexpected result type",
                                                    null,
                                                    false,
                                                    "CommandRepository"
                                                )
                                            )
                                        )
                                    }
                                }
                            } else {
                                logger.e(TAG, "Response JSON is null")
                                continuation.resume(
                                    Result.failure(
                                        AppError.IPC(
                                            "Response data is missing",
                                            null,
                                            false,
                                            "CommandRepository"
                                        )
                                    )
                                )
                            }

                            try {
                                context.unregisterReceiver(this)
                            } catch (e: Exception) {
                                logger.w(TAG, "Failed to unregister receiver", e)
                            }
                        }
                    }

                    val filter = IntentFilter(IPCContract.Broadcast.ACTION_RESPONSE)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
                    } else {
                        context.registerReceiver(receiver, filter)
                    }

                    continuation.invokeOnCancellation {
                        try {
                            context.unregisterReceiver(receiver)
                        } catch (e: Exception) {
                            logger.w(TAG, "Failed to unregister receiver on cancellation", e)
                        }
                    }

                    val commandIntent = Intent(IPCContract.Broadcast.ACTION_COMMAND).apply {
                        component = android.content.ComponentName(
                            IPCContract.LOCATION_APP_PACKAGE,
                            "com.mahdisamavat.location.ipc.receiver.CommandBroadcastReceiver"
                        )
                        putExtra(IPCContract.Broadcast.EXTRA_COMMAND_TYPE, commandType)
                        putExtra(IPCContract.Broadcast.EXTRA_TIMESTAMP, System.currentTimeMillis())
                        if (data != null) {
                            data.forEach { (key, value) ->
                                putExtra(key, value)
                            }
                        }
                    }

                    context.sendBroadcast(commandIntent)
                    logger.i(TAG, "Command broadcast sent to explicit component: $commandType")
                }
            }

            response

        } catch (e: Exception) {
            logger.e(TAG, "Failed to send command: $commandType", e)
            Result.failure(
                AppError.IPC(
                    "Failed to send command: ${e.message}",
                    e,
                    true,
                    "CommandRepository"
                )
            )
        }
    }
}
