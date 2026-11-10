# DOKUMEN RANCANGAN PRODUK (PRD) & PANDUAN LENGKAP PROYEK: FOCUSGUARD

**Mata Kuliah:** Pemrograman Mobile (Proyek UTS)  
**Nama Aplikasi:** FocusGuard — Dual-Engine Hardware-Aware Focus & Desk-Sentry Utility  
**Package ID:** `com.example.focusguard`  
**Bahasa Pemrograman:** 100% Java Native  
**Spesifikasi SDK:** `minSdk = 21` (Android 5.0 Lollipop), `targetSdk = 35`, `compileSdk = 37`  
**Kebijakan Pustaka:** 0% Pustaka Pihak Ketiga Eksternal (Murni Android SDK & AndroidX UI Dasar)

---

## 1. Ringkasan Eksekutif & Latar Belakang Masalah

### 1.1 Masalah Utama Mahasiswa
Mahasiswa yang belajar di ruang publik seperti perpustakaan kampus, ruang baca, laboratorium, maupun kafe menghadapi dua permasalahan utama:
1. **Distraksi Digital Impulsif (*Continuous Partial Attention*):** Kebiasaan mengangkat ponsel setiap beberapa menit hanya untuk memeriksa notifikasi media sosial atau pesan singkat yang memecah konsentrasi belajar mendalam (*deep work*).
2. **Keamanan Fisik Perangkat di Meja Publik:** Rasa khawatir ketika harus meninggalkan meja sejenak (misalnya mengambil referensi buku di rak, ke toilet, atau memesan minuman) sementara ponsel berada di atas meja.

### 1.2 Solusi: FocusGuard Dual-Engine
**FocusGuard** menyatukan dua mesin berbasis sensor perangkat keras (*hardware-aware*) dalam satu aplikasi ringan:
- **Engine 1 — Flip-to-Focus:** Memastikan pengguna benar-benar menelungkupkan layar ponsel ke permukaan meja ($a_z \le -8.5\text{ m/s}^2$ dan jarak sensor *proximity* $d = 0\text{ cm}$) agar waktu hitung mundur (*Pomodoro*) berjalan. Ketika ponsel terangkat sebelum sesi selesai ($a_z > -5.0\text{ m/s}^2$), timer otomatis dijeda dan muncul dialog pencatatan alasan distraksi ke database SQLite.
- **Engine 2 — Desk-Sentry Anti-Theft:** Mengubah ponsel menjadi alarm keamanan lokal ber-PIN 4 digit. Setelah jeda stabilisasi 5 detik di atas meja, sistem memantau deviasi percepatan tiga sumbu ($\Delta a$). Jika terjadi pergerakan atau getaran melebihi ambang sensitivitas ($\Delta a > \tau$), sirine darurat berbunyi kencang, layar berkedip merah, insiden dicatat ke SQLite, dan alarm hanya dapat dimatikan menggunakan PIN rahasia.

---

## 2. Arsitektur Komponen Native

Aplikasi dirancang mengikuti prinsip efisiensi memori dan kompatibilitas perangkat luas (`minSdk 21` mencakup >99.8% perangkat Android aktif):

| Komponen Sistem | API Android Native | Kelas Implementasi Proyek |
| :--- | :--- | :--- |
| Pemrosesan Sensor Fisik | `SensorManager`, `SensorEventListener`, `TYPE_ACCELEROMETER`, `TYPE_PROXIMITY` | `com.example.focusguard.sensor.SensorController` |
| Basis Data Relasional Lokal | `SQLiteOpenHelper`, `SQLiteDatabase`, `Cursor` | `com.example.focusguard.database.FocusGuardDBHelper` |
| Penyimpanan Konfigurasi & PIN | `SharedPreferences` | `com.example.focusguard.util.PreferenceManager` |
| Sirine Audio & Haptik | `RingtoneManager`, `ToneGenerator`, `Vibrator`, `VibrationEffect` | `com.example.focusguard.util.AlarmAlertManager` |
| Pewaktu & State Machine | `CountDownTimer`, `WindowManager.LayoutParams` (OLED Dimming) | `com.example.focusguard.ActiveSessionActivity` |
| Antarmuka & Riwayat Data | `AppCompatActivity`, `RecyclerView`, `AlertDialog` | `MainActivity`, `HistoryActivity`, `HistoryAdapter` |

---

## 3. Formulasi Matematis Sensor & State Machine

### 3.1 Deteksi Orientasi Telungkup (Mode Fokus)
Ponsel dinyatakan berada dalam posisi telungkup di atas meja apabila memenuhi dua kondisi fisik secara bersamaan:
1. Percepatan gravitasi pada sumbu vertikal $Z$ bernilai negatif mendekati $-g$:
   $$a_z \le -8.5 \text{ m/s}^2$$
2. Sensor jarak (*Proximity*) mendeteksi objek tepat di depan layar:
   $$d = 0.0 \text{ cm} \quad (d < d_{\max})$$

Apabila pengguna mengangkat ponsel hingga kemiringan sumbu $Z$ melewati batas atas ($a_z > -5.0 \text{ m/s}^2$), status segera beralih dari `FOCUS_IN_SESSION` menjadi `FOCUS_DISTRACTED`.

### 3.2 Deteksi Pergerakan Anti-Maling (Mode Desk-Sentry)
Pada mode Desk-Sentry, sistem menghitung perubahan vektor percepatan antar-siklus ($\Delta \vec{a}$) serta simpangan magnitudo resultan terhadap percepatan gravitasi bumi ($g = 9.80665 \text{ m/s}^2$):

$$\Delta a_{\text{vektor}} = \sqrt{(a_x - a_{x,\text{prev}})^2 + (a_y - a_{y,\text{prev}})^2 + (a_z - a_{z,\text{prev}})^2}$$

$$\Delta a_{\text{gravitasi}} = \left| \sqrt{a_x^2 + a_y^2 + a_z^2} - g \right|$$

$$\Delta a = \max(\Delta a_{\text{vektor}}, \Delta a_{\text{gravitasi}})$$

Jika $\Delta a > \tau$ (dengan pilihan ambang sensitivitas $\tau \in \{1.2, 1.8, 2.8\} \text{ m/s}^2$), sistem langsung memicu status `SENTRY_ALARM_ACTIVE`.

---

## 4. Desain Basis Data Relasional (SQLite)

Database `focus_guard.db` dikelola oleh `FocusGuardDBHelper` dengan tiga tabel relasional:

### 4.1 Tabel `focus_sessions`
Menyimpan riwayat sesi belajar:
- `id` (`INTEGER PRIMARY KEY AUTOINCREMENT`)
- `start_timestamp` (`INTEGER NOT NULL`) — Waktu mulai sesi (epoch ms)
- `target_duration_minutes` (`INTEGER NOT NULL`) — Target durasi belajar (1, 15, 25, 45 menit)
- `actual_duration_seconds` (`INTEGER NOT NULL`) — Realisasi waktu fokus aktual dalam detik
- `status` (`TEXT NOT NULL`) — `'COMPLETED'` atau `'INTERRUPTED'`

### 4.2 Tabel `distraction_logs`
Menyimpan alasan mengapa pengguna mengangkat ponsel di tengah sesi fokus:
- `id` (`INTEGER PRIMARY KEY AUTOINCREMENT`)
- `session_id` (`INTEGER NOT NULL`) — Foreign Key ke `focus_sessions(id) ON DELETE CASCADE`
- `distraction_reason` (`TEXT NOT NULL`) — Alasan interupsi (Notifikasi, Medsos, Bosan, Panggilan, dll.)
- `occurred_at_second` (`INTEGER NOT NULL`) — Detik ke-berapa distraksi terjadi sejak sesi dimulai

### 4.3 Tabel `sentry_incident_logs`
Menyimpan catatan insiden keamanan saat mode penjaga meja aktif:
- `id` (`INTEGER PRIMARY KEY AUTOINCREMENT`)
- `timestamp` (`INTEGER NOT NULL`) — Waktu terjadinya gerakan mencurigakan
- `incident_type` (`TEXT NOT NULL`) — `'UNAUTHORIZED_MOVEMENT'` beserta nilai puncak $\Delta a$
- `resolved_by_pin` (`INTEGER NOT NULL`) — `1` jika berhasil dimatikan dengan PIN, `0` jika belum terverifikasi

---

## 5. Panduan Penggunaan & Skenario Demo Pengujian (UTS)

### 5.1 Pengujian pada Perangkat Fisik maupun Emulator
Aplikasi dilengkapi dengan **Live Telemetry Monitor** (menampilkan nilai $X, Y, Z, \text{Proximity}, \Delta a$ secara *real-time*) serta **Panel Uji Interaktif / Simulasi Sensor** pada layar sesi aktif sehingga penguji dapat mendemonstrasikan seluruh transisi *State Machine* baik dengan membalikkan ponsel fisik di meja penguji maupun melalui tombol simulasi pada Android Emulator.

### 5.2 Langkah Skenario Demo 3 Menit
1. **Beranda & Kesiapan Sensor (30 Detik):** Tunjukkan indikator kesiapan sensor *Accelerometer & Proximity*, telemetri langsung di beranda, serta pengaturan PIN 4-digit (`SharedPreferences`).
2. **Demo Flip-to-Focus (60 Detik):** Pilih durasi fokus, tekan **Mulai Sesi Fokus**, lalu telungkupkan ponsel. Tunjukkan fitur *OLED Screen Dimming* aktif dan timer berjalan. Angkat ponsel untuk memicu getaran peringatan dan **Dialog Pencatatan Alasan Distraksi**, pilih alasan, lalu simpan.
3. **Demo Desk-Sentry Anti-Theft (60 Detik):** Pilih **Mode Penjaga Meja**, tekan **Aktifkan Desk-Sentry Alarm**, tunggu 5 detik masa kalibrasi hingga berstatus `ARMED`. Gerakkan ponsel untuk memicu sirine darurat, layar berkedip merah, dan matikan alarm menggunakan **Keypad PIN 4-Digit** (`1234`).
4. **Verifikasi Riwayat SQLite (30 Detik):** Buka halaman **Riwayat & Log Database SQLite**, perlihatkan Tab 1 (Sesi Fokus & Alasan Distraksi) dan Tab 2 (Log Insiden Sentry) yang tersimpan secara permanen.
