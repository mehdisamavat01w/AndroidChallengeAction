package com.mahdisamavat.location.ipc.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.mahdisamavat.core.logger.Logger
import com.mahdisamavat.location.service.LocationCollectionService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var logger: Logger

    companion object {
        private const val TAG = "BootCompletedReceiver"
        private const val PREFS_NAME = "service_prefs"
        private const val KEY_SERVICE_ENABLED = "service_enabled"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                logger.i(TAG, "Device boot completed")
                handleBootCompleted(context)
            }

            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                logger.i(TAG, "App package replaced (updated)")
                handleBootCompleted(context)
            }

            else -> {
                logger.w(TAG, "Received unknown action: ${intent.action}")
            }
        }
    }


    private fun handleBootCompleted(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val serviceEnabled = prefs.getBoolean(KEY_SERVICE_ENABLED, false)

        if (serviceEnabled) {
            logger.i(TAG, "Service was previously enabled, restarting...")
            startLocationService(context)
        } else {
            logger.i(TAG, "Service was not enabled, not starting")
        }
    }


    private fun startLocationService(context: Context) {
        try {
            if (LocationCollectionService.isServiceRunning()) {
                logger.i(TAG, "Service already running")
                return
            }

            val serviceIntent = Intent(context, LocationCollectionService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
            logger.i(TAG, "Location collection service started after boot")
        } catch (e: Exception) {
            logger.e(TAG, "Failed to start service, will retry via activity", e)
            try {
                val activityIntent = Intent().apply {
                    setClassName(
                        "com.mahdisamavat.location",
                        "com.mahdisamavat.location.MainActivity"
                    )
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra("START_SERVICE", true)
                }
                context.startActivity(activityIntent)
                logger.i(TAG, "Service start requested via activity fallback")
            } catch (e2: Exception) {
                logger.e(TAG, "Failed to start service via activity", e2)
            }
        }
    }
}
