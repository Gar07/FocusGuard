package com.example.focusguard.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Engine pemrosesan sensor fisik native (Accelerometer & Proximity)
 * untuk Flip-to-Focus dan Desk-Sentry Anti-Theft.
 */
public class SensorController implements SensorEventListener {

    public static final float GRAVITY_EARTH = 9.80665f;
    public static final float FACE_DOWN_Z_THRESHOLD = -8.5f;
    public static final float FACE_UP_Z_THRESHOLD = -5.0f;

    public interface SensorCallback {
        void onDeviceFaceDown();
        void onDeviceFaceUp();
        void onMotionDetected(double deltaMovement);
        void onTelemetryUpdated(float x, float y, float z, float proximityCm, double delta);
    }

    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private final Sensor proximitySensor;
    private final SensorCallback callback;

    private boolean isFaceDown = false;
    private float lastX = 0f, lastY = 0f, lastZ = GRAVITY_EARTH;
    private float currentX = 0f, currentY = 0f, currentZ = GRAVITY_EARTH;
    private float currentProximityCm = 5.0f;
    private float maxProximityRange = 5.0f;
    private boolean isFirstRead = true;
    private boolean sentryModeActive = false;
    private float motionThreshold = 1.8f;
    private boolean manualSimulationLock = false;

    public SensorController(Context context, SensorCallback callback) {
        this.callback = callback;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            if (proximitySensor != null) {
                maxProximityRange = proximitySensor.getMaximumRange();
                currentProximityCm = maxProximityRange;
            }
        } else {
            accelerometer = null;
            proximitySensor = null;
        }
    }

    public boolean hasAccelerometer() {
        return accelerometer != null;
    }

    public boolean hasProximitySensor() {
        return proximitySensor != null;
    }

    public void setMotionThreshold(float threshold) {
        this.motionThreshold = threshold;
    }

    public void startListening(boolean enableSentryMode) {
        this.sentryModeActive = enableSentryMode;
        this.isFirstRead = true;
        this.manualSimulationLock = false;
        if (sensorManager == null) return;

        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
        if (proximitySensor != null && !enableSentryMode) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void stopListening() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    /**
     * Rumus matematis deteksi orientasi telungkup:
     * a_z <= -8.5 m/s^2 dan (jika proximity tersedia & aktif) d < maxRange.
     */
    public static boolean isFaceDownState(float z, float proximityCm, float maxRange, boolean requireProximity) {
        boolean zFaceDown = (z <= FACE_DOWN_Z_THRESHOLD);
        if (!requireProximity) {
            return zFaceDown;
        }
        return zFaceDown && (proximityCm < maxRange);
    }

    /**
     * Rumus matematis deviasi percepatan dinamis (Mode Sentry):
     * Mengukur maksimum antara perubahan vektor antar-siklus dan deviasi terhadap gravitasi g.
     */
    public static double calculateMovementDelta(
            float x, float y, float z,
            float prevX, float prevY, float prevZ) {
        double vectorDelta = Math.sqrt(
                Math.pow(x - prevX, 2) +
                Math.pow(y - prevY, 2) +
                Math.pow(z - prevZ, 2));
        double magnitude = Math.sqrt(x * x + y * y + z * z);
        double gravityDelta = Math.abs(magnitude - GRAVITY_EARTH);
        return Math.max(vectorDelta, gravityDelta);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (manualSimulationLock) {
            return; // Jangan timpa state simulasi manual pada emulator
        }

        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            currentProximityCm = event.values[0];
            evaluateFocusOrientation();
            if (callback != null) {
                callback.onTelemetryUpdated(currentX, currentY, currentZ, currentProximityCm, 0.0);
            }
            return;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            currentX = event.values[0];
            currentY = event.values[1];
            currentZ = event.values[2];

            double delta = 0.0;
            if (!isFirstRead) {
                delta = calculateMovementDelta(currentX, currentY, currentZ, lastX, lastY, lastZ);
            }
            lastX = currentX;
            lastY = currentY;
            lastZ = currentZ;
            isFirstRead = false;

            if (callback != null) {
                callback.onTelemetryUpdated(currentX, currentY, currentZ, currentProximityCm, delta);
            }

            if (sentryModeActive) {
                if (delta > motionThreshold && callback != null) {
                    callback.onMotionDetected(delta);
                }
            } else {
                evaluateFocusOrientation();
            }
        }
    }

    private void evaluateFocusOrientation() {
        // Deteksi telungkup saat Z <= -8.5 m/s^2
        if (currentZ <= FACE_DOWN_Z_THRESHOLD && !isFaceDown) {
            isFaceDown = true;
            if (callback != null) {
                callback.onDeviceFaceDown();
            }
        } else if (currentZ > FACE_UP_Z_THRESHOLD && isFaceDown) {
            // Ponsel diangkat dari meja (Z > -5.0 m/s^2)
            isFaceDown = false;
            if (callback != null) {
                callback.onDeviceFaceUp();
            }
        }
    }

    // Metode simulasi interaktif untuk demonstrasi UTS & pengujian Emulator
    public void simulateFaceDown() {
        manualSimulationLock = true;
        currentX = 0.02f;
        currentY = -0.05f;
        currentZ = -9.78f;
        currentProximityCm = 0.0f;
        if (callback != null) {
            callback.onTelemetryUpdated(currentX, currentY, currentZ, currentProximityCm, 0.05);
        }
        if (!isFaceDown) {
            isFaceDown = true;
            if (callback != null) {
                callback.onDeviceFaceDown();
            }
        }
    }

    public void simulateFaceUp() {
        manualSimulationLock = true;
        currentX = 0.45f;
        currentY = 4.20f;
        currentZ = 8.65f;
        currentProximityCm = 5.0f;
        if (callback != null) {
            callback.onTelemetryUpdated(currentX, currentY, currentZ, currentProximityCm, 3.10);
        }
        if (isFaceDown) {
            isFaceDown = false;
            if (callback != null) {
                callback.onDeviceFaceUp();
            }
        }
    }

    public void simulateSentryMotion(double simulatedDelta) {
        manualSimulationLock = true;
        currentX = 3.85f;
        currentY = -2.90f;
        currentZ = 11.40f;
        if (callback != null) {
            callback.onTelemetryUpdated(currentX, currentY, currentZ, currentProximityCm, simulatedDelta);
            callback.onMotionDetected(simulatedDelta);
        }
    }

    public boolean isCurrentlyFaceDown() {
        return isFaceDown;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Tidak memerlukan kalibrasi akurasi khusus
    }
}
