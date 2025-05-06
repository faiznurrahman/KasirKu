package com.example.kasirku

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SignUpActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var btnDashboardActivity: Button
    private lateinit var textMasuk: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnDashboardActivity = findViewById(R.id.btnDashboard)
        btnDashboardActivity.setOnClickListener(this)

        textMasuk = findViewById(R.id.textMasuk)
        textMasuk.setOnClickListener(this) // Tambahkan listener
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnDashboard -> {
                val intentDashboard = Intent(this@SignUpActivity, DashboardActivity::class.java)
                startActivity(intentDashboard)
            }
            R.id.textMasuk -> {
                val intentSignIn = Intent(this@SignUpActivity, SignInActivity::class.java)
                startActivity(intentSignIn)
            }
        }
    }
}
