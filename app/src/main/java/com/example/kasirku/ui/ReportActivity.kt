package com.example.kasirku.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.example.kasirku.R
import com.example.kasirku.base.BaseActivity
import com.google.android.material.navigation.NavigationView

class ReportActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Gunakan layout dashboard di dalam frameLayout dari BaseActivity
        setContentLayout(R.layout.activity_report)

        // Set judul toolbar
        setToolbarTitle("Laporan")
        setCheckedNavigationItem((R.id.nav_laporan))

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setCheckedItem(R.id.nav_laporan) // atau item yang sesuai


        val spinner = findViewById<Spinner>(R.id.spinnerFilter)
        val options = arrayOf("Harian", "Bulanan")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)
        spinner.adapter = adapter
    }
}