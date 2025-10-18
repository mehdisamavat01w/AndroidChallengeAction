package com.mahdisamavat.internet.di

import com.mahdisamavat.core.analytics.AnalyticsHelper
import com.mahdisamavat.core.analytics.NoOpAnalyticsHelper
import com.mahdisamavat.core.ipc.serializer.MessageSerializer
import com.mahdisamavat.core.logger.Logger
import com.mahdisamavat.core.logger.TimberLogger
import com.mahdisamavat.domain.repository.CommandRepository
import com.mahdisamavat.domain.repository.LocationQueryRepository
import com.mahdisamavat.internet.data.repository.CommandRepositoryImpl
import com.mahdisamavat.internet.data.repository.LocationQueryRepositoryImpl
import com.mahdisamavat.internet.presentation.main.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module


val appModule = module {

    single<Logger> { TimberLogger() }

    single<AnalyticsHelper> { NoOpAnalyticsHelper() }

    single { MessageSerializer(get()) }

    single<CommandRepository> {
        CommandRepositoryImpl(
            context = androidContext(),
            messageSerializer = get(),
            logger = get(),
            analyticsHelper = get()
        )
    }

    single<LocationQueryRepository> {
        LocationQueryRepositoryImpl(
            context = androidContext(),
            logger = get(),
            analyticsHelper = get()
        )
    }

    viewModel {
        MainViewModel(
            commandRepository = get(),
            locationQueryRepository = get(),
            logger = get(),
            analyticsHelper = get()
        )
    }
}
