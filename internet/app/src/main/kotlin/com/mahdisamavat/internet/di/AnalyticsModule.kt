package com.mahdisamavat.internet.di

import com.mahdisamavat.core.analytics.AnalyticsHelper
import com.mahdisamavat.core.analytics.StubAnalyticsHelper
import org.koin.dsl.module

val analyticsModule = module {
    single<AnalyticsHelper> { StubAnalyticsHelper(get()) }
}
