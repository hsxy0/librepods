package me.kavishdevar.librepods.presentation.viewmodel

import me.kavishdevar.librepods.data.AirPodsNotifications
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoModeConnectionPolicyTest {
    @Test fun realConnectionLeavesDemoMode() {
        assertTrue(isRealAirPodsConnectionEvent(AirPodsNotifications.AIRPODS_CONNECTION_DETECTED))
        assertTrue(isRealAirPodsConnectionEvent(AirPodsNotifications.AIRPODS_CONNECTED))
    }

    @Test fun unrelatedUpdatesDoNotLeaveDemoMode() {
        assertFalse(isRealAirPodsConnectionEvent(AirPodsNotifications.BATTERY_DATA))
        assertFalse(isRealAirPodsConnectionEvent(AirPodsNotifications.AIRPODS_DISCONNECTED))
    }
}
