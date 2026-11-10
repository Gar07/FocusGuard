package com.example.focusguard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.focusguard.sensor.SensorController;
import com.example.focusguard.util.PreferenceManager;

import org.junit.Test;

public class FocusGuardUnitTest {

    @Test
    public void testFaceDownOrientationMath() {
        // Layar menghadap bawah (Z = -9.6 m/s^2) dan proximity = 0 cm -> Telungkup (true)
        assertTrue(SensorController.isFaceDownState(-9.6f, 0.0f, 5.0f, true));
        // Layar menghadap atas (Z = +9.8 m/s^2) -> Tidak telungkup (false)
        assertFalse(SensorController.isFaceDownState(9.8f, 0.0f, 5.0f, true));
        // Layar menghadap bawah tetapi proximity jauh pada mode wajib proximity -> false
        assertFalse(SensorController.isFaceDownState(-9.2f, 5.0f, 5.0f, true));
        // Mode hybrid tanpa proximity wajib -> true jika Z <= -8.5
        assertTrue(SensorController.isFaceDownState(-9.1f, 5.0f, 5.0f, false));
    }

    @Test
    public void testSentryMovementDeltaMath() {
        // Perangkat diam di meja datar (0, 0, 9.80665) -> Delta ~ 0
        double idleDelta = SensorController.calculateMovementDelta(
                0f, 0f, SensorController.GRAVITY_EARTH,
                0f, 0f, SensorController.GRAVITY_EARTH);
        assertEquals(0.0, idleDelta, 0.01);

        // Perangkat digerakkan mendadak -> Delta > 1.8 m/s^2 (Threshold Sentry)
        double movedDelta = SensorController.calculateMovementDelta(
                2.5f, -1.8f, 10.5f,
                0f, 0f, SensorController.GRAVITY_EARTH);
        assertTrue("Delta gerakan harus melebihi ambang 1.8 m/s^2", movedDelta > 1.8);
    }

    @Test
    public void testPinValidation() {
        assertTrue(PreferenceManager.isValidPin("1234"));
        assertTrue(PreferenceManager.isValidPin("0099"));
        assertFalse(PreferenceManager.isValidPin("123"));
        assertFalse(PreferenceManager.isValidPin("12a4"));
        assertFalse(PreferenceManager.isValidPin(null));
    }
}
