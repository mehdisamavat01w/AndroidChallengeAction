package com.mahdisamavat.location.domain.usecase

import com.mahdisamavat.core.common.result.Result
import com.mahdisamavat.core.model.Location
import com.mahdisamavat.location.domain.repository.LocationRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetAllLocationsUseCaseTest {

    private lateinit var useCase: GetAllLocationsUseCase
    private lateinit var repository: LocationRepository

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetAllLocationsUseCase(repository)
    }

    @Test
    fun `invoke returns all locations successfully`() = runTest {
        val locations = listOf(
            createLocation(1, 37.7749, -122.4194),
            createLocation(2, 34.0522, -118.2437)
        )
        every { repository.getAllLocationsFlow() } returns flowOf(Result.success(locations))

        val result = useCase().first()

        assertTrue(result is Result.Success)
        assertEquals(2, (result as Result.Success).data.size)
        verify { repository.getAllLocationsFlow() }
    }

    @Test
    fun `invoke returns empty list when no locations exist`() = runTest {
        every { repository.getAllLocationsFlow() } returns flowOf(Result.success(emptyList()))

        val result = useCase().first()

        assertTrue(result is Result.Success)
        assertEquals(0, (result as Result.Success).data.size)
    }

    @Test
    fun `invoke handles repository failure`() = runTest {
        val error = mockk<com.mahdisamavat.core.common.error.AppError>()
        every { repository.getAllLocationsFlow() } returns flowOf(Result.failure(error))

        val result = useCase().first()

        assertTrue(result is Result.Failure)
    }

    @Test
    fun `invoke preserves location order`() = runTest {
        val locations = listOf(
            createLocation(1, 37.7749, -122.4194),
            createLocation(2, 34.0522, -118.2437),
            createLocation(3, 40.7128, -74.0060)
        )
        every { repository.getAllLocationsFlow() } returns flowOf(Result.success(locations))

        val result = useCase().first()

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals(37.7749, data[0].latitude, 0.0001)
        assertEquals(34.0522, data[1].latitude, 0.0001)
        assertEquals(40.7128, data[2].latitude, 0.0001)
    }

    @Test
    fun `invoke returns locations with all fields`() = runTest {
        val location = Location(
            id = 1,
            latitude = 37.7749,
            longitude = -122.4194,
            accuracy = 15.5f,
            altitude = 100.0,
            bearing = 45.0f,
            speed = 5.5f,
            timestamp = System.currentTimeMillis(),
            provider = "gps"
        )
        every { repository.getAllLocationsFlow() } returns flowOf(Result.success(listOf(location)))

        val result = useCase().first()

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals(100.0, data[0].altitude)
        assertEquals(45.0f, data[0].bearing)
        assertEquals(5.5f, data[0].speed)
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
