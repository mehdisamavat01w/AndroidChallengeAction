package com.mahdisamavat.core.model

import kotlinx.serialization.Serializable

/**
 * State of the location collection service.
 */
@Serializable
data class ServiceState(
    val isRunning: Boolean,
    val startTime: Long? = null,
    val locationCount: Int = 0,
    val lastCollectionTime: Long? = null,
    val errorCount: Int = 0
) {
    
    /**
     * Get service uptime in milliseconds
     */
    fun getUptime(): Long? {
        return if (isRunning && startTime != null) {
            System.currentTimeMillis() - startTime
        } else {
            null
        }
    }
    
    /**
     * Get uptime formatted as string
     */
    fun getUptimeString(): String {
        val uptime = getUptime() ?: return "N/A"
        
        val seconds = uptime / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        return when {
            days > 0 -> "${days}d ${hours % 24}h"
            hours > 0 -> "${hours}h ${minutes % 60}m"
            minutes > 0 -> "${minutes}m ${seconds % 60}s"
            else -> "${seconds}s"
        }
    }
    
    /**
     * Get time since last collection in milliseconds
     */
    fun getTimeSinceLastCollection(): Long? {
        return if (lastCollectionTime != null) {
            System.currentTimeMillis() - lastCollectionTime
        } else {
            null
        }
    }
    
    /**
     * Check if service is healthy (running and collecting)
     */
    fun isHealthy(): Boolean {
        if (!isRunning) return false
        val timeSince = getTimeSinceLastCollection()
        return timeSince == null || timeSince < 120_000
    }
}
