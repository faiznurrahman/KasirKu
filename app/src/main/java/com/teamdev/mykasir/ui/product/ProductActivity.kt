package com.teamdev.mykasir.ui.product

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
import com.teamdev.mykasir.adapter.ProductAdapter
import com.teamdev.mykasir.base.BaseActivity
import com.teamdev.mykasir.model.Product


class ProductActivity : BaseActivity() {

    private lateinit var productAdapter: ProductAdapter
    private lateinit var productList: MutableList<Product>
    private lateinit var recyclerView: RecyclerView
    private lateinit var userId: String
    private lateinit var searchEditText: EditText
    private lateinit var originalList: MutableList<Product> // List asli



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Gunakan layout product di dalam frame dari BaseActivity
        setContentLayout(R.layout.activity_product)
        setToolbarTitle("Barang")
        setCheckedNavigationItem(R.id.nav_barang)

        searchEditText = findViewById(R.id.searchEditText)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterProducts(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User belum login", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        userId = currentUser.uid

        // Inisialisasi RecyclerView
        recyclerView = findViewById(R.id.rv_produk)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Inisialisasi list dan adapter
        productList = mutableListOf()
        productAdapter = ProductAdapter(productList, ::onEditProduct, ::onDeleteProduct)
        recyclerView.adapter = productAdapter

        // Tombol tambah produk
        val btnAdd = findViewById<Button>(R.id.btnAdd)
        btnAdd.setOnClickListener {
            val intent = Intent(this, AddProductActivity::class.java)
            startActivity(intent)
        }

        // Ambil data dari Firebase
        fetchDataFromFirebase()
    }

    override fun onResume() {
        super.onResume()
        setCheckedNavigationItem(R.id.nav_barang)
    }

    private fun fetchDataFromFirebase() {
        originalList = mutableListOf()
        val database = FirebaseDatabase.getInstance("https://mykasir-cae5d-default-rtdb.asia-southeast1.firebasedatabase.app")
        val ref = database.getReference("users").child(userId).child("products")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear()
                for (child in snapshot.children) {
                    val product = child.getValue(Product::class.java)
                    product?.let {
                        productList.add(it)
                        originalList.add(it)}
                }

                // Urutkan berdasarkan nama produk
                productList.sortBy { it.nama?.lowercase() }

                productAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProductActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterProducts(query: String) {
        val filteredList = if (query.isEmpty()) {
            originalList
        } else {
            originalList.filter {
                it.nama?.contains(query, true) == true ||
                        it.sku?.contains(query, true) == true ||
                        it.kategori?.contains(query, true) == true
            }
        }

        // Update list di adapter
        productAdapter.updateData(filteredList.toMutableList())

        // Tampilkan pesan jika kosong
        val notFoundText = findViewById<TextView>(R.id.tvNotFound)
        notFoundText.visibility = if (filteredList.isEmpty()) TextView.VISIBLE else TextView.GONE
    }




    private fun onEditProduct(product: Product) {
//        val intent = Intent(this, EditProductActivity::class.java)
//        intent.putExtra("PRODUCT_SKU", product.sku)
//        startActivity(intent)
        Toast.makeText(this, "Edit produk: ${product.nama}", Toast.LENGTH_SHORT).show()
    }

    private fun onDeleteProduct(product: Product) {
        val database = FirebaseDatabase.getInstance("https://mykasir-cae5d-default-rtdb.asia-southeast1.firebasedatabase.app")
        val ref = database.getReference("users").child(userId).child("products").child(product.sku)

        ref.removeValue().addOnSuccessListener {
            Toast.makeText(this, "Produk berhasil dihapus", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal menghapus produk", Toast.LENGTH_SHORT).show()
        }
    }
}