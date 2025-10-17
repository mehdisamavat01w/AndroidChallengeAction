package com.mahdisamavat.location.ipc.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.core.content.ContextCompat
import com.mahdisamavat.core.logger.Logger
import com.mahdisamavat.location.service.LocationCollectionService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class NetworkChangeReceiver : BroadcastReceiver() {

    @Inject
    lateinit var logger: Logger

    companion object {
        private const val TAG = "NetworkChangeReceiver"
        private const val PREFS_NAME = "service_prefs"
        private const val KEY_SERVICE_ENABLED = "service_enabled"
    }

    override fun onReceive(context: Context, intent: Intent) {
        @Suppress("DEPRECATION")
        if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
            logger.i(TAG, "Network connectivity changed")

            val networkState = getNetworkState(context)
            logger.i(TAG, "Network state: $networkState")

            ensureServiceRunning(context)
        }
    }


    @SuppressLint("MissingPermission")
    private fun getNetworkState(context: Context): String {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            if (network == null) {
                "No network"
            } else {
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                when {
                    capabilities == null -> "No capabilities"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi connected"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular connected"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet connected"
                    else -> "Unknown network type"
                }
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo

            @Suppress("DEPRECATION")
            val connected = networkInfo?.isConnected == true
            if (connected) {
                @Suppress("DEPRECATION")
                val name = networkInfo?.typeName ?: "Unknown"
                "Connected ($name)"
            } else {
                "Disconnected"
            }
        }
    }


    private fun ensureServiceRunning(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val serviceEnabled = prefs.getBoolean(KEY_SERVICE_ENABLED, false)

        if (!serviceEnabled) {
            logger.d(TAG, "Service not enabled, not checking")
            return
        }

        val isRunning = LocationCollectionService.isServiceRunning()

        if (!isRunning) {
            logger.w(TAG, "Service should be running but isn't, restarting...")
            startLocationService(context)
        } else {
            logger.d(TAG, "Service is running correctly")
        }
    }


    private fun startLocationService(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                logger.d(TAG, "Android 12+: Starting service via activity")
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
                logger.i(TAG, "Service start requested via activity")
            } else {
                val serviceIntent = Intent(context, LocationCollectionService::class.java)
                ContextCompat.startForegroundService(context, serviceIntent)
                logger.i(TAG, "Location collection service started successfully")
            }
        } catch (e: Exception) {
            logger.e(TAG, "Failed to start location collection service", e)
        }
    }
}
