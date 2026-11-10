package com.example.focusguard;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.focusguard.database.FocusGuardDBHelper;
import com.example.focusguard.sensor.SensorController;
import com.example.focusguard.util.PreferenceManager;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorController.SensorCallback {

    public static final String EXTRA_MODE = "EXTRA_MODE";
    public static final String EXTRA_FOCUS_MINUTES = "EXTRA_FOCUS_MINUTES";
    public static final String MODE_FOCUS = "FOCUS";
    public static final String MODE_SENTRY = "SENTRY";

    private FocusGuardDBHelper dbHelper;
    private PreferenceManager prefManager;
    private SensorController sensorController;

    private String selectedMode = MODE_FOCUS;
    private int selectedFocusMinutes = 25;
    private float selectedSensitivity = 1.8f;

    private LinearLayout cardModeFocus, cardModeSentry;
    private TextView tvFocusSelectedTag, tvSentrySelectedTag;
    private TextView tvSensorReadiness, tvHomeLiveTelemetry, tvPinStatusBadge, tvSentryFormulaSub;
    private TextView tvStatFocusMin, tvStatSuccessRate, tvStatSentryCount;
    private Button btnDur1, btnDur15, btnDur25, btnDur45;
    private Button btnSensHigh, btnSensNormal, btnSensLow;
    private Button btnStartSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new FocusGuardDBHelper(this);
        dbHelper.seedDemoDataIfEmpty(false);
        prefManager = new PreferenceManager(this);
        sensorController = new SensorController(this, this);

        selectedFocusMinutes = prefManager.getDefaultFocusTimeMinutes();
        selectedSensitivity = prefManager.getSentrySensitivityThreshold();

        bindViews();
        setupListeners();
        updateSensorReadinessBanner();
        updateModeSelectionUI();
        updateDurationButtonsUI();
        updateSensitivityButtonsUI();
    }

    private void bindViews() {
        cardModeFocus = findViewById(R.id.cardModeFocus);
        cardModeSentry = findViewById(R.id.cardModeSentry);
        tvFocusSelectedTag = findViewById(R.id.tvFocusSelectedTag);
        tvSentrySelectedTag = findViewById(R.id.tvSentrySelectedTag);
        tvSensorReadiness = findViewById(R.id.tvSensorReadiness);
        tvHomeLiveTelemetry = findViewById(R.id.tvHomeLiveTelemetry);
        tvPinStatusBadge = findViewById(R.id.tvPinStatusBadge);
        tvSentryFormulaSub = findViewById(R.id.tvSentryFormulaSub);

        tvStatFocusMin = findViewById(R.id.tvStatFocusMin);
        tvStatSuccessRate = findViewById(R.id.tvStatSuccessRate);
        tvStatSentryCount = findViewById(R.id.tvStatSentryCount);

        btnDur1 = findViewById(R.id.btnDur1);
        btnDur15 = findViewById(R.id.btnDur15);
        btnDur25 = findViewById(R.id.btnDur25);
        btnDur45 = findViewById(R.id.btnDur45);

        btnSensHigh = findViewById(R.id.btnSensHigh);
        btnSensNormal = findViewById(R.id.btnSensNormal);
        btnSensLow = findViewById(R.id.btnSensLow);

        btnStartSession = findViewById(R.id.btnStartSession);
    }

    private void setupListeners() {
        cardModeFocus.setOnClickListener(v -> {
            selectedMode = MODE_FOCUS;
            updateModeSelectionUI();
        });

        cardModeSentry.setOnClickListener(v -> {
            selectedMode = MODE_SENTRY;
            updateModeSelectionUI();
        });

        btnDur1.setOnClickListener(v -> selectDuration(1));
        btnDur15.setOnClickListener(v -> selectDuration(15));
        btnDur25.setOnClickListener(v -> selectDuration(25));
        btnDur45.setOnClickListener(v -> selectDuration(45));

        btnSensHigh.setOnClickListener(v -> selectSensitivity(1.2f));
        btnSensNormal.setOnClickListener(v -> selectSensitivity(1.8f));
        btnSensLow.setOnClickListener(v -> selectSensitivity(2.8f));

        ImageButton btnSettingsPin = findViewById(R.id.btnSettingsPin);
        btnSettingsPin.setOnClickListener(v -> showSettingsDialog());

        btnStartSession.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ActiveSessionActivity.class);
            intent.putExtra(EXTRA_MODE, selectedMode);
            intent.putExtra(EXTRA_FOCUS_MINUTES, selectedFocusMinutes);
            startActivity(intent);
        });

        Button btnOpenHistory = findViewById(R.id.btnOpenHistory);
        btnOpenHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });
    }

    private void selectDuration(int minutes) {
        selectedFocusMinutes = minutes;
        prefManager.setDefaultFocusTimeMinutes(minutes);
        selectedMode = MODE_FOCUS;
        updateDurationButtonsUI();
        updateModeSelectionUI();
    }

    private void selectSensitivity(float threshold) {
        selectedSensitivity = threshold;
        prefManager.setSentrySensitivityThreshold(threshold);
        selectedMode = MODE_SENTRY;
        updateSensitivityButtonsUI();
        updateModeSelectionUI();
    }

    private void updateSensorReadinessBanner() {
        boolean hasAccel = sensorController.hasAccelerometer();
        boolean hasProx = sensorController.hasProximitySensor();
        if (hasAccel && hasProx) {
            tvSensorReadiness.setText("SENSOR HARDWARE SIAP (Accelerometer + Proximity)");
            tvSensorReadiness.setTextColor(Color.parseColor("#10B981"));
        } else if (hasAccel) {
            tvSensorReadiness.setText("SENSOR AKTIF (Accelerometer Siap • Mode Hybrid)");
            tvSensorReadiness.setTextColor(Color.parseColor("#06B6D4"));
        } else {
            tvSensorReadiness.setText("MODE SIMULASI EMULATOR AKTIF");
            tvSensorReadiness.setTextColor(Color.parseColor("#F59E0B"));
        }
        String pin = prefManager.getUserPin();
        tvPinStatusBadge.setText("1234".equals(pin) ? "PIN: 1234 (Default)" : "PIN: Kustom (Aktif)");
        tvSentryFormulaSub.setText(String.format(Locale.US,
                "Syarat Trigger: Δa > %.1f m/s² | Jeda Arming: %d dtk",
                selectedSensitivity, prefManager.getSentryGracePeriodSeconds()));
    }

    private void updateModeSelectionUI() {
        boolean isFocus = MODE_FOCUS.equals(selectedMode);
        cardModeFocus.setBackgroundResource(isFocus ? R.drawable.bg_card_selected_focus : R.drawable.bg_card_unselected);
        cardModeSentry.setBackgroundResource(!isFocus ? R.drawable.bg_card_selected_sentry : R.drawable.bg_card_unselected);

        tvFocusSelectedTag.setText(isFocus ? "AKTIF" : "PILIH");
        tvFocusSelectedTag.setTextColor(Color.parseColor(isFocus ? "#10B981" : "#94A3B8"));

        tvSentrySelectedTag.setText(!isFocus ? "AKTIF" : "PILIH");
        tvSentrySelectedTag.setTextColor(Color.parseColor(!isFocus ? "#F59E0B" : "#94A3B8"));

        if (isFocus) {
            btnStartSession.setText("MULAI SESI FOKUS (" + selectedFocusMinutes + " MENIT)");
            btnStartSession.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#10B981")));
        } else {
            btnStartSession.setText("AKTIFKAN DESK-SENTRY ALARM");
            btnStartSession.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F59E0B")));
        }
    }

    private void updateDurationButtonsUI() {
        styleChipButton(btnDur1, selectedFocusMinutes == 1, "#10B981", R.drawable.bg_card_selected_focus);
        styleChipButton(btnDur15, selectedFocusMinutes == 15, "#10B981", R.drawable.bg_card_selected_focus);
        styleChipButton(btnDur25, selectedFocusMinutes == 25, "#10B981", R.drawable.bg_card_selected_focus);
        styleChipButton(btnDur45, selectedFocusMinutes == 45, "#10B981", R.drawable.bg_card_selected_focus);
    }

    private void updateSensitivityButtonsUI() {
        styleChipButton(btnSensHigh, Math.abs(selectedSensitivity - 1.2f) < 0.1f, "#F59E0B", R.drawable.bg_card_selected_sentry);
        styleChipButton(btnSensNormal, Math.abs(selectedSensitivity - 1.8f) < 0.1f, "#F59E0B", R.drawable.bg_card_selected_sentry);
        styleChipButton(btnSensLow, Math.abs(selectedSensitivity - 2.8f) < 0.1f, "#F59E0B", R.drawable.bg_card_selected_sentry);
        tvSentryFormulaSub.setText(String.format(Locale.US,
                "Syarat Trigger: Δa > %.1f m/s² | Jeda Arming: %d dtk",
                selectedSensitivity, prefManager.getSentryGracePeriodSeconds()));
    }

    private void styleChipButton(Button btn, boolean selected, String hexColor, int selectedBgRes) {
        btn.setBackgroundResource(selected ? selectedBgRes : R.drawable.bg_badge_pill);
        btn.setTextColor(Color.parseColor(selected ? hexColor : "#94A3B8"));
    }

    private void refreshStatsSummary() {
        int focusMin = dbHelper.getTotalFocusMinutes();
        int completed = dbHelper.getCompletedSessionCount();
        int sentryCount = dbHelper.getTotalSentryCount();

        tvStatFocusMin.setText(focusMin + " mnt");
        tvStatSuccessRate.setText(completed + " Sesi");
        tvStatSentryCount.setText(sentryCount + " Alert");
    }

    private void showSettingsDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_settings_pin, null);
        EditText etPin = view.findViewById(R.id.etSettingPin);
        EditText etGrace = view.findViewById(R.id.etSettingGrace);
        Button btnCancel = view.findViewById(R.id.btnCancelSettings);
        Button btnSave = view.findViewById(R.id.btnSaveSettings);

        etPin.setText(prefManager.getUserPin());
        etGrace.setText(String.valueOf(prefManager.getSentryGracePeriodSeconds()));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String pin = etPin.getText().toString().trim();
            String graceStr = etGrace.getText().toString().trim();
            if (!PreferenceManager.isValidPin(pin)) {
                Toast.makeText(this, "PIN harus tepat 4 digit angka!", Toast.LENGTH_SHORT).show();
                return;
            }
            int grace = 5;
            try {
                grace = Integer.parseInt(graceStr);
            } catch (NumberFormatException ignored) {
            }
            prefManager.setUserPin(pin);
            prefManager.setSentryGracePeriodSeconds(grace);
            updateSensorReadinessBanner();
            Toast.makeText(this, "Konfigurasi PIN & Sensor disimpan!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatsSummary();
        sensorController.startListening(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorController.stopListening();
    }

    @Override
    public void onDeviceFaceDown() {
        // Ditampilkan secara visual pada telemetri beranda
    }

    @Override
    public void onDeviceFaceUp() {
    }

    @Override
    public void onMotionDetected(double deltaMovement) {
    }

    @Override
    public void onTelemetryUpdated(float x, float y, float z, float proximityCm, double delta) {
        String pos = (z <= SensorController.FACE_DOWN_Z_THRESHOLD) ? "Telungkup" : "Tegak/Meja";
        tvHomeLiveTelemetry.setText(String.format(Locale.US,
                "X: %+.2f | Y: %+.2f | Z: %+.2f m/s² | Prox: %.1f cm (%s)",
                x, y, z, proximityCm, pos));
    }
}
