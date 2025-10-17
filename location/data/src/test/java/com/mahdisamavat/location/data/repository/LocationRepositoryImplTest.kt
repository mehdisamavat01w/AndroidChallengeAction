package com.mahdisamavat.location.data.repository

import com.mahdisamavat.core.common.result.Result
import com.mahdisamavat.core.logger.Logger
import com.mahdisamavat.core.model.Location
import com.mahdisamavat.location.data.dao.LocationDao
import com.mahdisamavat.location.data.entity.LocationEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LocationRepositoryImplTest {

    private lateinit var repository: LocationRepositoryImpl
    private lateinit var locationDao: LocationDao
    private lateinit var logger: Logger
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        locationDao = mockk()
        logger = mockk(relaxed = true)
        repository = LocationRepositoryImpl(locationDao, logger, testDispatcher)
    }

    @Test
    fun `getAllLocations returns success with locations`() = runTest {
        val entities = listOf(
            createLocationEntity(1, 37.7749, -122.4194),
            createLocationEntity(2, 34.0522, -118.2437)
        )
        coEvery { locationDao.getAllLocations() } returns entities

        val result = repository.getAllLocations()

        assertTrue(result is Result.Success)
        val locations = (result as Result.Success).data
        assertEquals(2, locations.size)
        assertEquals(37.7749, locations[0].latitude, 0.0001)
    }

    @Test
    fun `getAllLocations handles empty list`() = runTest {
        coEvery { locationDao.getAllLocations() } returns emptyList()

        val result = repository.getAllLocations()

        assertTrue(result is Result.Success)
        assertEquals(0, (result as Result.Success).data.size)
    }

    @Test
    fun `getAllLocations returns failure on exception`() = runTest {
        coEvery { locationDao.getAllLocations() } throws Exception("Database error")

        val result = repository.getAllLocations()

        assertTrue(result is Result.Failure)
    }

    @Test
    fun `getLatestLocation returns success with location`() = runTest {
        val entity = createLocationEntity(1, 37.7749, -122.4194)
        coEvery { locationDao.getLatestLocation() } returns entity

        val result = repository.getLatestLocation()

        assertTrue(result is Result.Success)
        val location = (result as Result.Success).data
        assertEquals(37.7749, location?.latitude ?: 0.0, 0.0001)
    }

    @Test
    fun `getLatestLocation returns null when no locations exist`() = runTest {
        coEvery { locationDao.getLatestLocation() } returns null

        val result = repository.getLatestLocation()

        assertTrue(result is Result.Success)
        assertEquals(null, (result as Result.Success).data)
    }

    @Test
    fun `storeLocation inserts location successfully`() = runTest {
        val location = createLocation(0, 37.7749, -122.4194)
        coEvery { locationDao.insertLocation(any()) } returns 1L

        val result = repository.storeLocation(location)

        assertTrue(result is Result.Success)
        assertEquals(1L, (result as Result.Success).data)
        coVerify { locationDao.insertLocation(any()) }
    }

    @Test
    fun `storeLocation handles insertion failure`() = runTest {
        val location = createLocation(0, 37.7749, -122.4194)
        coEvery { locationDao.insertLocation(any()) } throws Exception("Insert failed")

        val result = repository.storeLocation(location)

        assertTrue(result is Result.Failure)
    }

    @Test
    fun `getLocationCount returns correct count`() = runTest {
        coEvery { locationDao.getLocationCount() } returns 5

        val result = repository.getLocationCount()

        assertTrue(result is Result.Success)
        assertEquals(5, (result as Result.Success).data)
    }

    @Test
    fun `deleteLocationsBefore deletes old locations`() = runTest {
        val timestamp = System.currentTimeMillis() - 86400000
        coEvery { locationDao.deleteLocationsBefore(timestamp) } returns 3

        val result = repository.deleteLocationsBefore(timestamp)

        assertTrue(result is Result.Success)
        assertEquals(3, (result as Result.Success).data)
    }

    @Test
    fun `getAllLocationsFlow emits updates`() = runTest {
        val entities = listOf(createLocationEntity(1, 37.7749, -122.4194))
        every { locationDao.getAllLocationsFlow() } returns flowOf(entities)

        val result = repository.getAllLocationsFlow().first()

        assertTrue(result is Result.Success)
        assertEquals(1, (result as Result.Success).data.size)
    }

    private fun createLocationEntity(
        id: Long,
        latitude: Double,
        longitude: Double
    ): LocationEntity {
        return LocationEntity(
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
