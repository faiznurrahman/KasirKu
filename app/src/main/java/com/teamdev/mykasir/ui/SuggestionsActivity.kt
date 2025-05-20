package com.teamdev.mykasir.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.teamdev.mykasir.R
import com.teamdev.mykasir.base.BaseActivity
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class SuggestionsActivity : BaseActivity() {

    private lateinit var editMasukan: EditText
    private lateinit var btnKirim: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentLayout(R.layout.activity_suggestions)
        setToolbarTitle("Kritik & Saran")
        setCheckedNavigationItem(R.id.nav_kritik)

        editMasukan = findViewById(R.id.editMasukan)
        btnKirim = findViewById(R.id.btnKirim)

        btnKirim.setOnClickListener {
            val masukan = editMasukan.text.toString().trim()

            if (masukan.isEmpty()) {
                Toast.makeText(this, "Masukan tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                Toast.makeText(this, "Anda harus login", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val email = user.email ?: "unknown"

            val data = mapOf(
                "email" to email,
                "message" to masukan,
                "timestamp" to System.currentTimeMillis()
            )

            val dbRef = FirebaseDatabase.getInstance().getReference("suggestions")
            val newKey = dbRef.push().key

            if (newKey != null) {
                dbRef.child(newKey).setValue(data).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        MotionToast.createToast(
                            this,
                            "Berhasil!",
                            "Terima kasih atas masukkan anda!",
                            MotionToastStyle.SUCCESS,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(this, R.font.poppinsregular))
                        // Kembali ke DashboardActivity
                        val intent = Intent(this, DashboardActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        MotionToast.createToast(
                            this,
                            "Gagal!",
                            "Tidak berhasil mengirim!",
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(this, R.font.poppinsregular))
                    }
                }
            }
        }
    }
}
