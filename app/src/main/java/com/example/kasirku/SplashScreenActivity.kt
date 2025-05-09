package com.example.kasirku

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kasirku.ui.DashboardActivity
import com.example.kasirku.ui.OnBoardingActivity
import com.google.firebase.auth.FirebaseAuth

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.hide()

        val currentUser = FirebaseAuth.getInstance().currentUser

        Handler(Looper.getMainLooper()).postDelayed({
            if (currentUser != null) {
                // Sudah login → langsung ke Dashboard
                startActivity(Intent(this@SplashScreenActivity, DashboardActivity::class.java))
            } else {
                // Belum login → lanjut ke onboarding
                startActivity(Intent(this@SplashScreenActivity, OnBoardingActivity::class.java))
            }
            finish()
        }, 3000) // 3 detik delay splash screen
    }
}
