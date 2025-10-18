package com.mahdisamavat.internet

import android.app.Application
import com.mahdisamavat.core.logger.Logger
import com.mahdisamavat.internet.di.analyticsModule
import com.mahdisamavat.internet.di.appModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber


class InternetApp : Application() {

    private val logger: Logger by inject()

    companion object {
        private const val TAG = "InternetApp"
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }


        startKoin {
            androidLogger()
            androidContext(this@InternetApp)
            modules(analyticsModule, appModule)
        }

        logger.i(TAG, "Internet App initialized")
        logger.i(TAG, "Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        logger.d(TAG, "Package: ${packageName}")
    }
}
