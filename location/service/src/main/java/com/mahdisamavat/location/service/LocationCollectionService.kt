package com.mahdisamavat.location.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.mahdisamavat.core.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@AndroidEntryPoint
class LocationCollectionService : Service() {

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var locationRepository: com.mahdisamavat.location.domain.repository.LocationRepository

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)

    private var locationCount = 0
    private var startTime: Long = 0

    companion object {
        private const val TAG = "LocationCollectionService"

        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "location_collection_channel"
        private const val CHANNEL_NAME = "Location Collection"

        private const val COLLECTION_INTERVAL_MS = 60_000L

        @Volatile
        private var isRunning = false

        fun isServiceRunning(): Boolean = isRunning
    }

    override fun onCreate() {
        super.onCreate()
        logger.i(TAG, "Service created")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logger.i(TAG, "Service started (intent=${intent?.action}, flags=$flags, startId=$startId)")
        startTime = System.currentTimeMillis()
        isRunning = true

        startForegroundServiceWithNotification()

        startContinuousLocationCollection()

        return START_STICKY
    }

    private fun startForegroundServiceWithNotification() {
        try {
            val notification = createNotification()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceCompat.startForeground(
                    this,
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }

            logger.i(TAG, "Foreground service started with notification")
        } catch (e: Exception) {
            logger.e(TAG, "Failed to start foreground service", e)
        }
    }

    private fun startContinuousLocationCollection() {
        serviceScope.launch {
            logger.i(TAG, "Starting continuous location collection loop")
            logger.i(TAG, "Collection interval: ${COLLECTION_INTERVAL_MS}ms (1 minute)")

            while (isActive) {
                try {
                    logger.d(TAG, "Attempting to collect location...")
                    val location = collectLocation()

                    if (location != null) {
                        locationCount++

                        val storeResult = locationRepository.storeAndroidLocation(location)

                        when (storeResult) {
                            is com.mahdisamavat.core.common.result.Result.Success -> {
                                logger.i(
                                    TAG,
                                    "Location collected and stored successfully (#$locationCount, id=${storeResult.data}): " +
                                            "lat=${location.latitude}, lng=${location.longitude}, " +
                                            "accuracy=${location.accuracy}m"
                                )
                            }
                            is com.mahdisamavat.core.common.result.Result.Failure -> {
                                logger.e(TAG, "Failed to store location: ${storeResult.error.message}")
                            }
                            else -> {
                                logger.w(TAG, "Unexpected result from store operation")
                            }
                        }

                        updateNotification()
                    } else {
                        logger.w(TAG, "Location collection returned null")
                    }

                } catch (e: SecurityException) {
                    logger.e(TAG, "Location permission denied", e)
                } catch (e: Exception) {
                    logger.e(TAG, "Error collecting location", e)
                }


                logger.d(TAG, "Waiting ${COLLECTION_INTERVAL_MS}ms until next collection...")
                delay(COLLECTION_INTERVAL_MS)
            }

            logger.w(TAG, "Location collection loop terminated")
        }
    }

    private suspend fun collectLocation(): android.location.Location? = suspendCoroutine { continuation ->
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            logger.e(TAG, "Location permission not granted")
            continuation.resume(null)
            return@suspendCoroutine
        }

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        )
            .addOnSuccessListener { location ->
                if (location != null) {
                    logger.d(TAG, "Location received from FusedLocationProvider")
                    continuation.resume(location)
                } else {
                    logger.w(TAG, "Location provider returned null")
                    continuation.resume(null)
                }
            }
            .addOnFailureListener { exception ->
                logger.e(TAG, "Failed to get location from provider", exception)
                continuation.resume(null)
            }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Notification for location collection service"
            setShowBadge(false)
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        logger.d(TAG, "Notification channel created")
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(),
            PendingIntent.FLAG_IMMUTABLE
        )

        val uptime = if (startTime > 0) {
            val minutes = (System.currentTimeMillis() - startTime) / 60_000
            "Uptime: ${minutes}m"
        } else {
            "Starting..."
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Collection Active")
            .setContentText("Collected: $locationCount locations • $uptime")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification() {
        try {
            val notification = createNotification()
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            logger.e(TAG, "Failed to update notification", e)
        }
    }

    override fun onDestroy() {
        logger.i(TAG, "Service destroyed")
        logger.i(TAG, "Total locations collected: $locationCount")
        isRunning = false

        serviceJob.cancel()
        serviceScope.cancel()

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
