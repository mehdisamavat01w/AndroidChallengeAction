package com.mahdisamavat.location.ipc.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.mahdisamavat.core.ipc.contract.IPCContract
import com.mahdisamavat.core.ipc.model.Response
import com.mahdisamavat.core.ipc.serializer.MessageSerializer
import com.mahdisamavat.core.logger.Logger
import com.mahdisamavat.location.service.LocationCollectionService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class CommandBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var messageSerializer: MessageSerializer

    companion object {
        private const val TAG = "CommandBroadcastReceiver"
        private const val PREFS_NAME = "service_prefs"
        private const val KEY_SERVICE_ENABLED = "service_enabled"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != IPCContract.Broadcast.ACTION_COMMAND) {
            logger.w(TAG, "Received unknown action: ${intent.action}")
            return
        }

        logger.i(TAG, "Command received from Internet App")

        val commandType = intent.getStringExtra(IPCContract.Broadcast.EXTRA_COMMAND_TYPE)
        logger.d(TAG, "Command type: $commandType")

        if (commandType == null) {
            logger.e(TAG, "Command type is null")
            sendResponse(
                context,
                Response.Error("Missing command type", IPCContract.ErrorCodes.INVALID_COMMAND)
            )
            return
        }

        handleCommand(context, commandType)
    }


    private fun handleCommand(context: Context, commandType: String) {
        when (commandType) {
            IPCContract.Broadcast.COMMAND_START_SERVICE -> {
                logger.i(TAG, "Handling START_SERVICE command")
                handleStartService(context)
            }

            IPCContract.Broadcast.COMMAND_STOP_SERVICE -> {
                logger.i(TAG, "Handling STOP_SERVICE command")
                handleStopService(context)
            }

            IPCContract.Broadcast.COMMAND_GET_ALL_LOCATIONS -> {
                logger.i(TAG, "GET_ALL_LOCATIONS command received (use ContentProvider)")
                sendResponse(context, Response.Success("Use ContentProvider to query locations"))
            }

            IPCContract.Broadcast.COMMAND_GET_LATEST_LOCATION -> {
                logger.i(TAG, "GET_LATEST_LOCATION command received (use ContentProvider)")
                sendResponse(
                    context,
                    Response.Success("Use ContentProvider to query latest location")
                )
            }

            else -> {
                logger.w(TAG, "Unknown command type: $commandType")
                sendResponse(
                    context,
                    Response.Error("Unknown command", IPCContract.ErrorCodes.INVALID_COMMAND)
                )
            }
        }
    }


    private fun handleStartService(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_SERVICE_ENABLED, true).apply()
            logger.i(TAG, "Service enabled flag set to true")

            if (LocationCollectionService.isServiceRunning()) {
                logger.i(TAG, "Service already running")
                sendResponse(
                    context,
                    Response.ServiceState(
                        isRunning = true,
                        message = "Service is already running"
                    )
                )
                return
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                logger.d(TAG, "Android 12+: Using activity trampoline to start service")
                val activityIntent = Intent().apply {
                    setClassName(
                        "com.mahdisamavat.location",
                        "com.mahdisamavat.location.MainActivity"
                    )
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    putExtra("START_SERVICE", true)
                }
                context.startActivity(activityIntent)

                sendResponse(
                    context,
                    Response.ServiceState(
                        isRunning = true,
                        message = "Service start requested"
                    )
                )
            } else {
                val serviceIntent = Intent(context, LocationCollectionService::class.java)
                ContextCompat.startForegroundService(context, serviceIntent)
                logger.i(TAG, "Service started successfully")

                sendResponse(
                    context,
                    Response.ServiceState(
                        isRunning = true,
                        message = "Location collection service started"
                    )
                )
            }
        } catch (e: Exception) {
            logger.e(TAG, "Failed to start service", e)
            sendResponse(
                context,
                Response.Error(
                    "Failed to start service: ${e.message}",
                    IPCContract.ErrorCodes.SERVICE_NOT_AVAILABLE
                )
            )
        }
    }


    private fun handleStopService(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_SERVICE_ENABLED, false).apply()
            logger.i(TAG, "Service enabled flag set to false")

            if (!LocationCollectionService.isServiceRunning()) {
                logger.i(TAG, "Service not running, nothing to stop")
                sendResponse(
                    context,
                    Response.ServiceState(
                        isRunning = false,
                        message = "Service is not running"
                    )
                )
                return
            }

            val serviceIntent = Intent(context, LocationCollectionService::class.java)
            val stopped = context.stopService(serviceIntent)

            logger.i(TAG, "Stop service called, result: $stopped")

            sendResponse(
                context,
                Response.ServiceState(
                    isRunning = false,
                    message = "Location collection service stopped"
                )
            )
        } catch (e: Exception) {
            logger.e(TAG, "Failed to stop service", e)
            sendResponse(
                context,
                Response.Error(
                    "Failed to stop service: ${e.message}",
                    IPCContract.ErrorCodes.SERVICE_NOT_AVAILABLE
                )
            )
        }
    }


    private fun sendResponse(context: Context, response: Response) {
        try {
            logger.d(TAG, "Sending response: ${response.getTypeName()}")

            val result = messageSerializer.serializeResponse(response)

            if (result is com.mahdisamavat.core.common.result.Result.Success) {
                val responseJson = result.data

                val responseIntent = Intent(IPCContract.Broadcast.ACTION_RESPONSE).apply {
                    setPackage(IPCContract.INTERNET_APP_PACKAGE)
                    putExtra(IPCContract.Broadcast.EXTRA_RESPONSE, responseJson)
                    putExtra(IPCContract.Broadcast.EXTRA_RESPONSE_TYPE, response.getTypeName())
                    putExtra(IPCContract.Broadcast.EXTRA_TIMESTAMP, System.currentTimeMillis())
                }

                context.sendBroadcast(responseIntent)

                logger.i(TAG, "Response sent successfully")
            } else {
                logger.e(TAG, "Failed to serialize response")
            }
        } catch (e: Exception) {
            logger.e(TAG, "Failed to send response", e)
        }
    }
}
