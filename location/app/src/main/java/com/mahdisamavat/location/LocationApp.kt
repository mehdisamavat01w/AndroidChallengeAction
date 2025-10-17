package com.mahdisamavat.location

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import com.mahdisamavat.core.logger.Logger
import com.mahdisamavat.location.service.LocationCollectionService
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject


@HiltAndroidApp
class LocationApp : Application() {

    @Inject
    lateinit var logger: Logger

    companion object {
        private const val TAG = "LocationApp"
        private const val PREFS_NAME = "service_prefs"
        private const val KEY_SERVICE_ENABLED = "service_enabled"
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        logger.i(TAG, "Location App initialized")
        logger.i(TAG, "Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        logger.d(TAG, "Package: ${packageName}")

        checkAndRestoreServiceState()
    }

    private fun checkAndRestoreServiceState() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val serviceEnabled = prefs.getBoolean(KEY_SERVICE_ENABLED, false)

        logger.d(TAG, "Service enabled flag: $serviceEnabled")

        if (serviceEnabled) {
            val isRunning = LocationCollectionService.isServiceRunning()
            logger.d(TAG, "Service currently running: $isRunning")

            if (!isRunning) {
                logger.i(TAG, "Service should be running, starting it now")
                try {
                    val serviceIntent = Intent(this, LocationCollectionService::class.java)
                    ContextCompat.startForegroundService(this, serviceIntent)
                } catch (e: Exception) {
                    logger.e(TAG, "Failed to start service on app launch", e)
                }
            }
        }
    }
}
