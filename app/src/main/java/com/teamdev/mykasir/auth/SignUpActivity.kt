package com.teamdev.mykasir.auth

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
import com.teamdev.mykasir.R
import com.teamdev.mykasir.ui.DashboardActivity
import com.google.firebase.auth.FirebaseAuth
import android.text.InputType
import android.view.MotionEvent
import android.widget.CheckBox
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.database.FirebaseDatabase
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class SignUpActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var btnDashboardActivity: Button
    private lateinit var textMasuk: TextView
    private lateinit var checkBox: CheckBox


    private lateinit var editNama: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var editKonfirmasi: EditText
    private var isPasswordVisible = false
    private lateinit var loadingOverlay: FrameLayout

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
        checkBox = findViewById(R.id.checkBox)


        editNama = findViewById(R.id.editNama)
        editEmail = findViewById(R.id.editEmail)
        editPassword = findViewById(R.id.editPassword)
        editKonfirmasi = findViewById(R.id.editKonfirmasi)
        setupToggleIcon(editPassword)
        setupToggleIcon(editKonfirmasi)
        loadingOverlay = findViewById(R.id.progressOverlay)

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

                if (!checkBox.isChecked) {
                    Toast.makeText(this, "Anda harus menyetujui syarat dan ketentuan", Toast.LENGTH_SHORT).show()
                    return
                }

                loadingOverlay.visibility = View.VISIBLE

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        loadingOverlay.visibility = View.GONE
                        if (task.isSuccessful) {
                            val userId = task.result.user?.uid
                            val database = FirebaseDatabase.getInstance("https://mykasir-cae5d-default-rtdb.asia-southeast1.firebasedatabase.app").reference
                            val user = mapOf(
                                "uid" to userId,
                                "nama" to nama,
                                "email" to email
                            )
                            if (userId != null) {
                                database.child("users").child(userId).setValue(user)
                                    .addOnSuccessListener {
                                        MotionToast.createToast(
                                            this,
                                            "Berhasil!",
                                            "Akun berhasil dibuat.",
                                            MotionToastStyle.SUCCESS,
                                            MotionToast.GRAVITY_BOTTOM,
                                            MotionToast.LONG_DURATION,
                                            ResourcesCompat.getFont(this, R.font.poppinsregular)
                                        )

                                        val intent = Intent(this, DashboardActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                            }
                        } else {
                            MotionToast.createToast(
                                this,
                                "Gagal!",
                                "Akun tidak berhasil dibuat!",
                                MotionToastStyle.ERROR,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(this, R.font.poppinsregular))
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
