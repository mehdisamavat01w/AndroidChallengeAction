package com.mahdisamavat.internet.di

import android.content.Context
import com.mahdisamavat.core.ipc.serializer.MessageSerializer
import com.mahdisamavat.core.logger.Logger
import com.mahdisamavat.domain.repository.CommandRepository
import com.mahdisamavat.domain.repository.LocationQueryRepository
import com.mahdisamavat.internet.presentation.main.MainViewModel
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.koinApplication
import org.mockito.kotlin.mock

class KoinModuleVerificationTest {

    private val mockContext: Context = mock()

    @Test
    fun `appModule provides all dependencies`() {
        val koinApp = koinApplication {
            androidContext(mockContext)
            modules(appModule)
        }

        assertNotNull(koinApp.koin.get<Logger>())
        assertNotNull(koinApp.koin.get<MessageSerializer>())
        assertNotNull(koinApp.koin.get<CommandRepository>())
        assertNotNull(koinApp.koin.get<LocationQueryRepository>())
        assertNotNull(koinApp.koin.get<MainViewModel>())
    }

    @Test
    fun `repositories are singleton instances`() {
        val koinApp = koinApplication {
            androidContext(mockContext)
            modules(appModule)
        }

        val repo1 = koinApp.koin.get<CommandRepository>()
        val repo2 = koinApp.koin.get<CommandRepository>()

        assertTrue(repo1 === repo2)
    }
}
