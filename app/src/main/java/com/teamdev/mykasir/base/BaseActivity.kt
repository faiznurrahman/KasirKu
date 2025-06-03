package com.teamdev.mykasir.base

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.teamdev.mykasir.ui.setting.SettingActivity
import com.teamdev.mykasir.ui.*
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.teamdev.mykasir.R
import com.teamdev.mykasir.ui.cashier.CashierActivity
import com.teamdev.mykasir.ui.product.ProductActivity


open class BaseActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    protected lateinit var drawerLayout: DrawerLayout
    private lateinit var titleTextView: TextView
    private lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val baseLayout = layoutInflater.inflate(R.layout.activity_base, null) as DrawerLayout
        setContentView(baseLayout)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://mykasir-cae5d-default-rtdb.asia-southeast1.firebasedatabase.app").reference

        drawerLayout = baseLayout
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        titleTextView = findViewById(R.id.toolbar_title)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.menu)
        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navView = findViewById(R.id.nav_view)
        val headerView = navView.getHeaderView(0)
        val textNama = headerView.findViewById<TextView>(R.id.textView8)
        val textEmail = headerView.findViewById<TextView>(R.id.textView9)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            database.child("users").child(currentUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val nama = snapshot.child("nama").getValue(String::class.java)
                        val email = snapshot.child("email").getValue(String::class.java)

                        textNama.text = nama ?: "Nama tidak ditemukan"
                        textEmail.text = email ?: "Email tidak ditemukan"
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@BaseActivity, "Gagal memuat header: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        } else {
            val intent = Intent(this@BaseActivity, OnBoardingActivity::class.java)
            startActivity(intent)
            finish()
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                }
                R.id.nav_kasir -> {
                    startActivity(Intent(this, CashierActivity::class.java))
                }
                R.id.nav_barang -> {
                    startActivity(Intent(this, ProductActivity::class.java))
                }
                R.id.nav_laporan -> {
                    startActivity(Intent(this, ReportActivity::class.java))
                }
                R.id.nav_pengaturan -> {
                    startActivity(Intent(this, SettingActivity::class.java))
                }
                R.id.nav_kritik -> {
                    startActivity(Intent(this, SuggestionsActivity::class.java))
                }
                R.id.nav_bantuan -> {
                    val nomorWa = "6285721841873"
                    val pesan = "Halo, saya butuh bantuan dengan aplikasi MyKasir."
                    val url = "https://wa.me/$nomorWa?text=${Uri.encode(pesan)}"

                    try {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(url)
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(this, "WhatsApp tidak ditemukan di perangkat ini.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        val btnLogout = findViewById<Button>(R.id.btn_logout)
        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this@BaseActivity, OnBoardingActivity::class.java))
            finish()
        }
    }

    protected fun setToolbarTitle(title: String) {
        titleTextView.text = title
    }

    protected fun setContentLayout(layoutResId: Int) {
        val contentFrame = findViewById<FrameLayout>(R.id.base_content)
        layoutInflater.inflate(layoutResId, contentFrame, true)
    }

    protected fun setCheckedNavigationItem(itemId: Int) {
        navView.setCheckedItem(itemId)
    }
}
