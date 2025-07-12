# Omsetku: Aplikasi Pelacak Barang dan Penjualan
**Omsetku** adalah aplikasi Android yang dirancang untuk membantu Usaha Mikro, Kecil, dan Menengah (UMKM) dalam mengelola inventaris barang dan melacak penjualan. Proyek ini merupakan tugas akhir untuk mata kuliah Pemrograman Perangkat Bergerak. Aplikasi ini dibangun menggunakan bahasa pemrograman **Kotlin** dan memanfaatkan **Firebase Firestore** sebagai database-nya.

## Fitur Utama
Aplikasi ini dilengkapi dengan berbagai fitur untuk memudahkan pengelolaan bisnis:
* **Dashboard Interaktif**: Menampilkan ringkasan informasi penting seperti:
    * Total item yang terdaftar.
    * Ringkasan penjualan (harian, mingguan, bulanan).
    * Grafik visual untuk melacak tren penjualan.
    * Peringatan untuk item dengan stok yang hampir habis.
* **Manajemen Kategori & Produk (CRUD)**: Pengguna dapat dengan mudah menambah, melihat, mengubah, dan menghapus data untuk kategori dan produk.
* **Pencatatan Penjualan Otomatis**: Formulir penjualan yang secara otomatis mengurangi stok barang yang terjual, memastikan data inventaris selalu *up-to-date* setelah setiap transaksi.
* **Laporan Penjualan**: Semua transaksi penjualan dicatat dan dapat dilihat kembali, membantu analisis performa bisnis.
* **Pencarian & Penyaringan**:
    * Cari kategori dan produk berdasarkan nama.
    * Saring laporan penjualan berdasarkan periode waktu tertentu (bulan dan tahun).

## Desain Antarmuka (UI)
Antarmuka Omsetku dirancang dengan tampilan yang menarik dan mudah digunakan:
1.  **Layar Pembuka & Login**: Saat aplikasi dibuka, pengguna akan disambut dengan *splash screen* yang menampilkan logo dan nama aplikasi, diikuti oleh halaman login untuk keamanan.
2.  **Navigasi Utama**: Setelah berhasil login, pengguna akan diarahkan ke **Dashboard**. Navigasi utama terletak di bagian bawah layar dan terdiri dari empat menu: **Dashboard, Kategori, Produk,** dan **Penjualan**.
3.  **Struktur Halaman**: Setiap halaman memiliki *header* yang jelas untuk menunjukkan lokasi pengguna saat ini dan *footer* untuk navigasi.

## Teknologi yang Digunakan
* **Bahasa Pemrograman**: Kotlin
* **Database**: Firebase Firestore
* **Arsitektur**: Menggunakan ViewModel untuk memisahkan logika bisnis dari UI, serta Repository untuk mengelola sumber data.

## Cara Menjalankan Proyek
1.  **Clone Repositori**:
    ```bash
    git clone https://github.com/idrmdhni/MobileProgramming-Omsetku.git
    ```
2.  **Buka di Android Studio**: Buka proyek menggunakan Android Studio.
3.  **Konfigurasi Firebase**: Pastikan file `google-services.json` sudah ada di dalam direktori `app/`.
4.  **Jalankan Aplikasi**: Hubungkan perangkat Android atau gunakan emulator, lalu jalankan aplikasi dari Android Studio.
