package com.teamdev.mykasir.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.teamdev.mykasir.R
import com.teamdev.mykasir.ui.setting.SettingActivity
import com.teamdev.mykasir.base.BaseActivity
import com.teamdev.mykasir.ui.cashier.CashierActivity
import com.teamdev.mykasir.ui.product.ProductActivity
import java.text.SimpleDateFormat
import java.util.*

// Activity untuk halaman Dashboard
class DashboardActivity : BaseActivity() {

    private lateinit var textJumlahSaldo: TextView
    private lateinit var iconVisibility: ImageView

    private var isSaldoVisible = true
    private var saldoAsli = "" // Akan diisi setelah hitung saldo

    override fun onBackPressed() {
        if (this is DashboardActivity) {
            finishAffinity()
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        setCheckedNavigationItem(R.id.nav_dashboard)
        loadSaldoHariIni() // Refresh saldo saat kembali ke Dashboard
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentLayout(R.layout.activity_dashboard)

        setToolbarTitle("Dashboard")
        setCheckedNavigationItem(R.id.nav_dashboard)

        textJumlahSaldo = findViewById(R.id.textJumlahSaldo)
        iconVisibility = findViewById(R.id.iconVisibility)

        iconVisibility.setOnClickListener {
            isSaldoVisible = !isSaldoVisible
            if (isSaldoVisible) {
                textJumlahSaldo.text = saldoAsli
                iconVisibility.setImageResource(R.drawable.hide)
            } else {
                textJumlahSaldo.text = "********"
                iconVisibility.setImageResource(R.drawable.show)
            }
        }

        val cardKasir: View = findViewById(R.id.cardKasir)
        val cardBarang: View = findViewById(R.id.cardBarang)
        val cardLaporan: View = findViewById(R.id.cardLaporan)
        val cardPengaturan: View = findViewById(R.id.cardPengaturan)
        val cardKritik: View = findViewById(R.id.cardKritik)
        val cardBantuan: View = findViewById(R.id.cardBantuan)

        cardBantuan.setOnClickListener {
            val nomorWa = "6285721841873" // Ganti dengan nomor tujuan (format internasional tanpa "+" atau spasi)
            val pesan = "Halo, saya butuh bantuan dengan aplikasi MyKasir."
            val url = "https://wa.me/$nomorWa?text=${Uri.encode(pesan)}"

            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("DashboardActivity", "WhatsApp tidak terpasang: ${e.message}")
                Toast.makeText(this, "WhatsApp tidak ditemukan di perangkat ini.", Toast.LENGTH_SHORT).show()

            }
        }


        cardKasir.setOnClickListener {
            startActivity(Intent(this, CashierActivity::class.java))
        }

        cardBarang.setOnClickListener {
            startActivity(Intent(this, ProductActivity::class.java))
        }

        cardLaporan.setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java))
        }

        cardPengaturan.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }
        cardKritik.setOnClickListener {
            startActivity(Intent(this, SuggestionsActivity::class.java))
        }

        // Load saldo pertama kali
        loadSaldoHariIni()
    }

    private fun loadSaldoHariIni() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance().getReference("users/$uid/purchaseHistory")

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        var totalSaldoHariIni = 0

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (trans in snapshot.children) {
                    val tanggal = trans.child("tanggal").getValue(String::class.java)
                    val totalHarga = trans.child("totalHarga").getValue(Int::class.java) ?: 0
                    if (tanggal?.startsWith(today) == true) {
                        totalSaldoHariIni += totalHarga
                    }
                }

                saldoAsli = "Rp ${formatRupiah(totalSaldoHariIni)}"
                if (isSaldoVisible) {
                    textJumlahSaldo.text = saldoAsli
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DashboardActivity", "Gagal mengambil data: ${error.message}")
            }
        })
    }

    private fun formatRupiah(amount: Int): String {
        return String.format("%,d", amount).replace(',', '.')
    }
}
