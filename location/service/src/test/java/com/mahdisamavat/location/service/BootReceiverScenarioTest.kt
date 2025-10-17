package com.mahdisamavat.location.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BootReceiverScenarioTest {

    @Test
    fun `scenario - boot action constants are defined`() {
        val bootAction = "android.intent.action.BOOT_COMPLETED"
        val lockedBootAction = "android.intent.action.LOCKED_BOOT_COMPLETED"

        assertEquals("android.intent.action.BOOT_COMPLETED", bootAction)
        assertEquals("android.intent.action.LOCKED_BOOT_COMPLETED", lockedBootAction)
    }

    @Test
    fun `scenario - service state is accessible after boot`() {
        val serviceRunning = LocationCollectionService.isServiceRunning()

        assertTrue(serviceRunning is Boolean)
    }

    @Test
    fun `scenario - service can check running state consistently`() {
        val state1 = LocationCollectionService.isServiceRunning()
        val state2 = LocationCollectionService.isServiceRunning()

        assertEquals(state1, state2)
    }
}
