package com.mahdisamavat.location.domain

import com.mahdisamavat.core.common.result.Result
import com.mahdisamavat.core.model.Location
import com.mahdisamavat.location.domain.repository.LocationRepository
import com.mahdisamavat.location.domain.usecase.GetAllLocationsUseCase
import com.mahdisamavat.location.domain.usecase.GetLatestLocationUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LocationStorageScenarioTest {

    private lateinit var repository: LocationRepository
    private lateinit var getAllUseCase: GetAllLocationsUseCase
    private lateinit var getLatestUseCase: GetLatestLocationUseCase

    @Before
    fun setup() {
        repository = mockk()
        getAllUseCase = GetAllLocationsUseCase(repository)
        getLatestUseCase = GetLatestLocationUseCase(repository)
    }

    @Test
    fun `scenario - store first location and retrieve it`() = runTest {
        val location = createLocation(1, 37.7749, -122.4194)
        val locations = listOf(location)

        every { repository.getAllLocationsFlow() } returns flowOf(Result.success(locations))

        val result = getAllUseCase().first()

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals(1, data.size)
        assertEquals(37.7749, data[0].latitude, 0.0001)
    }

    @Test
    fun `scenario - store multiple locations and retrieve all`() = runTest {
        val locations = listOf(
            createLocation(1, 37.7749, -122.4194),
            createLocation(2, 34.0522, -118.2437),
            createLocation(3, 40.7128, -74.0060)
        )

        every { repository.getAllLocationsFlow() } returns flowOf(Result.success(locations))

        val result = getAllUseCase().first()

        assertTrue(result is Result.Success)
        assertEquals(3, (result as Result.Success).data.size)
    }

    @Test
    fun `scenario - retrieve latest location from multiple stored`() = runTest {
        val now = System.currentTimeMillis()
        val latestLocation = createLocation(3, 40.7128, -74.0060).copy(timestamp = now)

        coEvery { repository.getLatestLocation() } returns Result.success(latestLocation)

        val result = getLatestUseCase()

        assertTrue(result is Result.Success)
        val latest = (result as Result.Success).data
        assertNotNull(latest)
        assertEquals(40.7128, latest?.latitude ?: 0.0, 0.0001)
        assertEquals(now, latest?.timestamp ?: 0)
    }

    @Test
    fun `scenario - no locations stored returns empty list`() = runTest {
        every { repository.getAllLocationsFlow() } returns flowOf(Result.success(emptyList()))

        val result = getAllUseCase().first()

        assertTrue(result is Result.Success)
        assertTrue((result as Result.Success).data.isEmpty())
    }

    @Test
    fun `scenario - no locations stored returns null for latest`() = runTest {
        coEvery { repository.getLatestLocation() } returns Result.success(null)

        val result = getLatestUseCase()

        assertTrue(result is Result.Success)
        assertNull((result as Result.Success).data)
    }

    @Test
    fun `scenario - continuous location updates over time`() = runTest {
        val timestamp1 = System.currentTimeMillis()
        val timestamp2 = timestamp1 + 60000
        val timestamp3 = timestamp2 + 60000

        val locations = listOf(
            createLocation(1, 37.7749, -122.4194).copy(timestamp = timestamp1),
            createLocation(2, 37.7750, -122.4195).copy(timestamp = timestamp2),
            createLocation(3, 37.7751, -122.4196).copy(timestamp = timestamp3)
        )

        every { repository.getAllLocationsFlow() } returns flowOf(Result.success(locations))

        val result = getAllUseCase().first()

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals(3, data.size)
        assertTrue(data[0].timestamp < data[1].timestamp)
        assertTrue(data[1].timestamp < data[2].timestamp)
    }

    @Test
    fun `scenario - store location with all optional fields preserved`() = runTest {
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

        val result = getAllUseCase().first()

        assertTrue(result is Result.Success)
        val retrieved = (result as Result.Success).data[0]
        assertEquals(100.0, retrieved.altitude ?: 0.0, 0.01)
        assertEquals(45.0f, retrieved.bearing ?: 0.0f, 0.01f)
        assertEquals(5.5f, retrieved.speed ?: 0.0f, 0.01f)
    }

    @Test
    fun `scenario - retrieve locations maintains insertion order`() = runTest {
        val locations = listOf(
            createLocation(1, 37.7749, -122.4194),
            createLocation(2, 34.0522, -118.2437),
            createLocation(3, 40.7128, -74.0060),
            createLocation(4, 51.5074, -0.1278)
        )

        every { repository.getAllLocationsFlow() } returns flowOf(Result.success(locations))

        val result = getAllUseCase().first()

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals(37.7749, data[0].latitude, 0.0001)
        assertEquals(34.0522, data[1].latitude, 0.0001)
        assertEquals(40.7128, data[2].latitude, 0.0001)
        assertEquals(51.5074, data[3].latitude, 0.0001)
    }

    @Test
    fun `scenario - location accuracy is preserved`() = runTest {
        val location = createLocation(1, 37.7749, -122.4194).copy(accuracy = 5.2f)

        every { repository.getAllLocationsFlow() } returns flowOf(Result.success(listOf(location)))

        val result = getAllUseCase().first()

        assertTrue(result is Result.Success)
        assertEquals(5.2f, (result as Result.Success).data[0].accuracy, 0.01f)
    }

    @Test
    fun `scenario - location provider is preserved`() = runTest {
        val gpsLocation = createLocation(1, 37.7749, -122.4194).copy(provider = "gps")
        val networkLocation = createLocation(2, 34.0522, -118.2437).copy(provider = "network")

        every { repository.getAllLocationsFlow() } returns flowOf(
            Result.success(listOf(gpsLocation, networkLocation))
        )

        val result = getAllUseCase().first()

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals("gps", data[0].provider)
        assertEquals("network", data[1].provider)
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
