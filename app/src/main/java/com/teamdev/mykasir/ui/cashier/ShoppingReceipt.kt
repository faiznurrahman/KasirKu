package com.teamdev.mykasir.ui.cashier

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.teamdev.mykasir.R
import com.teamdev.mykasir.adapter.DetailBarangAdapter
import com.teamdev.mykasir.model.DetailBarang
import com.teamdev.mykasir.model.Transaksi

class ShoppingReceipt : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var rvProduk: RecyclerView
    private lateinit var adapter: DetailBarangAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_receipt)

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Inisialisasi RecyclerView
        rvProduk = findViewById(R.id.rvProduk)
        rvProduk.layoutManager = LinearLayoutManager(this)
        adapter = DetailBarangAdapter()
        rvProduk.adapter = adapter

        // Ambil kode transaksi dari intent
        val kodeTransaksi = intent.getStringExtra("KODE_TRANSAKSI")

        if (kodeTransaksi != null) {
            val uid = auth.currentUser?.uid ?: return
            val transaksiRef = database.child("users").child(uid)
                .child("purchaseHistory").child(kodeTransaksi)

            transaksiRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Ambil data transaksi
                        val transaksi = snapshot.getValue(Transaksi::class.java)

                        // Tampilkan data transaksi ke TextView
                        findViewById<TextView>(R.id.txtNoTransaksi).text = transaksi?.kodeTransaksi
                        findViewById<TextView>(R.id.txtTanggal).text = transaksi?.tanggal
                        findViewById<TextView>(R.id.txtPembayaran).text = transaksi?.metodePembayaran
                        findViewById<TextView>(R.id.txtTotal).text = formatRupiah(transaksi?.totalHarga ?: 0.0)
                        findViewById<TextView>(R.id.txtBayar).text = formatRupiah(transaksi?.jumlahUang ?: 0.0)
                        findViewById<TextView>(R.id.txtKembali).text = formatRupiah(transaksi?.kembalian ?: 0.0)

                        // Ambil nama kasir
                        database.child("users").child(uid).child("nama")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val namaKasir = snapshot.getValue(String::class.java) ?: "Kasir"
                                    findViewById<TextView>(R.id.txtKasir).text = namaKasir
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            })

                        // Ambil detail barang dan set ke adapter
                        val detailList = mutableListOf<DetailBarang>()
                        snapshot.child("detailBarang").children.forEach { item ->
                            val barang = item.getValue(DetailBarang::class.java)
                            barang?.let { detailList.add(it) }
                        }

                        // Set data ke adapter
                        if (::adapter.isInitialized) {
                            adapter.setData(detailList)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }

        // Tombol keluar
        findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btnKeluar).setOnClickListener {
            val intent = Intent(this, CashierActivity::class.java)
            startActivity(intent)
            finish() // Tutup aktivitas ShoppingReceipt setelah berpindah
        }
    }

    // Format Rupiah
    private fun formatRupiah(number: Number): String {
        val formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale("in", "ID"))
        return "Rp${formatter.format(number)}"
    }
}
