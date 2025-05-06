package com.example.kasirku

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

// Activity untuk halaman Dashboard
class DashboardActivity : AppCompatActivity() {

    // Deklarasi komponen UI
    private lateinit var textJumlahSaldo: TextView // TextView untuk menampilkan jumlah saldo
    private lateinit var iconVisibility: ImageView // ImageView untuk ikon mata (show/hide saldo)
    private lateinit var drawerLayout: DrawerLayout // DrawerLayout untuk menampung Navigation Drawer

    // Variabel untuk mengatur status visibilitas saldo
    private var isSaldoVisible = true
    private val saldoAsli = "Rp 1.000.000.000" // Nilai asli saldo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true) // Untuk mencegah konten terhalang status bar
        setContentView(R.layout.activity_dashboard) // Menetapkan layout activity_dashboard.xml

        // Setup Toolbar dan Drawer
        drawerLayout = findViewById(R.id.main) // Menemukan DrawerLayout dengan id 'main'
        val toolbar = findViewById<Toolbar>(R.id.toolbar) // Menemukan Toolbar dengan id 'toolbar'
        setSupportActionBar(toolbar) // Mengatur toolbar sebagai ActionBar
        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START) // Membuka Navigation Drawer saat toolbar dipencet
        }

        // Setup Navigation Drawer
        val navigationView = findViewById<NavigationView>(R.id.nav_view) // Menemukan NavigationView dengan id 'nav_view'
        navigationView.setCheckedItem(R.id.nav_dashboard) // Menandai item Dashboard sebagai yang terpilih

        // Menangani aksi saat item dalam Navigation Drawer diklik
        navigationView.setNavigationItemSelectedListener { menuItem ->
            drawerLayout.closeDrawer(GravityCompat.START) // Menutup drawer setelah item dipilih

            when (menuItem.itemId) {
                // Jika item laporan dipilih, buka ReportActivity
                R.id.nav_laporan -> {
                    startActivity(Intent(this, ReportActivity::class.java)) // Memulai activity ReportActivity
                    true // Menandakan bahwa aksi berhasil diproses
                }
                // Tambahkan menu lainnya jika diperlukan
                else -> false // Menandakan tidak ada aksi
            }
        }

        // Inisialisasi tampilan saldo dan ikon mata
        textJumlahSaldo = findViewById(R.id.textJumlahSaldo) // Menemukan TextView saldo dengan id 'textJumlahSaldo'
        iconVisibility = findViewById(R.id.iconVisibility) // Menemukan ImageView dengan id 'iconVisibility'

        // Event klik ikon visibilitas saldo
        iconVisibility.setOnClickListener {
            isSaldoVisible = !isSaldoVisible // Toggle status visibilitas saldo
            if (isSaldoVisible) {
                textJumlahSaldo.text = saldoAsli // Menampilkan saldo asli
                iconVisibility.setImageResource(R.drawable.hide) // Ganti ikon ke "hide" (mata tertutup)
            } else {
                textJumlahSaldo.text = "********" // Menyembunyikan saldo dengan asterisks
                iconVisibility.setImageResource(R.drawable.show) // Ganti ikon ke "show" (mata terbuka)
            }
        }
    }

    // Jika masih ingin menggunakan tombol dari layout (misalnya, tombol di layout XML untuk membuka ReportActivity)
    fun openReportActivity(view: View) {
        val intent = Intent(this, ReportActivity::class.java) // Membuat intent untuk membuka ReportActivity
        startActivity(intent) // Memulai ReportActivity
    }
}
