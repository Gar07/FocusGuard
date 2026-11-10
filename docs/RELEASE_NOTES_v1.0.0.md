# FocusGuard v1.0.0 — Production Release (UTS Pemrograman Mobile)

**FocusGuard** adalah aplikasi utilitas ganda native Android berbasis sensor perangkat keras (*hardware-aware*) yang menggabungkan manajemen produktivitas belajar (*Flip-to-Focus*) dan sistem alarm keamanan meja belajar (*Desk-Sentry Anti-Theft*).

---

## 🌟 Sorotan Fitur Utama (Key Features)

### 1. Engine 1: Flip-to-Focus (Hardware-Aware Pomodoro)
* **Validasi Fisik 3D:** Mengharuskan pengguna benar-benar menelungkupkan ponsel ke atas meja ($a_z \le -8.5\text{ m/s}^2$ dan jarak sensor *Proximity* $d = 0\text{ cm}$) agar hitungan waktu belajar berjalan.
* **Auto OLED Screen Dimming:** Kecerahan layar otomatis diturunkan hingga 8% saat posisi telungkup untuk menghemat konsumsi daya baterai.
* **Pencatatan Alasan Distraksi:** Jika ponsel diangkat sebelum sesi selesai ($a_z > -5.0\text{ m/s}^2$), timer dijeda dan dialog wajib mencatat alasan interupsi ke basis data SQLite lokal.

### 2. Engine 2: Desk-Sentry Anti-Theft Alarm
* **Perlindungan Meja Publik:** Mengubah ponsel menjadi alarm anti-maling lokal ber-PIN 4-digit saat ditinggal di perpustakaan, kafe, atau ruang kelas.
* **Jeda Kalibrasi 5 Detik:** Memberikan waktu stabilisasi posisi perangkat sebelum sistem bersenjata (*ARMED*).
* **Deteksi Getaran Dinamis 3D:** Menghitung deviasi percepatan resultan dinamis ($\Delta a > 1.8\text{ m/s}^2$). Getaran atau perpindahan sekecil apa pun akan memicu sirine darurat berulang, getaran *waveform*, dan layar berkedip merah.
* **Verifikasi PIN 4-Digit:** Alarm hanya dapat dimatikan dengan memasukkan PIN rahasia pada custom keypad numerik native.

### 3. Arsitektur & Kepatuhan Zero-Library
* **100% Java Native:** Dibangun murni menggunakan API bawaan Android SDK (Min SDK 21 s/d Target SDK 35).
* **0% Pustaka Pihak Ketiga:** Tidak menggunakan pustaka eksternal pihak ketiga apa pun, menghasilkan ukuran file APK sangat kecil (**hanya 5.7 MB**).
* **SQLiteOpenHelper Relasional:** 3 tabel relasional dengan integritas *Foreign Key CASCADE* (`focus_sessions`, `distraction_logs`, `sentry_incident_logs`).

---

## 📦 Artefak Rilis & Verifikasi Integritas

| File Artefak | Keterangan | Ukuran |
| :--- | :--- | :--- |
| **`FocusGuard-v1.0.0.apk`** | File APK debug siap pasang pada perangkat fisik atau emulator | ~5.7 MB |
| **`Panduan_Project_FocusGuard.pdf`** | Dokumen PRD dan panduan arsitektur teknis lengkap | ~141 KB |
| **`Presentasi_FocusGuard.pdf`** | Slide presentasi proyek untuk evaluasi dosen UTS | ~43 KB |

### SHA256 Checksum:
```text
FocusGuard-v1.0.0.apk:
5BC45D8CF46A2137F83BA8C393C1CECB4DAAD935957748A358813C7CEAAE3D7D
```

---

## 🔗 Tautan Dokumen Cloud
* **Google Docs (PRD & Panduan):** [https://docs.google.com/document/d/1t3Hd_1S1vkxu7yhWYbjRagOk1BW-0kDKUo4mkP3NMNA/edit](https://docs.google.com/document/d/1t3Hd_1S1vkxu7yhWYbjRagOk1BW-0kDKUo4mkP3NMNA/edit)
* **Google Slides (Slide Presentasi UTS):** [https://docs.google.com/presentation/d/1lnWGwD6V7e9I7YBV9gm05HLmRkATpytsocBdceVv4s8/edit](https://docs.google.com/presentation/d/1lnWGwD6V7e9I7YBV9gm05HLmRkATpytsocBdceVv4s8/edit)

---

## 📄 Lisensi & Kredit Pembuat
Proyek ini dilisensikan di bawah **MIT License**.  
**Copyright (c) 2026 Gar07 (GPM Pasca)**
