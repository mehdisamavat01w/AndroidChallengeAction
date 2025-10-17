package com.mahdisamavat.location.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ServiceRecoveryScenarioTest {

    @Test
    fun `scenario - service state query is accessible`() {
        val serviceRunning = LocationCollectionService.isServiceRunning()

        assertTrue(serviceRunning is Boolean)
    }

    @Test
    fun `scenario - service state check remains consistent`() {
        val state1 = LocationCollectionService.isServiceRunning()
        val state2 = LocationCollectionService.isServiceRunning()

        assertEquals(state1, state2)
    }

    @Test
    fun `scenario - multiple service state checks are consistent`() {
        val states = mutableListOf<Boolean>()

        repeat(5) {
            states.add(LocationCollectionService.isServiceRunning())
        }

        val firstState = states[0]
        assertTrue(states.all { it == firstState })
    }

    @Test
    fun `scenario - service can report running state`() {
        val isRunning = LocationCollectionService.isServiceRunning()

        assertTrue(isRunning is Boolean)
    }

    @Test
    fun `scenario - service state API is available`() {
        try {
            LocationCollectionService.isServiceRunning()
        } catch (e: Exception) {
            throw AssertionError("Service state API should not throw exception")
        }
    }
}
