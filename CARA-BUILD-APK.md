# CARA BUILD APK - EXAM BROWSER

## Persyaratan
- Android Studio (versi Flamingo 2022.2.1 atau lebih baru)
- JDK 11 atau lebih baru

## Langkah Build APK

### 1. Buka Proyek di Android Studio
- Buka Android Studio
- Pilih **File > Open**
- Arahkan ke folder: `H:\EXAM BROWSER\Exambrowser_Client_Android`
- Klik **OK** dan tunggu Gradle sync selesai (butuh koneksi internet pertama kali)

### 2. Build Debug APK (untuk testing)
- Menu: **Build > Build Bundle(s) / APK(s) > Build APK(s)**
- Tunggu proses selesai
- APK ada di: `app/build/outputs/apk/debug/app-debug.apk`

### 3. Build Release APK (untuk distribusi)
- Menu: **Build > Generate Signed Bundle / APK**
- Pilih **APK** > Next
- Buat keystore baru atau gunakan yang ada
- Pilih **release** build variant
- Klik **Finish**
- APK ada di: `app/build/outputs/apk/release/app-release.apk`

### 4. Install ke Perangkat Android
Cara 1 - Via Android Studio:
- Hubungkan HP ke PC via USB
- Aktifkan Developer Options dan USB Debugging di HP
- Klik tombol **Run** (segitiga hijau) di Android Studio

Cara 2 - Manual (pindah file APK):
- Copy file APK ke HP
- Di HP: Pengaturan > Keamanan > Aktifkan "Sumber Tidak Dikenal"
- Buka file APK dan install

---

## Fitur Aplikasi

| Fitur | Keterangan |
|-------|-----------|
| Kiosk Mode | Layar terkunci (Android 5.0+), tidak bisa minimize |
| Blokir Screenshot | FLAG_SECURE aktif, mencegah tangkapan layar |
| Blokir Copy-Paste | Disabled via JS injection + override long click |
| Fullscreen | Immersive mode, status bar dan nav bar tersembunyi |
| Whitelist URL | Hanya URL yang diizinkan yang bisa diakses |
| PIN Keluar | Butuh PIN untuk keluar dari ujian |
| PIN Admin | Akses pengaturan via PIN admin |
| Auto-start | Otomatis jalan saat HP dinyalakan |
| Multi Android | Support Android 4.1 (API 16) sampai Android 13 (API 33) |

---

## Cara Penggunaan

### Setup Pertama (Admin)
1. Buka aplikasi Exam Browser
2. Layar konfigurasi akan muncul otomatis
3. Isi:
   - **URL Ujian**: alamat website ujian (misal: `https://ujian.sekolah.ac.id`)
   - **URL yang Diizinkan**: domain yang boleh diakses (satu per baris)
   - **PIN Keluar**: PIN untuk peserta/pengawas keluar ujian (min. 4 digit)
   - **PIN Admin**: PIN untuk mengubah pengaturan (min. 4 digit)
4. Klik **SIMPAN DAN MULAI**

### Saat Ujian Berlangsung
- Browser otomatis buka URL ujian dalam mode fullscreen
- Semua tombol (back, home, recent apps) diblokir
- Screenshot diblokir
- Copy-paste diblokir
- Hanya URL yang diizinkan yang bisa diakses

### Keluar dari Ujian
- Tekan tombol **Back**
- Pilih **Keluar Ujian**
- Masukkan **PIN Keluar**

### Ubah Pengaturan (Admin)
- Di layar ujian, ketuk tulisan **EXAM MODE** sebanyak 5x cepat
- Masukkan **PIN Admin**
- Ubah pengaturan yang diperlukan
- Klik **SIMPAN**

---

## Konfigurasi Kiosk Mode Penuh (Opsional)

Untuk kiosk mode yang lebih kuat (tidak bisa keluar sama sekali tanpa PIN),
aktifkan sebagai **Device Owner** menggunakan ADB:

```bash
adb shell dpm set-device-owner com.exambrowser/.ExamDeviceAdminReceiver
```

> **Catatan**: Perintah ini hanya bisa dilakukan sebelum ada akun Google yang login
> dan membutuhkan USB Debugging aktif.

---

## Default PIN
- PIN Keluar: `1234`
- PIN Admin: `admin123`

**GANTI PIN INI SEBELUM DIGUNAKAN!**
