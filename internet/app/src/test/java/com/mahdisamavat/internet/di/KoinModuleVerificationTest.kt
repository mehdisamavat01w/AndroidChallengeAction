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

/**
 * Simplified Koin module verification tests.
 * Ensures all dependencies in appModule are properly configured.
 */
class KoinModuleVerificationTest {

    private val mockContext: Context = mock()

    @Test
    fun `appModule can be loaded without errors`() {
        val koinApp = koinApplication {
            androidContext(mockContext)
            modules(appModule)
        }

        assertNotNull(koinApp.koin)
    }

    @Test
    fun `appModule provides Logger`() {
        val koinApp = koinApplication {
            androidContext(mockContext)
            modules(appModule)
        }

        val logger = koinApp.koin.get<Logger>()
        assertNotNull(logger)
    }

    @Test
    fun `appModule provides MessageSerializer`() {
        val koinApp = koinApplication {
            androidContext(mockContext)
            modules(appModule)
        }

        val serializer = koinApp.koin.get<MessageSerializer>()
        assertNotNull(serializer)
    }

    @Test
    fun `appModule provides CommandRepository`() {
        val koinApp = koinApplication {
            androidContext(mockContext)
            modules(appModule)
        }

        val repository = koinApp.koin.get<CommandRepository>()
        assertNotNull(repository)
    }

    @Test
    fun `appModule provides LocationQueryRepository`() {
        val koinApp = koinApplication {
            androidContext(mockContext)
            modules(appModule)
        }

        val repository = koinApp.koin.get<LocationQueryRepository>()
        assertNotNull(repository)
    }

    @Test
    fun `appModule provides MainViewModel`() {
        val koinApp = koinApplication {
            androidContext(mockContext)
            modules(appModule)
        }

        val viewModel = koinApp.koin.get<MainViewModel>()
        assertNotNull(viewModel)
    }

    @Test
    fun `Logger is singleton instance`() {
        val koinApp = koinApplication {
            androidContext(mockContext)
            modules(appModule)
        }

        val logger1 = koinApp.koin.get<Logger>()
        val logger2 = koinApp.koin.get<Logger>()

        assertTrue(logger1 === logger2)
    }

    @Test
    fun `CommandRepository is singleton instance`() {
        val koinApp = koinApplication {
            androidContext(mockContext)
            modules(appModule)
        }

        val repo1 = koinApp.koin.get<CommandRepository>()
        val repo2 = koinApp.koin.get<CommandRepository>()

        assertTrue(repo1 === repo2)
    }

    @Test
    fun `MainViewModel creates new instances`() {
        val koinApp = koinApplication {
            androidContext(mockContext)
            modules(appModule)
        }

        val vm1 = koinApp.koin.get<MainViewModel>()
        val vm2 = koinApp.koin.get<MainViewModel>()

        // ViewModels are scoped, so different instances expected
        assertTrue(vm1 !== vm2)
    }

    @Test
    fun `all core dependencies can be resolved`() {
        val koinApp = koinApplication {
            androidContext(mockContext)
            modules(appModule)
        }

        // Get all dependencies without errors
        assertNotNull(koinApp.koin.get<Logger>())
        assertNotNull(koinApp.koin.get<MessageSerializer>())
        assertNotNull(koinApp.koin.get<CommandRepository>())
        assertNotNull(koinApp.koin.get<LocationQueryRepository>())
        assertNotNull(koinApp.koin.get<MainViewModel>())
    }

    @Test
    fun `repository implementations are correct types`() {
        val koinApp = koinApplication {
            androidContext(mockContext)
            modules(appModule)
        }

        val commandRepo = koinApp.koin.get<CommandRepository>()
        val queryRepo = koinApp.koin.get<LocationQueryRepository>()

        assertTrue(commandRepo.javaClass.simpleName.contains("CommandRepositoryImpl"))
        assertTrue(queryRepo.javaClass.simpleName.contains("LocationQueryRepositoryImpl"))
    }
}
