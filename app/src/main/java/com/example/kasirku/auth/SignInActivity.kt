package com.example.kasirku.auth

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kasirku.R
import com.example.kasirku.ui.DashboardActivity
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {

    private lateinit var textDaftar: TextView
    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var btnSignIn: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var auth: FirebaseAuth

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_in)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        editEmail = findViewById(R.id.editEmail)
        editPassword = findViewById(R.id.editPassword)
        btnSignIn = findViewById(R.id.btnSignIn)
        progressBar = findViewById(R.id.progressBar)
        textDaftar = findViewById(R.id.textDaftar)

        setupToggleIcon(editPassword)

        textDaftar.setOnClickListener {
            val intent = Intent(this@SignInActivity, SignUpActivity::class.java)
            startActivity(intent)
        }

        btnSignIn.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    progressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Login gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun setupToggleIcon(editText: EditText) {
        editText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = 2
                val drawable = editText.compoundDrawables[drawableEnd]
                if (drawable != null) {
                    val drawableWidth = drawable.bounds.width()
                    val touchArea = editText.right - drawableWidth - editText.paddingEnd
                    if (event.rawX >= touchArea) {
                        togglePasswordVisibility(editText)
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }

    private fun togglePasswordVisibility(editText: EditText) {
        isPasswordVisible = !isPasswordVisible

        val inputTypeVisible = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        val inputTypeHidden = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        val iconRes = if (isPasswordVisible) R.drawable.show else R.drawable.hide

        editText.inputType = if (isPasswordVisible) inputTypeVisible else inputTypeHidden
        editText.setCompoundDrawablesWithIntrinsicBounds(
            null,
            null,
            ContextCompat.getDrawable(this, iconRes)?.apply {
                setTint(ContextCompat.getColor(this@SignInActivity, android.R.color.darker_gray))
            },
            null
        )
        editText.setSelection(editText.text.length)
    }
}
