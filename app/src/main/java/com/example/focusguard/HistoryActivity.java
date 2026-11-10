package com.example.focusguard;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.focusguard.adapter.HistoryAdapter;
import com.example.focusguard.adapter.HistoryAdapter.HistoryItem;
import com.example.focusguard.database.FocusGuardDBHelper;

import java.util.List;

/**
 * Activity Riwayat yang menampilkan data relasional SQLite:
 * - Tab 1: Sesi Fokus & Catatan Distraksi (focus_sessions JOIN distraction_logs)
 * - Tab 2: Insiden Keamanan Desk-Sentry (sentry_incident_logs)
 */
public class HistoryActivity extends AppCompatActivity {

    private FocusGuardDBHelper dbHelper;
    private HistoryAdapter adapter;
    private boolean showingFocusTab = true;

    private TextView tvHistTotalFocus, tvHistTotalDistractions, tvHistTotalSentry, tvEmptyHistory;
    private Button btnTabFocus, btnTabSentry;
    private RecyclerView rvHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        dbHelper = new FocusGuardDBHelper(this);
        adapter = new HistoryAdapter();

        tvHistTotalFocus = findViewById(R.id.tvHistTotalFocus);
        tvHistTotalDistractions = findViewById(R.id.tvHistTotalDistractions);
        tvHistTotalSentry = findViewById(R.id.tvHistTotalSentry);
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory);

        btnTabFocus = findViewById(R.id.btnTabFocus);
        btnTabSentry = findViewById(R.id.btnTabSentry);
        rvHistory = findViewById(R.id.rvHistory);

        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(adapter);

        findViewById(R.id.btnBackHistory).setOnClickListener(v -> finish());

        findViewById(R.id.btnSeedDemoData).setOnClickListener(v -> {
            dbHelper.seedDemoDataIfEmpty(true);
            refreshHistoryView();
            Toast.makeText(this, "Data sampel sesi fokus & sentry ditambahkan!", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnClearHistory).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Reset Database SQLite?")
                    .setMessage("Seluruh riwayat sesi fokus, alasan distraksi, dan log insiden sentry akan dihapus.")
                    .setPositiveButton("Hapus Semua", (d, w) -> {
                        dbHelper.clearAllData();
                        refreshHistoryView();
                        Toast.makeText(this, "Database berhasil dikosongkan.", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });

        btnTabFocus.setOnClickListener(v -> {
            showingFocusTab = true;
            refreshHistoryView();
        });

        btnTabSentry.setOnClickListener(v -> {
            showingFocusTab = false;
            refreshHistoryView();
        });

        refreshHistoryView();
    }

    private void refreshHistoryView() {
        tvHistTotalFocus.setText(dbHelper.getTotalFocusMinutes() + " mnt");
        tvHistTotalDistractions.setText(dbHelper.getTotalDistractionCount() + " Log");
        tvHistTotalSentry.setText(dbHelper.getTotalSentryCount() + " Alert");

        if (showingFocusTab) {
            btnTabFocus.setBackgroundResource(R.drawable.bg_card_selected_focus);
            btnTabFocus.setTextColor(Color.parseColor("#10B981"));
            btnTabSentry.setBackgroundResource(R.drawable.bg_card_unselected);
            btnTabSentry.setTextColor(Color.parseColor("#94A3B8"));
        } else {
            btnTabFocus.setBackgroundResource(R.drawable.bg_card_unselected);
            btnTabFocus.setTextColor(Color.parseColor("#94A3B8"));
            btnTabSentry.setBackgroundResource(R.drawable.bg_card_selected_sentry);
            btnTabSentry.setTextColor(Color.parseColor("#F59E0B"));
        }

        List<HistoryItem> items = showingFocusTab
                ? dbHelper.getFocusHistoryItems()
                : dbHelper.getSentryHistoryItems();

        adapter.setItems(items);
        if (items.isEmpty()) {
            rvHistory.setVisibility(View.GONE);
            tvEmptyHistory.setVisibility(View.VISIBLE);
        } else {
            rvHistory.setVisibility(View.VISIBLE);
            tvEmptyHistory.setVisibility(View.GONE);
        }
    }
}
