package com.teamdev.mykasir.ui.cashier

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.teamdev.mykasir.R
import android.content.Intent
import android.widget.Button


class PaymentSuccess : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_payment_success)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Mengambil ID dari Intent yang datang (misalnya KODE_TRANSAKSI)
        val kodeTransaksi = intent.getStringExtra("KODE_TRANSAKSI")

        // Inisialisasi tombol
        val btnTransaksiBaru = findViewById<Button>(R.id.btnTransaksiBaru)
        val btnLihatStruk = findViewById<Button>(R.id.btnLihatStruk)

        // Klik Transaksi Baru -> ke CashierActivity
        btnTransaksiBaru.setOnClickListener {
            val intent = Intent(this, CashierActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish() // Optional: supaya tidak bisa kembali ke halaman ini
        }

        // Klik Lihat Struk -> ke ShoppingReceipt
        btnLihatStruk.setOnClickListener {
            // Kirim ID (KODE_TRANSAKSI) ke ShoppingReceipt
            val lihatStrukIntent = Intent(this, ShoppingReceipt::class.java)
            lihatStrukIntent.putExtra("KODE_TRANSAKSI", kodeTransaksi)
            startActivity(lihatStrukIntent)
        }
    }
}
