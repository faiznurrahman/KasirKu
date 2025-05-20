package com.teamdev.mykasir.auth

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.teamdev.mykasir.R
import com.teamdev.mykasir.ui.DashboardActivity
import com.google.firebase.auth.FirebaseAuth
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class SignInActivity : AppCompatActivity() {

    private lateinit var textDaftar: TextView
    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var btnSignIn: Button
    private lateinit var loadingOverlay: FrameLayout


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
        loadingOverlay = findViewById(R.id.progressOverlay)
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

            loadingOverlay.visibility = View.VISIBLE


            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    loadingOverlay.visibility = View.GONE
                    if (task.isSuccessful) {
                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                    } else {
                        MotionToast.createToast(
                            this,
                            "Gagal!",
                            "Login Gagal! Cek katasandi anda.",
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(this, R.font.poppinsregular))
                    }
                }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val view = currentFocus
        if (view is EditText) {
            val outRect = android.graphics.Rect()
            view.getGlobalVisibleRect(outRect)
            if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                view.clearFocus()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
        return super.dispatchTouchEvent(ev)
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
