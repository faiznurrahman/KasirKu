package com.teamdev.mykasir.ui.cashier

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.teamdev.mykasir.R
import com.teamdev.mykasir.adapter.CashierAdapter
import com.teamdev.mykasir.model.Cashier
import com.teamdev.mykasir.base.BaseActivity
import com.teamdev.mykasir.model.Pesanan

class CashierActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var cashierAdapter: CashierAdapter
    private lateinit var productList: MutableList<Cashier>
    private lateinit var searchEditText: EditText
    private lateinit var totalTextView: TextView
    private lateinit var userId: String
    private lateinit var originalList: MutableList<Cashier>

    // Tambahan: map namaBarang -> productId
    private val productIdMap = HashMap<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentLayout(R.layout.activity_cashier)
        setToolbarTitle("Kasir")
        setCheckedNavigationItem(R.id.nav_kasir)

        searchEditText = findViewById(R.id.searchEditText)
        totalTextView = findViewById(R.id.totalHarga)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User belum login", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        userId = currentUser.uid
        recyclerView = findViewById(R.id.recyclerViewCashier)
        recyclerView.layoutManager = LinearLayoutManager(this)

        productList = mutableListOf()
        cashierAdapter = CashierAdapter(productList) { updatedProduct ->
            updateTotal()
        }
        recyclerView.adapter = cashierAdapter

        // Ambil data produk dari Firebase
        fetchDataFromFirebase()

        // Implementasi search
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterProducts(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        val buttonBeli = findViewById<Button>(R.id.buttonBeli)
        buttonBeli.setOnClickListener {
            val pesananList = productList
                .filter { it.quantity > 0 }
                .map {
                    Pesanan(
                        namaBarang = it.name ?: "",
                        qty = it.quantity,
                        harga = it.price ?: 0.0,
                        subtotal = (it.price ?: 0.0) * it.quantity
                    )
                }

            if (pesananList.isEmpty()) {
                Toast.makeText(this, "Tidak ada barang yang dipilih", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, com.teamdev.mykasir.ui.cashier.PaymentActivity::class.java)
            intent.putParcelableArrayListExtra("PESANAN_LIST", ArrayList(pesananList))
            intent.putExtra("PRODUCT_ID_MAP", productIdMap)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        setCheckedNavigationItem(R.id.nav_kasir)
    }

    private fun fetchDataFromFirebase() {
        originalList = mutableListOf()
        val database = FirebaseDatabase.getInstance("https://mykasir-cae5d-default-rtdb.asia-southeast1.firebasedatabase.app")
        val ref = database.getReference("users").child(userId).child("products")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear()
                productIdMap.clear() // Kosongkan map dulu
                for (child in snapshot.children) {
                    val product = child.getValue(Cashier::class.java)
                    val productId = child.key
                    product?.let {
                        productList.add(it)
                        originalList.add(it)

                        // Simpan productId berdasarkan nama
                        if (it.name != null && productId != null) {
                            productIdMap[it.name!!] = productId
                        }
                    }
                }
                cashierAdapter.notifyDataSetChanged()
                updateTotal()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CashierActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateTotal() {
        val total = productList.sumOf { (it.price ?: 0.0) * it.quantity }
        totalTextView.text = formatRupiah(total)
    }

    private fun filterProducts(query: String) {
        val filteredList = if (query.isEmpty()) {
            originalList
        } else {
            originalList.filter {
                it.name?.contains(query, true) == true || it.sku?.contains(query, true) == true
            }
        }

        cashierAdapter.updateData(filteredList)
    }

    private fun formatRupiah(amount: Number): String {
        val formatter = java.text.NumberFormat.getInstance(java.util.Locale("in", "ID"))
        return "Rp ${formatter.format(amount)}"
    }
}
