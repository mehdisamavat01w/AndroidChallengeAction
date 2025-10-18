package com.mahdisamavat.core.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

fun AnalyticsHelper.logScreenView(screenName: String) {
    logEvent(
        AnalyticsEvent(
            type = AnalyticsEvent.Types.SCREEN_VIEW,
            extras = listOf(
                AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.SCREEN_NAME, screenName),
            ),
        ),
    )
}

fun AnalyticsHelper.logServiceStarted(serviceType: String) {
    logEvent(
        AnalyticsEvent(
            type = AnalyticsEvent.Types.SERVICE_STARTED,
            extras = listOf(
                AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.SERVICE_TYPE, serviceType),
            ),
        ),
    )
}

fun AnalyticsHelper.logServiceStopped(serviceType: String) {
    logEvent(
        AnalyticsEvent(
            type = AnalyticsEvent.Types.SERVICE_STOPPED,
            extras = listOf(
                AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.SERVICE_TYPE, serviceType),
            ),
        ),
    )
}

fun AnalyticsHelper.logLocationCollected(accuracy: Float, provider: String) {
    logEvent(
        AnalyticsEvent(
            type = AnalyticsEvent.Types.LOCATION_COLLECTED,
            extras = listOf(
                AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.LOCATION_ACCURACY, accuracy.toString()),
                AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.LOCATION_PROVIDER, provider),
            ),
        ),
    )
}

fun AnalyticsHelper.logCommandSent(commandType: String) {
    logEvent(
        AnalyticsEvent(
            type = AnalyticsEvent.Types.COMMAND_SENT,
            extras = listOf(
                AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.COMMAND_TYPE, commandType),
            ),
        ),
    )
}

fun AnalyticsHelper.logDataQuery(dataCount: Int) {
    logEvent(
        AnalyticsEvent(
            type = AnalyticsEvent.Types.DATA_QUERY,
            extras = listOf(
                AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.DATA_COUNT, dataCount.toString()),
            ),
        ),
    )
}

fun AnalyticsHelper.logError(error: Throwable) {
    logEvent(
        AnalyticsEvent(
            type = AnalyticsEvent.Types.ERROR_OCCURRED,
            extras = listOf(
                AnalyticsEvent.Param(
                    AnalyticsEvent.ParamKeys.ERROR_TYPE,
                    error.javaClass.simpleName,
                ),
                AnalyticsEvent.Param(
                    AnalyticsEvent.ParamKeys.ERROR_MESSAGE,
                    error.message?.take(100) ?: "Unknown error",
                ),
            ),
        ),
    )
}

@Composable
fun TrackScreenViewEvent(
    screenName: String,
    analyticsHelper: AnalyticsHelper = LocalAnalyticsHelper.current,
) = DisposableEffect(Unit) {
    analyticsHelper.logScreenView(screenName)
    onDispose {}
}
