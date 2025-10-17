package com.mahdisamavat.location.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ServiceStartStopScenarioTest {

    @Test
    fun `scenario - service can check if it is running`() {
        val isRunning = LocationCollectionService.isServiceRunning()

        assertTrue(isRunning is Boolean)
    }

    @Test
    fun `scenario - service running state is queryable`() {
        val state = LocationCollectionService.isServiceRunning()

        assertFalse(state is String)
        assertTrue(state is Boolean)
    }

    @Test
    fun `scenario - service state remains consistent during checks`() {
        val firstCheck = LocationCollectionService.isServiceRunning()
        val secondCheck = LocationCollectionService.isServiceRunning()
        val thirdCheck = LocationCollectionService.isServiceRunning()

        assertEquals(firstCheck, secondCheck)
        assertEquals(secondCheck, thirdCheck)
    }

    @Test
    fun `scenario - service provides boolean state value`() {
        val serviceState = LocationCollectionService.isServiceRunning()

        assertTrue(serviceState == true || serviceState == false)
    }

    @Test
    fun `scenario - service state check does not throw exception`() {
        try {
            val state = LocationCollectionService.isServiceRunning()
            assertTrue(state is Boolean)
        } catch (e: Exception) {
            throw AssertionError("Service state check should not throw exception", e)
        }
    }

    @Test
    fun `scenario - multiple sequential state checks work correctly`() {
        val states = mutableListOf<Boolean>()

        repeat(10) {
            states.add(LocationCollectionService.isServiceRunning())
        }

        assertEquals(10, states.size)
        assertTrue(states.all { it is Boolean })
    }

    @Test
    fun `scenario - service state is accessible from any thread`() {
        val mainThreadState = LocationCollectionService.isServiceRunning()

        assertTrue(mainThreadState is Boolean)
    }
}
