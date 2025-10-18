package com.mahdisamavat.core.analytics

object PerformanceThresholds {
    const val LOCATION_ACCURACY_WARNING_METERS = 50.0f
    const val LOCATION_ACCURACY_CRITICAL_METERS = 100.0f
    
    const val IPC_LATENCY_WARNING_MS = 1000L
    const val DATABASE_QUERY_WARNING_MS = 100L
    const val MEMORY_WARNING_MB = 256.0
    const val ENCRYPTION_WARNING_MS = 50L
}

class PerformanceMonitor(
    private val analyticsHelper: AnalyticsHelper,
) {
    fun checkLocationAccuracy(accuracy: Float) {
        when {
            accuracy > PerformanceThresholds.LOCATION_ACCURACY_CRITICAL_METERS -> {
                analyticsHelper.logLocationAccuracyWarning(
                    accuracy,
                    PerformanceThresholds.LOCATION_ACCURACY_CRITICAL_METERS
                )
            }
            accuracy > PerformanceThresholds.LOCATION_ACCURACY_WARNING_METERS -> {
                analyticsHelper.logLocationAccuracyWarning(
                    accuracy,
                    PerformanceThresholds.LOCATION_ACCURACY_WARNING_METERS
                )
            }
        }
    }
    
    fun checkIPCLatency(command: String, latencyMs: Long) {
        if (latencyMs > PerformanceThresholds.IPC_LATENCY_WARNING_MS) {
            analyticsHelper.logIPCCommunicationLatency(command, latencyMs)
        }
    }
    
    fun checkDatabasePerformance(operation: String, durationMs: Long, itemCount: Int) {
        if (durationMs > PerformanceThresholds.DATABASE_QUERY_WARNING_MS) {
            analyticsHelper.logDatabaseOperation(operation, durationMs, itemCount)
        }
    }
    
    fun checkMemoryUsage(usedMemoryMB: Double) {
        if (usedMemoryMB > PerformanceThresholds.MEMORY_WARNING_MB) {
            analyticsHelper.logMemoryPressure(usedMemoryMB, PerformanceThresholds.MEMORY_WARNING_MB)
        }
    }
    
    fun checkEncryptionPerformance(operation: String, durationMs: Long) {
        if (durationMs > PerformanceThresholds.ENCRYPTION_WARNING_MS) {
            analyticsHelper.logEncryptionOperation(operation, durationMs)
        }
    }
}
