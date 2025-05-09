package com.example.kasirku.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.kasirku.R
import com.example.kasirku.base.BaseActivity
import com.google.android.material.navigation.NavigationView

// Activity untuk halaman Dashboard
class DashboardActivity : BaseActivity() {

    // Deklarasi komponen UI
    private lateinit var textJumlahSaldo: TextView // TextView untuk menampilkan jumlah saldo
    private lateinit var iconVisibility: ImageView // ImageView untuk ikon mata (show/hide saldo)

    // Variabel untuk mengatur status visibilitas saldo
    private var isSaldoVisible = true
    private val saldoAsli = "Rp 1.000.000.000" // Nilai asli saldo

    override fun onBackPressed() {
        if (this is DashboardActivity) {
            // Jika di halaman Dashboard, keluar aplikasi
            finishAffinity()
        } else {
            super.onBackPressed() // Jika tidak di Dashboard, lakukan aksi default
        }
    }

    override fun onResume() {
        super.onResume()
        setCheckedNavigationItem(R.id.nav_dashboard)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Gunakan layout dashboard di dalam frameLayout dari BaseActivity
        setContentLayout(R.layout.activity_dashboard)

        // Set judul toolbar
        setToolbarTitle("Dashboard")
        setCheckedNavigationItem((R.id.nav_dashboard))
   
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
