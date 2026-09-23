package me.kavishdevar.librepods.services

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AirPodsConnectionPopupPolicyTest {
    @Test fun savedAirPodsConnectsBeforeSdpCompletes() {
        assertTrue(shouldDispatchAirPodsConnection("08:E6:4B:9C:B0:95", "08:e6:4b:9c:b0:95", false))
    }

    @Test fun newlyPairedAirPodsCanBeRecognizedByUuid() {
        assertTrue(shouldDispatchAirPodsConnection("", "08:E6:4B:9C:B0:95", true))
    }

    @Test fun unrelatedBluetoothDeviceDoesNotTriggerPopup() {
        assertFalse(shouldDispatchAirPodsConnection("08:E6:4B:9C:B0:95", "30:11:22:33:44:55", false))
        assertFalse(shouldDispatchAirPodsConnection("", "30:11:22:33:44:55", false))
    }
}
