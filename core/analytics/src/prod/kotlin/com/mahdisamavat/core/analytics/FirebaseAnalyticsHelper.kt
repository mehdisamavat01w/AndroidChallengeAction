package com.mahdisamavat.core.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAnalyticsHelper @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val crashlytics: FirebaseCrashlytics,
) : AnalyticsHelper {

    override fun logEvent(event: AnalyticsEvent) {
        firebaseAnalytics.logEvent(event.type) {
            for (extra in event.extras) {
                param(
                    key = extra.key.take(40),
                    value = extra.value.take(100),
                )
            }
        }
    }

    fun logNonFatal(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    fun setUserId(userId: String) {
        firebaseAnalytics.setUserId(userId)
        crashlytics.setUserId(userId)
    }

    fun setUserProperty(key: String, value: String) {
        firebaseAnalytics.setUserProperty(key, value)
        crashlytics.setCustomKey(key, value)
    }
}
