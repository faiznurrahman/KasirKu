package com.example.kasirku.base

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.kasirku.R
import com.example.kasirku.SettingActivity
import com.example.kasirku.ui.*
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

open class BaseActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    protected lateinit var drawerLayout: DrawerLayout
    private lateinit var titleTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val baseLayout = layoutInflater.inflate(R.layout.activity_base, null) as DrawerLayout
        setContentView(baseLayout)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://kasirku-7ce1b-default-rtdb.asia-southeast1.firebasedatabase.app").reference

        drawerLayout = baseLayout
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        titleTextView = findViewById(R.id.toolbar_title)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.menu)
        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        val navView = findViewById<NavigationView>(R.id.nav_view)
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
                R.id.nav_barang -> {
                    startActivity(Intent(this, ProductActivity::class.java))
                }
                R.id.nav_laporan -> {
                    startActivity(Intent(this, ReportActivity::class.java))
                }
                R.id.nav_pengaturan -> {
                    startActivity(Intent(this, SettingActivity::class.java))
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
        val navView = findViewById<NavigationView>(R.id.nav_view)
        navView.setCheckedItem(itemId)
    }
}