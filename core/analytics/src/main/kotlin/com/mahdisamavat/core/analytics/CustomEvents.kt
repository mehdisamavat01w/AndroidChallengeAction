package com.mahdisamavat.core.analytics

fun AnalyticsHelper.logIPCCommunicationLatency(command: String, latencyMs: Long) {
    logEvent(
        AnalyticsEvent(
            type = "ipc_latency",
            extras = listOf(
                AnalyticsEvent.Param("command", command),
                AnalyticsEvent.Param("latency_ms", latencyMs.toString()),
            ),
        ),
    )
}

fun AnalyticsHelper.logDatabaseOperation(operation: String, durationMs: Long, itemCount: Int) {
    logEvent(
        AnalyticsEvent(
            type = "database_operation",
            extras = listOf(
                AnalyticsEvent.Param("operation", operation),
                AnalyticsEvent.Param("duration_ms", durationMs.toString()),
                AnalyticsEvent.Param("item_count", itemCount.toString()),
            ),
        ),
    )
}

fun AnalyticsHelper.logLocationAccuracyWarning(accuracy: Float, threshold: Float) {
    logEvent(
        AnalyticsEvent(
            type = "location_accuracy_warning",
            extras = listOf(
                AnalyticsEvent.Param("accuracy", accuracy.toString()),
                AnalyticsEvent.Param("threshold", threshold.toString()),
            ),
        ),
    )
}

fun AnalyticsHelper.logMemoryPressure(usedMemoryMB: Double, threshold: Double) {
    logEvent(
        AnalyticsEvent(
            type = "memory_pressure",
            extras = listOf(
                AnalyticsEvent.Param("used_memory_mb", usedMemoryMB.toString()),
                AnalyticsEvent.Param("threshold_mb", threshold.toString()),
            ),
        ),
    )
}

fun AnalyticsHelper.logEncryptionOperation(operation: String, durationMs: Long) {
    logEvent(
        AnalyticsEvent(
            type = "encryption_operation",
            extras = listOf(
                AnalyticsEvent.Param("operation", operation),
                AnalyticsEvent.Param("duration_ms", durationMs.toString()),
            ),
        ),
    )
}
