package com.mahdisamavat.location.presentation

import app.cash.turbine.test
import com.mahdisamavat.core.common.error.AppError
import com.mahdisamavat.core.common.result.Result
import com.mahdisamavat.core.logger.Logger
import com.mahdisamavat.core.model.Location
import com.mahdisamavat.location.domain.usecase.GetAllLocationsUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LocationViewModelTest {

    private lateinit var viewModel: LocationViewModel
    private lateinit var getAllLocationsUseCase: GetAllLocationsUseCase
    private lateinit var logger: Logger
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getAllLocationsUseCase = mockk()
        logger = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty locations`() = runTest {
        every { getAllLocationsUseCase.invoke() } returns flowOf(Result.success(emptyList()))
        viewModel = LocationViewModel(getAllLocationsUseCase, logger)

        val state = viewModel.state.value

        assertTrue(state.locations.isEmpty())
        assertEquals(0, state.locationCount)
    }

    @Test
    fun `viewModel loads locations on init success`() = runTest {
        val mockLocations = listOf(
            createLocation(1, 37.7749, -122.4194),
            createLocation(2, 34.0522, -118.2437)
        )
        every { getAllLocationsUseCase.invoke() } returns flowOf(Result.success(mockLocations))

        viewModel = LocationViewModel(getAllLocationsUseCase, logger)

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(2, state.locations.size)
        assertEquals(37.7749, state.locations[0].latitude, 0.0001)
        assertEquals(2, state.locationCount)
        assertNull(state.error)
    }

    @Test
    fun `viewModel handles error on init failure`() = runTest {
        val error = AppError.Data("Failed to load", null, false, "Test")
        every { getAllLocationsUseCase.invoke() } returns flowOf(Result.failure(error))

        viewModel = LocationViewModel(getAllLocationsUseCase, logger)

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Failed to load", state.error)
        assertTrue(state.locations.isEmpty())
    }

    @Test
    fun `handleIntent LoadLocations refreshes data`() = runTest {
        val initialLocations = listOf(createLocation(1, 37.7749, -122.4194))
        val updatedLocations = listOf(
            createLocation(1, 37.7749, -122.4194),
            createLocation(2, 34.0522, -118.2437)
        )
        every { getAllLocationsUseCase.invoke() } returns flowOf(
            Result.success(initialLocations)
        ) andThen flowOf(Result.success(updatedLocations))

        viewModel = LocationViewModel(getAllLocationsUseCase, logger)
        assertEquals(1, viewModel.state.value.locationCount)

        viewModel.handleIntent(LocationIntent.LoadLocations)

        assertEquals(2, viewModel.state.value.locationCount)
    }

    @Test
    fun `handleIntent ClearError clears error state`() = runTest {
        val error = AppError.Data("Test error", null, false, "Test")
        every { getAllLocationsUseCase.invoke() } returns flowOf(Result.failure(error))

        viewModel = LocationViewModel(getAllLocationsUseCase, logger)
        assertEquals("Test error", viewModel.state.value.error)

        viewModel.handleIntent(LocationIntent.ClearError)

        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `handleIntent StartService emits effect`() = runTest {
        every { getAllLocationsUseCase.invoke() } returns flowOf(Result.success(emptyList()))
        viewModel = LocationViewModel(getAllLocationsUseCase, logger)

        viewModel.effect.test {
            viewModel.handleIntent(LocationIntent.StartService)

            val effect = awaitItem()
            assertTrue(effect is LocationEffect.ShowMessage)
            assertEquals(
                "Use Internet App to start service",
                (effect as LocationEffect.ShowMessage).message
            )
        }
    }

    @Test
    fun `handleIntent StopService emits effect`() = runTest {
        every { getAllLocationsUseCase.invoke() } returns flowOf(Result.success(emptyList()))
        viewModel = LocationViewModel(getAllLocationsUseCase, logger)

        viewModel.effect.test {
            viewModel.handleIntent(LocationIntent.StopService)

            val effect = awaitItem()
            assertTrue(effect is LocationEffect.ShowMessage)
            assertEquals(
                "Use Internet App to stop service",
                (effect as LocationEffect.ShowMessage).message
            )
        }
    }

    private fun createLocation(
        id: Long,
        latitude: Double,
        longitude: Double
    ): Location {
        return Location(
            id = id,
            latitude = latitude,
            longitude = longitude,
            accuracy = 10.0f,
            altitude = null,
            bearing = null,
            speed = null,
            timestamp = System.currentTimeMillis(),
            provider = "gps"
        )
    }
}
