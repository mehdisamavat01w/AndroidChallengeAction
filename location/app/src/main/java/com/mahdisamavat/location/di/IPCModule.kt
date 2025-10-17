package com.mahdisamavat.location.di

import com.mahdisamavat.core.ipc.serializer.MessageSerializer
import com.mahdisamavat.core.logger.Logger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object IPCModule {


    @Provides
    @Singleton
    fun provideMessageSerializer(logger: Logger): MessageSerializer {
        return MessageSerializer(logger)
    }
}
