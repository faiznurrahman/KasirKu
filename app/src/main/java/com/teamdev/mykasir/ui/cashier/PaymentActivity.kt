package com.teamdev.mykasir.ui.cashier

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.teamdev.mykasir.R
import com.teamdev.mykasir.adapter.PesananAdapter
import com.teamdev.mykasir.model.Pesanan
import com.teamdev.mykasir.ui.product.ProductActivity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import androidx.appcompat.widget.Toolbar
import java.util.*

class PaymentActivity : AppCompatActivity() {

    private lateinit var btnTunai: Button
    private lateinit var btnNonTunai: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var textTotal: TextView
    private lateinit var textKembalian: TextView
    private lateinit var editJumlahUang: EditText
    private lateinit var btnKonfirmasi: Button
    private var loadingDialog: AlertDialog? = null

    private var metodePembayaran: String = "Tunai"
    private var totalHarga: Double = 0.0
    private lateinit var pesananList: ArrayList<Pesanan>
    private lateinit var productIdMap: HashMap<String, String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        btnTunai = findViewById(R.id.btnTunai)
        btnNonTunai = findViewById(R.id.btnNonTunai)
        recyclerView = findViewById(R.id.recyclerViewPesanan)
        textTotal = findViewById(R.id.textTotal)
        textKembalian = findViewById(R.id.textKembalian)
        editJumlahUang = findViewById(R.id.editJumlahUang)
        btnKonfirmasi = findViewById(R.id.btnKonfirmasi)

        productIdMap = intent.getSerializableExtra("PRODUCT_ID_MAP") as HashMap<String, String>
        pesananList = intent.getParcelableArrayListExtra("PESANAN_LIST") ?: arrayListOf()

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = PesananAdapter(pesananList)

        totalHarga = pesananList.sumOf { it.subtotal }
        textTotal.text = " ${formatRupiah(totalHarga)}"

        showTunaiUI()

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.vector)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        btnTunai.setOnClickListener {
            metodePembayaran = "Tunai"
            showTunaiUI()
        }

        btnNonTunai.setOnClickListener {
            metodePembayaran = "Non-Tunai"
            showNonTunaiUI()
        }

        editJumlahUang.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (metodePembayaran == "Tunai") {
                    val inputUang = s.toString().toIntOrNull() ?: 0
                    val kembalian = inputUang - totalHarga.toInt()

                    if (kembalian < 0) {
                        textKembalian.text = "Uang tidak cukup"
                        textKembalian.setTextColor(Color.RED)
                    } else {
                        textKembalian.text = " ${formatRupiah(kembalian.toDouble())}"
                        textKembalian.setTextColor(Color.BLACK)
                    }
                } else {
                    textKembalian.text = ""
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnKonfirmasi.setOnClickListener {
            val jumlahUang = if (metodePembayaran == "Tunai") {
                editJumlahUang.text.toString().toIntOrNull()
            } else {
                totalHarga.toInt()
            }

            if (metodePembayaran == "Tunai" && (jumlahUang == null || jumlahUang == 0)) {
                Toast.makeText(this, "Silakan isi jumlah uang", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isConnected()) {
                Toast.makeText(this, "Tidak ada koneksi internet", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val kembalian = if (metodePembayaran == "Tunai") {
                val jumlahUangDouble = jumlahUang?.toDouble() ?: 0.0

                if (jumlahUangDouble < totalHarga) {
                    textKembalian.text = "Uang tidak cukup"
                    textKembalian.setTextColor(Color.RED)
                    return@setOnClickListener
                } else {
                    jumlahUangDouble - totalHarga
                }
            } else {
                0.0
            }

            showLoadingDialog()

            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val dbRef = FirebaseDatabase.getInstance().getReference("users/$uid/purchaseHistory")
            val produkRef = FirebaseDatabase.getInstance().getReference("users/$uid/products")

            val kodeTransaksi = "TRX${System.currentTimeMillis()}"
            val tanggal = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            // Ambil semua sku sebelum menyimpan transaksi
            val detailBarangList = mutableListOf<Map<String, Any>>()
            val iterator = pesananList.iterator()

            fun processNext() {
                if (!iterator.hasNext()) {
                    val transaksiMap = mapOf(
                        "kodeTransaksi" to kodeTransaksi,
                        "tanggal" to tanggal,
                        "metodePembayaran" to metodePembayaran,
                        "jumlahUang" to jumlahUang,
                        "totalHarga" to totalHarga,
                        "kembalian" to kembalian,
                        "detailBarang" to detailBarangList
                    )

                    dbRef.child(kodeTransaksi).setValue(transaksiMap).addOnCompleteListener { task ->
                        hideKeyboard()
                        dismissLoadingDialog()
                        if (task.isSuccessful) {
                            updateStock(uid, produkRef, kodeTransaksi, kembalian)
                        } else {
                            Toast.makeText(this, "Gagal menyimpan transaksi", Toast.LENGTH_SHORT).show()
                        }
                    }
                    return
                }

                val pesanan = iterator.next()
                val productId = productIdMap[pesanan.namaBarang]
                if (productId == null) {
                    Toast.makeText(this, "ID produk tidak ditemukan untuk ${pesanan.namaBarang}", Toast.LENGTH_SHORT).show()
                    processNext()
                    return
                }

                produkRef.child(productId).child("sku").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val sku = snapshot.getValue(String::class.java) ?: "-"
                        val barangMap = mapOf(
                            "nama" to pesanan.namaBarang,
                            "jumlah" to pesanan.qty,
                            "harga" to pesanan.harga,
                            "subtotal" to pesanan.subtotal,
                            "sku" to sku
                        )
                        detailBarangList.add(barangMap)
                        processNext()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@PaymentActivity, "Gagal mengambil SKU", Toast.LENGTH_SHORT).show()
                        dismissLoadingDialog()
                    }
                })
            }

            processNext()
        }
    }

    private fun updateStock(uid: String, produkRef: DatabaseReference, kodeTransaksi: String, kembalian: Double) {
        val dbRef = FirebaseDatabase.getInstance().getReference("users/$uid/purchaseHistory")
        var pendingUpdates = pesananList.size
        var allUpdatesSuccessful = true

        for (pesanan in pesananList) {
            val namaBarang = pesanan.namaBarang
            val jumlahBeli = pesanan.qty
            val productId = productIdMap[namaBarang]

            if (productId == null) {
                pendingUpdates--
                allUpdatesSuccessful = false
                Toast.makeText(this@PaymentActivity, "ID produk untuk $namaBarang tidak ditemukan", Toast.LENGTH_SHORT).show()

                if (pendingUpdates == 0) {
                    dbRef.child(kodeTransaksi).removeValue()
                    dismissLoadingDialog()
                    Toast.makeText(this@PaymentActivity, "Gagal memperbarui stok", Toast.LENGTH_SHORT).show()
                }
                continue
            }

            val stokRef = produkRef.child(productId).child("stok")
            stokRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val stokSekarang = currentData.getValue(String::class.java)?.toIntOrNull()
                    if (stokSekarang == null) return Transaction.abort()
                    val stokBaru = stokSekarang - jumlahBeli

                    return if (stokBaru >= 0) {
                        currentData.value = stokBaru.toString()
                        Transaction.success(currentData)
                    } else {
                        Transaction.abort()
                    }
                }

                override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                    pendingUpdates--
                    if (!committed || error != null) {
                        allUpdatesSuccessful = false
                        Toast.makeText(this@PaymentActivity, "Stok untuk $namaBarang tidak cukup", Toast.LENGTH_SHORT).show()
                    }

                    if (pendingUpdates == 0) {
                        if (allUpdatesSuccessful) {
                            val intent = Intent(this@PaymentActivity, PaymentSuccess::class.java)
                            intent.putExtra("KODE_TRANSAKSI", kodeTransaksi)
                            intent.putExtra("KEMBALIAN", kembalian)
                            startActivity(intent)
                            finish()
                        } else {
                            dbRef.child(kodeTransaksi).removeValue()
                            dismissLoadingDialog()
                            Toast.makeText(this@PaymentActivity, "Gagal memperbarui stok", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }
    }

    private fun showTunaiUI() {
        btnTunai.setTextColor(ContextCompat.getColor(this, R.color.blue))
        btnNonTunai.setTextColor(Color.GRAY)
        editJumlahUang.visibility = View.VISIBLE
        textKembalian.visibility = View.VISIBLE
        showKeyboard()
    }

    private fun showNonTunaiUI() {
        btnNonTunai.setTextColor(ContextCompat.getColor(this, R.color.blue))
        btnTunai.setTextColor(Color.GRAY)
        editJumlahUang.visibility = View.GONE
        textKembalian.visibility = View.GONE
    }

    private fun isConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork
        val capabilities = cm.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    private fun showLoadingDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setView(R.layout.dialog_loading)
        builder.setCancelable(false)
        loadingDialog = builder.create()
        loadingDialog?.show()
    }

    private fun dismissLoadingDialog() {
        loadingDialog?.dismiss()
    }

    private fun showKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editJumlahUang, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editJumlahUang.windowToken, 0)
    }

    private fun formatRupiah(value: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(value)
    }
}
