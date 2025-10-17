package com.mahdisamavat.location

import android.app.Application
import com.mahdisamavat.core.logger.Logger
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject


@HiltAndroidApp
class LocationApp : Application() {

    @Inject
    lateinit var logger: Logger

    companion object {
        private const val TAG = "LocationApp"
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        logger.i(TAG, "Location App initialized")
        logger.i(TAG, "Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        logger.d(TAG, "Package: ${packageName}")

    }
}
