package com.mahdisamavat.location.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LocationCollectionService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)
    private var locationCount = 0
    private var startTime: Long = 0

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "location_collection_channel"
        private const val CHANNEL_NAME = "Location Collection"
        private const val COLLECTION_INTERVAL_MS = 60_000L // 60 seconds
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startTime = System.currentTimeMillis()
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
        } catch (e: Exception) {
            // Handle error silently
        }
    }

    private fun startContinuousLocationCollection() {
        serviceScope.launch {
            while (isActive) {
                try {
                    val location = collectLocation()
                    if (location != null) {
                        locationCount++
                        // Store location (e.g., in-memory or local storage)
                        updateNotification()
                    }
                } catch (e: SecurityException) {
                    // Permission denied
                } catch (e: Exception) {
                    // Other errors
                }
                delay(COLLECTION_INTERVAL_MS)
            }
        }
    }

    private suspend fun collectLocation(): Location? = suspendCoroutine { continuation ->
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            continuation.resume(null)
            return@suspendCoroutine
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                continuation.resume(location)
            }
            .addOnFailureListener {
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
            // Handle error silently
        }
    }

    override fun onDestroy() {
        serviceJob.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}