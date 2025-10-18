package com.mahdisamavat.core.analytics

import com.mahdisamavat.core.logger.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StubAnalyticsHelper @Inject constructor(
    private val logger: Logger,
) : AnalyticsHelper {
    
    companion object {
        private const val TAG = "Analytics"
    }
    
    override fun logEvent(event: AnalyticsEvent) {
        logger.i(TAG, "Analytics event: ${event.type} ${formatExtras(event.extras)}")
    }
    
    private fun formatExtras(extras: List<AnalyticsEvent.Param>): String {
        if (extras.isEmpty()) return ""
        return extras.joinToString(prefix = "[", postfix = "]") { "${it.key}=${it.value}" }
    }
}
