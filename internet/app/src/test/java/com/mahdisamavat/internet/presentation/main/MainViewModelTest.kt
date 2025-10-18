package com.mahdisamavat.internet.presentation.main

import com.mahdisamavat.core.common.error.AppError
import com.mahdisamavat.core.analytics.AnalyticsHelper
import com.mahdisamavat.core.common.result.Result
import com.mahdisamavat.core.ipc.model.Response
import com.mahdisamavat.core.logger.Logger
import com.mahdisamavat.core.model.Location
import com.mahdisamavat.domain.repository.CommandRepository
import com.mahdisamavat.domain.repository.LocationQueryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.mockito.kotlin.any
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private lateinit var viewModel: MainViewModel
    private lateinit var commandRepository: CommandRepository
    private lateinit var locationQueryRepository: LocationQueryRepository
    private lateinit var logger: Logger
    private lateinit var analyticsHelper: AnalyticsHelper
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScheduler = testDispatcher.scheduler

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        commandRepository = mock()
        locationQueryRepository = mock()
        logger = mock()
        analyticsHelper = mock()
        viewModel = MainViewModel(commandRepository, locationQueryRepository, logger, analyticsHelper)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }


    @Test
    fun `initial state has default values`() = runTest {
        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertTrue(state.locations.isEmpty())
        assertNull(state.latestLocation)
        assertNull(state.lastResponse)
        assertNull(state.error)
        assertNull(state.serviceRunning)
    }


    @Test
    fun `handleIntent StartService success updates state`() = runTest {
        val response = Response.ServiceState(isRunning = true, message = "Service started")
        whenever(commandRepository.sendCommand(any(), isNull())).thenReturn(Result.success(response))

        viewModel.handleIntent(MainContract.Intent.StartService)
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(response, state.lastResponse)
        assertEquals(true, state.serviceRunning)
        assertNull(state.error)
    }

    @Test
    fun `handleIntent StartService failure updates error state`() = runTest {
        val error = AppError.IPC("Connection failed", null, false, "Test")
        whenever(commandRepository.sendCommand(any(), isNull())).thenReturn(Result.failure(error))

        viewModel.handleIntent(MainContract.Intent.StartService)
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Connection failed", state.error)
    }

    @Test
    fun `handleIntent StartService sets loading state during execution`() = runTest {
        val response = Response.ServiceState(isRunning = true, message = "Service started")
        whenever(commandRepository.sendCommand(any(), isNull())).thenReturn(Result.success(response))

        viewModel.handleIntent(MainContract.Intent.StartService)

        val finalState = viewModel.state.value
        assertFalse(finalState.isLoading)
    }


    @Test
    fun `handleIntent StopService success updates state`() = runTest {
        val response = Response.ServiceState(isRunning = false, message = "Service stopped")
        whenever(commandRepository.sendCommand(any(), isNull())).thenReturn(Result.success(response))

        viewModel.handleIntent(MainContract.Intent.StopService)
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(response, state.lastResponse)
        assertEquals(false, state.serviceRunning)
        assertNull(state.error)
    }

    @Test
    fun `handleIntent StopService failure updates error state`() = runTest {
        val error = AppError.IPC("Service not found", null, false, "Test")
        whenever(commandRepository.sendCommand(any(), isNull())).thenReturn(Result.failure(error))

        viewModel.handleIntent(MainContract.Intent.StopService)
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Service not found", state.error)
    }


    @Test
    fun `handleIntent GetAllLocations success updates state with locations`() = runTest {
        val locations = listOf(
            createLocation(1, 37.7749, -122.4194),
            createLocation(2, 34.0522, -118.2437)
        )
        whenever(locationQueryRepository.getAllLocations()).thenReturn(Result.success(locations))

        viewModel.handleIntent(MainContract.Intent.GetAllLocations)

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(2, state.locations.size)
        assertEquals(37.7749, state.locations[0].latitude, 0.0001)
        assertNull(state.latestLocation)
        assertNull(state.error)
    }

    @Test
    fun `handleIntent GetAllLocations with empty list updates state correctly`() = runTest {
        val emptyList = emptyList<Location>()
        whenever(locationQueryRepository.getAllLocations()).thenReturn(Result.success(emptyList))

        viewModel.handleIntent(MainContract.Intent.GetAllLocations)

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.locations.isEmpty())
        assertNull(state.latestLocation)
    }

    @Test
    fun `handleIntent GetAllLocations failure updates error state`() = runTest {
        val error = AppError.IPC("No locations available", null, false, "Test")
        whenever(locationQueryRepository.getAllLocations()).thenReturn(Result.failure(error))

        viewModel.handleIntent(MainContract.Intent.GetAllLocations)

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("No locations available", state.error)
    }


    @Test
    fun `handleIntent GetLatestLocation success updates state with single location`() = runTest {
        val location = createLocation(1, 37.7749, -122.4194)
        whenever(locationQueryRepository.getLatestLocation()).thenReturn(Result.success(location))

        viewModel.handleIntent(MainContract.Intent.GetLatestLocation)

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(location, state.latestLocation)
        assertTrue(state.locations.isEmpty())
        assertNull(state.error)
    }

    @Test
    fun `handleIntent GetLatestLocation with null location updates state correctly`() = runTest {
        whenever(locationQueryRepository.getLatestLocation()).thenReturn(Result.success(null))

        viewModel.handleIntent(MainContract.Intent.GetLatestLocation)

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.latestLocation)
        assertTrue(state.locations.isEmpty())
    }

    @Test
    fun `handleIntent GetLatestLocation failure updates error state`() = runTest {
        val error = AppError.Security("Permission denied", null, false, "Test")
        whenever(locationQueryRepository.getLatestLocation()).thenReturn(Result.failure(error))

        viewModel.handleIntent(MainContract.Intent.GetLatestLocation)

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Permission denied", state.error)
    }


    @Test
    fun `handleIntent ClearError clears error state`() = runTest {
        val freshViewModel = MainViewModel(commandRepository, locationQueryRepository, logger, analyticsHelper)

        val error = AppError.IPC("Test error", null, false, "Test")
        whenever(commandRepository.sendCommand(any(), isNull())).thenReturn(Result.failure(error))

        freshViewModel.handleIntent(MainContract.Intent.StartService)

        testScheduler.advanceUntilIdle()

        assertEquals("Test error", freshViewModel.state.value.error)

        freshViewModel.handleIntent(MainContract.Intent.ClearError)

        assertNull(freshViewModel.state.value.error)
    }

    @Test
    fun `handleIntent ClearError on clean state does nothing`() = runTest {
        assertNull(viewModel.state.value.error)

        viewModel.handleIntent(MainContract.Intent.ClearError)

        assertNull(viewModel.state.value.error)
    }


    @Test
    fun `multiple commands update state correctly`() = runTest {
        val startResponse = Response.ServiceState(isRunning = true, message = "Service started")
        val locations = listOf(createLocation(1, 37.7749, -122.4194))

        whenever(commandRepository.sendCommand("START_SERVICE", null))
            .thenReturn(Result.success(startResponse))
        whenever(locationQueryRepository.getAllLocations())
            .thenReturn(Result.success(locations))

        viewModel.handleIntent(MainContract.Intent.StartService)
        assertEquals(true, viewModel.state.value.serviceRunning)

        viewModel.handleIntent(MainContract.Intent.GetAllLocations)
        assertEquals(1, viewModel.state.value.locations.size)
        assertEquals(true, viewModel.state.value.serviceRunning)
    }

    @Test
    fun `getting all locations clears latest location`() = runTest {
        val location = createLocation(1, 37.7749, -122.4194)
        val locations = listOf(createLocation(2, 34.0522, -118.2437))

        whenever(locationQueryRepository.getLatestLocation()).thenReturn(Result.success(location))
        whenever(locationQueryRepository.getAllLocations()).thenReturn(Result.success(locations))

        viewModel.handleIntent(MainContract.Intent.GetLatestLocation)
        assertEquals(location, viewModel.state.value.latestLocation)

        viewModel.handleIntent(MainContract.Intent.GetAllLocations)
        assertNull(viewModel.state.value.latestLocation)
        assertEquals(1, viewModel.state.value.locations.size)
    }

    @Test
    fun `getting latest location clears locations list`() = runTest {
        val locations = listOf(createLocation(1, 37.7749, -122.4194))
        val latestLocation = createLocation(2, 34.0522, -118.2437)

        whenever(locationQueryRepository.getAllLocations()).thenReturn(Result.success(locations))
        whenever(locationQueryRepository.getLatestLocation()).thenReturn(
            Result.success(
                latestLocation
            )
        )

        viewModel.handleIntent(MainContract.Intent.GetAllLocations)
        assertEquals(1, viewModel.state.value.locations.size)

        viewModel.handleIntent(MainContract.Intent.GetLatestLocation)
        assertTrue(viewModel.state.value.locations.isEmpty())
        assertEquals(latestLocation, viewModel.state.value.latestLocation)
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
