package com.mahdisamavat.location.domain.usecase

import com.mahdisamavat.core.common.result.Result
import com.mahdisamavat.core.model.Location
import com.mahdisamavat.location.domain.repository.LocationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetLatestLocationUseCaseTest {

    private lateinit var useCase: GetLatestLocationUseCase
    private lateinit var repository: LocationRepository

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetLatestLocationUseCase(repository)
    }

    @Test
    fun `invoke returns latest location successfully`() = runTest {
        val location = createLocation(1, 37.7749, -122.4194)
        coEvery { repository.getLatestLocation() } returns Result.success(location)

        val result = useCase()

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals(37.7749, data?.latitude ?: 0.0, 0.0001)
        coVerify { repository.getLatestLocation() }
    }

    @Test
    fun `invoke returns null when no locations exist`() = runTest {
        coEvery { repository.getLatestLocation() } returns Result.success(null)

        val result = useCase()

        assertTrue(result is Result.Success)
        assertNull((result as Result.Success).data)
    }

    @Test
    fun `invoke handles repository failure`() = runTest {
        val error = mockk<com.mahdisamavat.core.common.error.AppError>()
        coEvery { repository.getLatestLocation() } returns Result.failure(error)

        val result = useCase()

        assertTrue(result is Result.Failure)
    }

    @Test
    fun `invoke returns location with all fields`() = runTest {
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
        coEvery { repository.getLatestLocation() } returns Result.success(location)

        val result = useCase()

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals(100.0, data?.altitude)
        assertEquals(45.0f, data?.bearing)
        assertEquals(5.5f, data?.speed)
    }

    @Test
    fun `invoke returns most recent location by timestamp`() = runTest {
        val timestamp = System.currentTimeMillis()
        val location = createLocation(1, 37.7749, -122.4194).copy(timestamp = timestamp)
        coEvery { repository.getLatestLocation() } returns Result.success(location)

        val result = useCase()

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals(timestamp, data?.timestamp)
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
