package com.example.focusguard.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Pengelola konfigurasi ringan & PIN menggunakan SharedPreferences native.
 */
public class PreferenceManager {

    private static final String PREF_NAME = "focus_guard_prefs";
    public static final String KEY_USER_PIN = "KEY_USER_PIN";
    public static final String KEY_DEFAULT_FOCUS_TIME = "KEY_DEFAULT_FOCUS_TIME";
    public static final String KEY_SENTRY_GRACE_PERIOD = "KEY_SENTRY_GRACE_PERIOD";
    public static final String KEY_SENTRY_SENSITIVITY = "KEY_SENTRY_SENSITIVITY";

    private final SharedPreferences prefs;

    public PreferenceManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public String getUserPin() {
        return prefs.getString(KEY_USER_PIN, "1234");
    }

    public void setUserPin(String pin) {
        if (isValidPin(pin)) {
            prefs.edit().putString(KEY_USER_PIN, pin).apply();
        }
    }

    public static boolean isValidPin(String pin) {
        return pin != null && pin.matches("\\d{4}");
    }

    public boolean verifyPin(String inputPin) {
        return getUserPin().equals(inputPin);
    }

    public int getDefaultFocusTimeMinutes() {
        return prefs.getInt(KEY_DEFAULT_FOCUS_TIME, 25);
    }

    public void setDefaultFocusTimeMinutes(int minutes) {
        prefs.edit().putInt(KEY_DEFAULT_FOCUS_TIME, minutes).apply();
    }

    public int getSentryGracePeriodSeconds() {
        return prefs.getInt(KEY_SENTRY_GRACE_PERIOD, 5);
    }

    public void setSentryGracePeriodSeconds(int seconds) {
        int clamped = Math.max(3, Math.min(30, seconds));
        prefs.edit().putInt(KEY_SENTRY_GRACE_PERIOD, clamped).apply();
    }

    public float getSentrySensitivityThreshold() {
        return prefs.getFloat(KEY_SENTRY_SENSITIVITY, 1.8f);
    }

    public void setSentrySensitivityThreshold(float threshold) {
        prefs.edit().putFloat(KEY_SENTRY_SENSITIVITY, threshold).apply();
    }
}
