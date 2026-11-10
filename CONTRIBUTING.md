# Panduan Kontribusi FocusGuard (Contributing Guide)

Terima kasih atas ketertarikan Anda untuk berkontribusi pada **FocusGuard**! FocusGuard adalah proyek sumber terbuka (*open-source*) berlisensi MIT yang berfokus pada utilitas produktivitas dan keamanan meja belajar berbasis sensor perangkat keras Android Native (100% Java murni).

---

## 📜 1. Kebijakan Kredit & Lisensi (Attribution Requirement)
FocusGuard dilisensikan di bawah **MIT License**.
* Anda bebas menggunakan, memodifikasi, memperbarui, mencabangkan (*fork*), dan mendistribusikan kode ini untuk keperluan edukasi, portofolio, maupun komersial.
* **Persyaratan Utama:** Segala bentuk salinan, pembaruan, atau modifikasi dari perangkat lunak ini **wajib mencantumkan hak cipta dan kredit asli kepada pembuat proyek:**
  ```text
  Copyright (c) 2026 Gar07 (GPM Pasca)
  Original Repository: https://github.com/Gar07/FocusGuard
  ```

---

## 🛠️ 2. Standar Pengembangan (Development Principles)

Untuk menjaga integritas arsitektur proyek:
1. **0% Third-Party Dependency Policy:**  
   Proyek ini **menolak penambahan library eksternal** yang tidak esensial. Seluruh fitur harus memaksimalkan Android SDK bawaan (`android.hardware.*`, `android.database.sqlite.*`, `android.media.*`, `android.os.*`, `android.content.*`).
2. **Bahasa Pemrograman: Java Native:**  
   Seluruh kode logika aplikasi ditulis dalam **Java** (kompatibel Java 11/17) dengan `minSdk = 21` (Android 5.0 Lollipop) agar tetap dapat dijalankan pada perangkat lama.
3. **Hardware Sensor Aware:**  
   Setiap penanganan sensor wajib mengimplementasikan `SensorEventListener` secara hemat daya (hindari polling loop) dan memastikan `onPause()` / `onDestroy()` mematikan listener untuk mencegah *battery drain* dan *memory leak*.
4. **Unit Testing Wajib:**  
   Setiap penambahan atau modifikasi logika matematis sensor atau validasi keamanan wajib disertai dengan pengujian unit di `app/src/test/java/com/example/focusguard/`.

---

## 🚀 3. Alur Kerja Kontribusi (Workflow)

### Langkah 1: Fork & Clone Repository
```bash
git clone https://github.com/<username-anda>/FocusGuard.git
cd FocusGuard
```

### Langkah 2: Buat Branch Fitur Baru
Gunakan format penamaan branch yang jelas:
```bash
git checkout -b feature/nama-fitur-baru
# atau untuk perbaikan bug:
git checkout -b fix/perbaikan-sensor-tertentu
```

### Langkah 3: Konvensi Pesan Commit (Conventional Commits)
Gunakan format pesan commit terstruktur:
* `feat:` Penambahan fitur atau mekanisme baru
* `fix:` Perbaikan bug atau penanganan sensor
* `docs:` Pembaruan dokumentasi atau panduan
* `style:` Pembersihan format tampilan kode (tanpa perubahan logika)
* `refactor:` Refaktorisasi kode internal
* `test:` Penambahan atau perbaikan unit testing
* `chore:` Pembaruan dependensi SDK atau konfigurasi Gradle

*Contoh:*
```bash
git commit -m "feat(sensor): tambahkan kalibrasi otomatis giroskop untuk sentry mode"
```

### Langkah 4: Uji Coba Lokal & Kompilasi
Pastikan kode lolos pengujian dan dapat terkompilasi sebelum mengajukan Pull Request:
```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

### Langkah 5: Ajukan Pull Request (PR)
1. Push branch Anda ke repository fork Anda di GitHub.
2. Buka Pull Request ke branch `main` pada repository utama `Gar07/FocusGuard`.
3. Lengkapi formulir PR template yang telah disediakan.

---

## 🐛 4. Melaporkan Masalah & Permintaan Fitur
* **Bug Report:** Gunakan template [Bug Report](.github/ISSUE_TEMPLATE/bug_report.md) dan sertakan model perangkat fisik atau spesifikasi emulator serta versi Android saat bug terjadi.
* **Feature Request:** Gunakan template [Feature Request](.github/ISSUE_TEMPLATE/feature_request.md) untuk mendiskusikan inovasi hardware sensor atau fitur baru.

Terima kasih atas kontribusi Anda dalam memajukan FocusGuard!
