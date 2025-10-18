package com.mahdisamavat.core.analytics

data class AnalyticsEvent(
    val type: String,
    val extras: List<Param> = emptyList(),
) {
    object Types {
        const val SCREEN_VIEW = "screen_view"
        const val SERVICE_STARTED = "service_started"
        const val SERVICE_STOPPED = "service_stopped"
        const val LOCATION_COLLECTED = "location_collected"
        const val COMMAND_SENT = "command_sent"
        const val DATA_QUERY = "data_query"
        const val ERROR_OCCURRED = "error_occurred"
    }

    data class Param(val key: String, val value: String)

    object ParamKeys {
        const val SCREEN_NAME = "screen_name"
        const val COMMAND_TYPE = "command_type"
        const val ERROR_MESSAGE = "error_message"
        const val ERROR_TYPE = "error_type"
        const val LOCATION_ACCURACY = "location_accuracy"
        const val LOCATION_PROVIDER = "location_provider"
        const val SERVICE_TYPE = "service_type"
        const val DATA_COUNT = "data_count"
    }
}
