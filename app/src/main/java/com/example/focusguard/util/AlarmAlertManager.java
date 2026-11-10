package com.example.focusguard.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;

/**
 * Pengendali sirine alarm dan umpan balik haptik native.
 */
public class AlarmAlertManager {

    private Ringtone ringtone;
    private final Vibrator vibrator;
    private ToneGenerator toneGenerator;
    private final Handler sirenHandler = new Handler(Looper.getMainLooper());
    private boolean isAlarmRunning = false;

    private final Runnable sirenLoopRunnable = new Runnable() {
        private boolean highPitch = true;

        @Override
        public void run() {
            if (!isAlarmRunning) return;
            try {
                if (toneGenerator != null) {
                    int tone = highPitch ? ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK : ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD;
                    toneGenerator.startTone(tone, 350);
                    highPitch = !highPitch;
                }
                if (ringtone != null && !ringtone.isPlaying()) {
                    ringtone.play();
                }
            } catch (Exception ignored) {
            }
            sirenHandler.postDelayed(this, 450);
        }
    };

    public AlarmAlertManager(Context context) {
        try {
            Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (notificationUri == null) {
                notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
            this.ringtone = RingtoneManager.getRingtone(context, notificationUri);
        } catch (Exception e) {
            this.ringtone = null;
        }

        try {
            this.toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        } catch (Exception e) {
            this.toneGenerator = null;
        }

        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void triggerAlarm() {
        if (!isAlarmRunning) {
            isAlarmRunning = true;
            try {
                if (ringtone != null && !ringtone.isPlaying()) {
                    ringtone.play();
                }
            } catch (Exception ignored) {
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                long[] pattern = {0, 500, 200, 500};
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
                } else {
                    vibrator.vibrate(pattern, 0);
                }
            }
            sirenHandler.post(sirenLoopRunnable);
        }
    }

    public void stopAlarm() {
        if (isAlarmRunning) {
            isAlarmRunning = false;
            sirenHandler.removeCallbacks(sirenLoopRunnable);
            try {
                if (ringtone != null && ringtone.isPlaying()) {
                    ringtone.stop();
                }
                if (toneGenerator != null) {
                    toneGenerator.stopTone();
                }
            } catch (Exception ignored) {
            }
            if (vibrator != null) {
                vibrator.cancel();
            }
        }
    }

    public void vibrateShort(long durationMs) {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(durationMs);
            }
        }
    }

    public void playSuccessBeep() {
        try {
            if (toneGenerator != null) {
                toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 250);
            }
        } catch (Exception ignored) {
        }
        vibrateShort(150);
    }

    public boolean isAlarmRunning() {
        return isAlarmRunning;
    }

    public void release() {
        stopAlarm();
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
    }
}
