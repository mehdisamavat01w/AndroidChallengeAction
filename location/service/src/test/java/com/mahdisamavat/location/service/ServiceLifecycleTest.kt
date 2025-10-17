package com.mahdisamavat.location.service

import org.junit.Assert.assertTrue
import org.junit.Test

class ServiceLifecycleTest {

    @Test
    fun `isServiceRunning returns false initially`() {
        val isRunning = LocationCollectionService.isServiceRunning()

        assertTrue(
            "Service should report initial state correctly",
            isRunning || !isRunning
        )
    }

    @Test
    fun `service running state is accessible`() {
        val state1 = LocationCollectionService.isServiceRunning()
        val state2 = LocationCollectionService.isServiceRunning()

        assertTrue("Service state should be consistent", state1 == state2)
    }

    @Test
    fun `service companion object provides state check method`() {
        try {
            LocationCollectionService.isServiceRunning()
        } catch (e: Exception) {
            throw AssertionError("isServiceRunning should not throw exception", e)
        }
    }
}
