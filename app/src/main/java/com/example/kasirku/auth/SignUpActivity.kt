package com.example.kasirku.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kasirku.R
import com.example.kasirku.ui.DashboardActivity
import com.google.firebase.auth.FirebaseAuth
import android.text.InputType
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var btnDashboardActivity: Button
    private lateinit var textMasuk: TextView

    private lateinit var editNama: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var editKonfirmasi: EditText
    private var isPasswordVisible = false
    private lateinit var progressBar: ProgressBar

    private lateinit var auth: FirebaseAuth

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
        textMasuk = findViewById(R.id.textMasuk)

        editNama = findViewById(R.id.editNama)
        editEmail = findViewById(R.id.editEmail)
        editPassword = findViewById(R.id.editPassword)
        editKonfirmasi = findViewById(R.id.editKonfirmasi)
        setupToggleIcon(editPassword)
        setupToggleIcon(editKonfirmasi)
        progressBar = findViewById(R.id.progressBar)

        auth = FirebaseAuth.getInstance()

        btnDashboardActivity.setOnClickListener(this)
        textMasuk.setOnClickListener(this)
    }

    private fun setupToggleIcon(editText: EditText) {
        editText.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = 2
                val drawable = editText.compoundDrawables[drawableEnd]
                if (drawable != null) {
                    val drawableWidth = drawable.bounds.width()
                    val touchArea = editText.right - drawableWidth - editText.paddingEnd
                    if (event.rawX >= touchArea) {
                        if (view.getTag(R.id.password_toggle_tag) != true) {
                            view.setTag(R.id.password_toggle_tag, true)
                            togglePasswordVisibility()
                            view.postDelayed({
                                view.setTag(R.id.password_toggle_tag, false)
                            }, 150)
                        }
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible

        val inputTypeVisible = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        val inputTypeHidden = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        val iconRes = if (isPasswordVisible) R.drawable.show else R.drawable.hide

        updateEditTextVisibility(editPassword, iconRes, isPasswordVisible, inputTypeVisible, inputTypeHidden)
        updateEditTextVisibility(editKonfirmasi, iconRes, isPasswordVisible, inputTypeVisible, inputTypeHidden)
    }

    private fun updateEditTextVisibility(
        editText: EditText,
        iconRes: Int,
        visible: Boolean,
        inputTypeVisible: Int,
        inputTypeHidden: Int
    ) {
        editText.inputType = if (visible) inputTypeVisible else inputTypeHidden
        editText.setCompoundDrawablesWithIntrinsicBounds(
            null,
            null,
            ContextCompat.getDrawable(this, iconRes)?.apply {
                setTint(ContextCompat.getColor(this@SignUpActivity, android.R.color.darker_gray))
            },
            null
        )
        editText.setSelection(editText.text.length)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnDashboard -> {
                val nama = editNama.text.toString().trim()
                val email = editEmail.text.toString().trim()
                val password = editPassword.text.toString().trim()
                val konfirmasi = editKonfirmasi.text.toString().trim()

                if (nama.isEmpty() || email.isEmpty() || password.isEmpty() || konfirmasi.isEmpty()) {
                    Toast.makeText(this, "Semua kolom harus diisi!", Toast.LENGTH_SHORT).show()
                    return
                }

                if (password != konfirmasi) {
                    Toast.makeText(this, "Password tidak cocok", Toast.LENGTH_SHORT).show()
                    return
                }

                progressBar.visibility = View.VISIBLE

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        progressBar.visibility = View.GONE
                        if (task.isSuccessful) {
                            val userId = task.result.user?.uid
                            val database = FirebaseDatabase.getInstance("https://kasirku-7ce1b-default-rtdb.asia-southeast1.firebasedatabase.app").reference
                            val user = mapOf(
                                "uid" to userId,
                                "nama" to nama,
                                "email" to email
                            )
                            if (userId != null) {
                                database.child("users").child(userId).setValue(user)
                                    .addOnSuccessListener {
                                        // Toast berhasil daftar
                                        Toast.makeText(this, "Berhasil daftar!", Toast.LENGTH_SHORT).show()

                                        // Langsung buka DashboardActivity tanpa delay
                                        val intent = Intent(this, DashboardActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                            }
                        } else {
                            // Gagal daftar, tampilkan toast error
                            Toast.makeText(this, "Gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }

            }

            R.id.textMasuk -> {
                val intentSignIn = Intent(this@SignUpActivity, SignInActivity::class.java)
                startActivity(intentSignIn)
            }
        }
    }
}
