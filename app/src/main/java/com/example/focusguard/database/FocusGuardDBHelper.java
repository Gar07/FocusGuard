package com.example.focusguard.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.focusguard.adapter.HistoryAdapter.HistoryItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FocusGuardDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "focus_guard.db";
    private static final int DATABASE_VERSION = 2;

    // Tabel 1: Focus Session
    public static final String TABLE_FOCUS = "focus_sessions";
    public static final String COL_FOCUS_ID = "id";
    public static final String COL_START_TIME = "start_timestamp";
    public static final String COL_TARGET_MIN = "target_duration_minutes";
    public static final String COL_ACTUAL_SEC = "actual_duration_seconds";
    public static final String COL_STATUS = "status"; // 'COMPLETED' atau 'INTERRUPTED'

    // Tabel 2: Log Catatan Distraksi
    public static final String TABLE_DISTRACTION = "distraction_logs";
    public static final String COL_DIST_ID = "id";
    public static final String COL_SESSION_REF = "session_id";
    public static final String COL_REASON = "distraction_reason";
    public static final String COL_ELAPSED_SEC = "occurred_at_second";

    // Tabel 3: Log Insiden Keamanan (Sentry Alerts)
    public static final String TABLE_SENTRY = "sentry_incident_logs";
    public static final String COL_SENTRY_ID = "id";
    public static final String COL_SENTRY_TIME = "timestamp";
    public static final String COL_INCIDENT_TYPE = "incident_type"; // 'UNAUTHORIZED_MOVEMENT'
    public static final String COL_RESOLVED_PIN = "resolved_by_pin"; // 1 = Berhasil Disarm, 0 = Paksa/Baterai

    public FocusGuardDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createFocusTable = "CREATE TABLE " + TABLE_FOCUS + " (" +
                COL_FOCUS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_START_TIME + " INTEGER NOT NULL, " +
                COL_TARGET_MIN + " INTEGER NOT NULL, " +
                COL_ACTUAL_SEC + " INTEGER NOT NULL, " +
                COL_STATUS + " TEXT NOT NULL)";

        String createDistractionTable = "CREATE TABLE " + TABLE_DISTRACTION + " (" +
                COL_DIST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_SESSION_REF + " INTEGER NOT NULL, " +
                COL_REASON + " TEXT NOT NULL, " +
                COL_ELAPSED_SEC + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + COL_SESSION_REF + ") REFERENCES " + TABLE_FOCUS + "(" + COL_FOCUS_ID + ") ON DELETE CASCADE)";

        String createSentryTable = "CREATE TABLE " + TABLE_SENTRY + " (" +
                COL_SENTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_SENTRY_TIME + " INTEGER NOT NULL, " +
                COL_INCIDENT_TYPE + " TEXT NOT NULL, " +
                COL_RESOLVED_PIN + " INTEGER NOT NULL)";

        db.execSQL(createFocusTable);
        db.execSQL(createDistractionTable);
        db.execSQL(createSentryTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DISTRACTION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOCUS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SENTRY);
        onCreate(db);
    }

    public long insertFocusSession(long startTime, int targetMin, int actualSec, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_START_TIME, startTime);
        values.put(COL_TARGET_MIN, targetMin);
        values.put(COL_ACTUAL_SEC, actualSec);
        values.put(COL_STATUS, status);
        return db.insert(TABLE_FOCUS, null, values);
    }

    public void updateFocusSession(long sessionId, int actualSec, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ACTUAL_SEC, actualSec);
        values.put(COL_STATUS, status);
        db.update(TABLE_FOCUS, values, COL_FOCUS_ID + " = ?", new String[]{String.valueOf(sessionId)});
    }

    public void insertDistraction(long sessionId, String reason, int elapsedSec) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SESSION_REF, sessionId);
        values.put(COL_REASON, reason);
        values.put(COL_ELAPSED_SEC, elapsedSec);
        db.insert(TABLE_DISTRACTION, null, values);
    }

    public long insertSentryIncident(long timestamp, String incidentType, int resolvedByPin) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SENTRY_TIME, timestamp);
        values.put(COL_INCIDENT_TYPE, incidentType);
        values.put(COL_RESOLVED_PIN, resolvedByPin);
        return db.insert(TABLE_SENTRY, null, values);
    }

    public void resolveSentryIncident(long incidentId, int resolvedByPin) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_RESOLVED_PIN, resolvedByPin);
        db.update(TABLE_SENTRY, values, COL_SENTRY_ID + " = ?", new String[]{String.valueOf(incidentId)});
    }

    public List<HistoryItem> getFocusHistoryItems() {
        List<HistoryItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm 'WIB'", Locale.getDefault());

        Cursor cursor = db.query(TABLE_FOCUS, null, null, null, null, null, COL_START_TIME + " DESC");
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_FOCUS_ID));
                long startTs = cursor.getLong(cursor.getColumnIndexOrThrow(COL_START_TIME));
                int targetMin = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TARGET_MIN));
                int actualSec = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ACTUAL_SEC));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS));

                List<String> reasons = getDistractionsForSession(db, id);
                int min = actualSec / 60;
                int sec = actualSec % 60;

                String title = "Sesi Fokus #" + id + " (" + targetMin + " Menit)";
                String timeStr = sdf.format(new Date(startTs));
                String subtitle = String.format(Locale.getDefault(),
                        "Target: %d mnt | Durasi Riil: %dm %02ds", targetMin, min, sec);

                String detail;
                if (reasons.isEmpty()) {
                    detail = "Distraksi: 0x (Fokus Penuh Tanpa Angkat HP)";
                } else {
                    StringBuilder sb = new StringBuilder("Distraksi (" + reasons.size() + "x): ");
                    for (int i = 0; i < reasons.size(); i++) {
                        if (i > 0) sb.append(" • ");
                        sb.append(reasons.get(i));
                    }
                    detail = sb.toString();
                }

                list.add(new HistoryItem(title, status, timeStr, subtitle, detail, "COMPLETED".equals(status) ? 0 : 1));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    private List<String> getDistractionsForSession(SQLiteDatabase db, long sessionId) {
        List<String> reasons = new ArrayList<>();
        Cursor c = db.query(TABLE_DISTRACTION,
                new String[]{COL_REASON, COL_ELAPSED_SEC},
                COL_SESSION_REF + " = ?",
                new String[]{String.valueOf(sessionId)},
                null, null, COL_ELAPSED_SEC + " ASC");
        if (c.moveToFirst()) {
            do {
                String r = c.getString(0);
                int sec = c.getInt(1);
                reasons.add(r + " (@" + (sec / 60) + "m" + (sec % 60) + "s)");
            } while (c.moveToNext());
        }
        c.close();
        return reasons;
    }

    public List<HistoryItem> getSentryHistoryItems() {
        List<HistoryItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss 'WIB'", Locale.getDefault());

        Cursor cursor = db.query(TABLE_SENTRY, null, null, null, null, null, COL_SENTRY_TIME + " DESC");
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_SENTRY_ID));
                long ts = cursor.getLong(cursor.getColumnIndexOrThrow(COL_SENTRY_TIME));
                String type = cursor.getString(cursor.getColumnIndexOrThrow(COL_INCIDENT_TYPE));
                int resolved = cursor.getInt(cursor.getColumnIndexOrThrow(COL_RESOLVED_PIN));

                String title = "Sentry Alert #" + id;
                String badge = resolved == 1 ? "DISARMED (PIN)" : "UNRESOLVED ALERT";
                String timeStr = sdf.format(new Date(ts));
                String subtitle = "Insiden: " + type;
                String detail = resolved == 1
                        ? "Status: Sirine dimatikan secara sah menggunakan verifikasi 4-Digit PIN."
                        : "Status: Alarm terpicu karena pergerakan meja melebihi ambang batas (Δa > τ).";

                list.add(new HistoryItem(title, badge, timeStr, subtitle, detail, 2));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public int getTotalFocusMinutes() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT SUM(" + COL_ACTUAL_SEC + ") FROM " + TABLE_FOCUS, null);
        int totalSec = 0;
        if (c.moveToFirst()) {
            totalSec = c.getInt(0);
        }
        c.close();
        return Math.max(0, totalSec / 60);
    }

    public int getCompletedSessionCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_FOCUS + " WHERE " + COL_STATUS + " = 'COMPLETED'", null);
        int count = 0;
        if (c.moveToFirst()) {
            count = c.getInt(0);
        }
        c.close();
        return count;
    }

    public int getTotalDistractionCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_DISTRACTION, null);
        int count = 0;
        if (c.moveToFirst()) {
            count = c.getInt(0);
        }
        c.close();
        return count;
    }

    public int getTotalSentryCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_SENTRY, null);
        int count = 0;
        if (c.moveToFirst()) {
            count = c.getInt(0);
        }
        c.close();
        return count;
    }

    public void seedDemoDataIfEmpty(boolean force) {
        if (!force && (getCompletedSessionCount() > 0 || getTotalSentryCount() > 0)) {
            return;
        }
        long now = System.currentTimeMillis();
        long s1 = insertFocusSession(now - 7200_000L, 25, 1500, "COMPLETED");
        insertDistraction(s1, "Cek Notifikasi Masuk (Chat / Email)", 480);

        long s2 = insertFocusSession(now - 3600_000L, 25, 840, "INTERRUPTED");
        insertDistraction(s2, "Impuls Membuka Media Sosial", 390);
        insertDistraction(s2, "Bosan / Lelah Belajar", 835);

        insertFocusSession(now - 1800_000L, 15, 900, "COMPLETED");

        insertSentryIncident(now - 5400_000L, "UNAUTHORIZED_MOVEMENT (Δa = 3.42 m/s²)", 1);
        insertSentryIncident(now - 900_000L, "UNAUTHORIZED_MOVEMENT (Δa = 2.65 m/s²)", 1);
    }

    public void clearAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DISTRACTION, null, null);
        db.delete(TABLE_FOCUS, null, null);
        db.delete(TABLE_SENTRY, null, null);
    }
}
