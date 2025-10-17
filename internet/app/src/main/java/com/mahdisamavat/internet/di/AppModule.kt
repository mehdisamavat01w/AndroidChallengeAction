package com.mahdisamavat.internet.di

import com.mahdisamavat.core.ipc.serializer.MessageSerializer
import com.mahdisamavat.core.logger.Logger
import com.mahdisamavat.core.logger.TimberLogger
import com.mahdisamavat.domain.repository.CommandRepository
import com.mahdisamavat.domain.repository.LocationQueryRepository
import com.mahdisamavat.internet.data.repository.CommandRepositoryImpl
import com.mahdisamavat.internet.data.repository.LocationQueryRepositoryImpl
import com.mahdisamavat.internet.presentation.main.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val appModule = module {

    single<Logger> { TimberLogger() }

    single { MessageSerializer(get()) }

    single<CommandRepository> {
        CommandRepositoryImpl(
            context = androidContext(),
            messageSerializer = get(),
            logger = get()
        )
    }

    single<LocationQueryRepository> {
        LocationQueryRepositoryImpl(
            context = androidContext(),
            logger = get()
        )
    }

    viewModel {
        MainViewModel(
            commandRepository = get(),
            locationQueryRepository = get(),
            logger = get()
        )
    }
}
