package com.teamdev.mykasir.ui.setting

import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.teamdev.mykasir.R
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class ChangePwActivity : AppCompatActivity() {

    private lateinit var etCurrentPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var ivToggleCurrent: ImageView
    private lateinit var ivToggleNew: ImageView
    private lateinit var ivToggleConfirm: ImageView
    private lateinit var btnSimpan: AppCompatButton

    private var isCurrentVisible = false
    private var isNewVisible = false
    private var isConfirmVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_pw)

        // Init
        etCurrentPassword = findViewById(R.id.etCurrentPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        ivToggleCurrent = findViewById(R.id.ivToggleCurrent)
        ivToggleNew = findViewById(R.id.ivToggleNew)
        ivToggleConfirm = findViewById(R.id.ivToggleConfirm)
        btnSimpan = findViewById(R.id.btnSimpan)

        // Toggle visibility listeners
        ivToggleCurrent.setOnClickListener {
            isCurrentVisible = !isCurrentVisible
            togglePasswordVisibility(etCurrentPassword, ivToggleCurrent, isCurrentVisible)
        }

        ivToggleNew.setOnClickListener {
            isNewVisible = !isNewVisible
            togglePasswordVisibility(etNewPassword, ivToggleNew, isNewVisible)
        }

        ivToggleConfirm.setOnClickListener {
            isConfirmVisible = !isConfirmVisible
            togglePasswordVisibility(etConfirmPassword, ivToggleConfirm, isConfirmVisible)
        }

        // Save button logic
        btnSimpan.setOnClickListener {
            val currentPassword = etCurrentPassword.text.toString().trim()
            val newPassword = etNewPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                MotionToast.createToast(
                    this,
                    "Gagal!",
                    "Harap isi semua kolom",
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, R.font.poppinsregular)
                )
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                MotionToast.createToast(
                    this,
                    "Gagal!",
                    "Konfirmasi password tidak cocok",
                    MotionToastStyle.WARNING,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, R.font.poppinsregular)
                )
                return@setOnClickListener
            }

            val user = FirebaseAuth.getInstance().currentUser
            val email = user?.email

            if (email != null) {
                val credential = EmailAuthProvider.getCredential(email, currentPassword)

                // Re-authenticate user first
                user.reauthenticate(credential).addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                MotionToast.createToast(
                                    this,
                                    "Berhasil!",
                                    "Password berhasil diperbarui",
                                    MotionToastStyle.SUCCESS,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    ResourcesCompat.getFont(this, R.font.poppinsregular)
                                )
                                finish()
                            } else {
                                MotionToast.createToast(
                                    this,
                                    "Gagal!",
                                    "Gagal memperbarui password",
                                    MotionToastStyle.ERROR,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    ResourcesCompat.getFont(this, R.font.poppinsregular)
                                )
                            }
                        }
                    } else {
                        MotionToast.createToast(
                            this,
                            "Gagal!",
                            "Password saat ini salah",
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(this, R.font.poppinsregular)
                        )
                    }
                }
            } else {
                MotionToast.createToast(
                    this,
                    "Gagal!",
                    "Pengguna tidak ditemukan",
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, R.font.poppinsregular)
                )
            }
        }
    }

    private fun togglePasswordVisibility(editText: EditText, toggleIcon: ImageView, isVisible: Boolean) {
        if (isVisible) {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            toggleIcon.setImageResource(R.drawable.show)
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            toggleIcon.setImageResource(R.drawable.hide)
        }
        editText.setSelection(editText.text.length)
    }
}
