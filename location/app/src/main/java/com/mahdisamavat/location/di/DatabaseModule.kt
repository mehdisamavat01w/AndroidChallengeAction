package com.mahdisamavat.location.di

import android.content.Context
import com.mahdisamavat.core.logger.Logger
import com.mahdisamavat.core.security.KeyStoreManager
import com.mahdisamavat.location.data.AppDatabase
import com.mahdisamavat.location.data.DatabaseBuilder
import com.mahdisamavat.location.data.dao.LocationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {


    @Provides
    @Singleton
    fun provideKeyStoreManager(logger: Logger): KeyStoreManager {
        return KeyStoreManager(logger)
    }


    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        keyStoreManager: KeyStoreManager,
        logger: Logger
    ): AppDatabase {
        return DatabaseBuilder.build(context, keyStoreManager, logger)
    }


    @Provides
    @Singleton
    fun provideLocationDao(database: AppDatabase): LocationDao {
        return database.locationDao()
    }
}
