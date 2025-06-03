package com.teamdev.mykasir.base

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.teamdev.mykasir.R

class HeaderActivity : AppCompatActivity() {

    private lateinit var textNama: TextView
    private lateinit var textEmail: TextView
    private lateinit var profileImageView: ImageView

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_header)

        textNama = findViewById(R.id.textView8)
        textEmail = findViewById(R.id.textView9)
        profileImageView = findViewById(R.id.profileImageView)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://kasirku-7ce1b-default-rtdb.asia-southeast1.firebasedatabase.app").reference

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userRef = database.child("users").child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nama = snapshot.child("nama").getValue(String::class.java)
                    val email = snapshot.child("email").getValue(String::class.java)
                    val profileUrl = snapshot.child("profileImageUrl").getValue(String::class.java)

                    textNama.text = nama ?: "Nama tidak ditemukan"
                    textEmail.text = email ?: "Email tidak ditemukan"

                    // Load image using Glide
                    Glide.with(this@HeaderActivity)
                        .load(profileUrl?.replace("http://", "https://"))
                        .error(R.drawable.blankprofil)
                        .into(profileImageView)
                }

                override fun onCancelled(error: DatabaseError) {
                    textNama.text = "Gagal memuat data"
                    textEmail.text = "Gagal memuat data"
                }
            })
        }
    }
}
