package com.mahdisamavat.core.analytics

import com.google.firebase.crashlytics.FirebaseCrashlytics

fun FirebaseCrashlytics.logNonFatal(throwable: Throwable, context: Map<String, String> = emptyMap()) {
    context.forEach { (key, value) ->
        setCustomKey(key, value)
    }
    recordException(throwable)
}

fun FirebaseCrashlytics.logBreadcrumb(message: String) {
    log(message)
}

fun FirebaseCrashlytics.setScreen(screenName: String) {
    setCustomKey("last_screen", screenName)
}

fun FirebaseCrashlytics.setServiceState(isRunning: Boolean, uptimeSeconds: Long) {
    setCustomKey("service_running", isRunning)
    setCustomKey("service_uptime_seconds", uptimeSeconds)
}

fun FirebaseCrashlytics.setLocationCollectionState(locationsCollected: Int, lastAccuracy: Float) {
    setCustomKey("locations_collected", locationsCollected)
    setCustomKey("last_location_accuracy", lastAccuracy)
}
