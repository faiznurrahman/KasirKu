package com.teamdev.mykasir.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.teamdev.mykasir.R
import com.teamdev.mykasir.auth.SignUpActivity
import com.teamdev.mykasir.auth.SignInActivity

class OnBoardingActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var btnDaftarActivity: Button
    private lateinit var btnMasukActivity: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_on_boarding)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnDaftarActivity = findViewById(R.id.buttonDaftar)
        btnDaftarActivity.setOnClickListener(this)

        btnMasukActivity = findViewById(R.id.btnmasuk)
        btnMasukActivity.setOnClickListener(this)


    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.buttonDaftar -> {
                val intentDaftar =  Intent(this@OnBoardingActivity, SignUpActivity::class.java)
                startActivity(intentDaftar)
            }
            R.id.btnmasuk -> {
                val intentMasuk =  Intent(this@OnBoardingActivity, SignInActivity::class.java)
                startActivity(intentMasuk)
            }
        }
    }
}