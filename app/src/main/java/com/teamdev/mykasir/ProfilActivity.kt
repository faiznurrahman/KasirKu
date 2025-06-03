package com.teamdev.mykasir

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.bumptech.glide.Glide
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.widget.Button
import androidx.core.content.res.ResourcesCompat
import com.cloudinary.Cloudinary
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.teamdev.mykasir.config.CloudinaryConfig
import com.yalantis.ucrop.UCrop
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.io.File
import java.util.*



class ProfilActivity : AppCompatActivity() {

    private lateinit var etNama: EditText
    private lateinit var etEmail: EditText
    private lateinit var etTelp: EditText
    private lateinit var etAlamat: EditText
    private lateinit var imagePreview: ImageView
    private lateinit var cloudinary: Cloudinary


    private var imageUri: Uri? = null
    private var croppedImageUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profil)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

// Aktifkan tombol kembali
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Supaya teks pakai TextView buatan kamu

// Tangani klik ikon navigasi
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        // Inisialisasi komponen UI
        etNama = findViewById(R.id.etNama)
        etEmail = findViewById(R.id.etEmail)
        etTelp = findViewById(R.id.etTelp)
        etAlamat = findViewById(R.id.etAlamat)
        imagePreview = findViewById(R.id.imagePreview)

        CloudinaryConfig.initCloudinary(this)

        // Inisialisasi Cloudinary
        cloudinary = CloudinaryConfig.cloudinary


        // Ambil data dari Firebase
        loadUserData()

        findViewById<Button>(R.id.btnCamera).setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 100)
        }

        findViewById<Button>(R.id.btnGallery).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 101)
        }

        findViewById<Button>(R.id.btnSimpan).setOnClickListener {
            saveUserData()
        }

    }

    private fun loadUserData() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "User belum login", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = user.uid
        val userRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("users").child(uid)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    etNama.setText(snapshot.child("nama").getValue(String::class.java) ?: "")
                    etEmail.setText(snapshot.child("email").getValue(String::class.java) ?: "")
                    etTelp.setText(snapshot.child("phone").getValue(String::class.java) ?: "")
                    etAlamat.setText(snapshot.child("address").getValue(String::class.java) ?: "")

                    val imageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this@ProfilActivity)
                            .load(imageUrl.replace("http://", "https://")) // Ganti http ke https kalau perlu
                            .error(R.drawable.error_image) // Gambar default jika gagal load
                            .into(imagePreview)

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProfilActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                100 -> { // Kamera
                    val bitmap = data?.extras?.get("data") as Bitmap
                    val tempUri = Uri.parse(MediaStore.Images.Media.insertImage(contentResolver, bitmap, "profile", null))
                    startCrop(tempUri)
                }
                101 -> { // Galeri
                    data?.data?.let { startCrop(it) }
                }
                UCrop.REQUEST_CROP -> {
                    croppedImageUri = UCrop.getOutput(data!!)
                    imagePreview.setImageURI(croppedImageUri)
                }
            }
        }
    }
    private fun startCrop(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "cropped_${System.currentTimeMillis()}.jpg"))
        val options = UCrop.Options()

        options.setCompressionFormat(Bitmap.CompressFormat.JPEG)
        options.setCompressionQuality(95) // Naikin kualitas dari 80 ke 95
        options.setHideBottomControls(true) // Tambahan: biar crop lebih clean
        options.setFreeStyleCropEnabled(false) // Tetap rasio 1:1

        UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(1024, 1024) // Ukuran lebih besar dari sebelumnya
            .withOptions(options)
            .start(this)
    }


    private fun saveUserData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)

        val nama = etNama.text.toString()
        val email = etEmail.text.toString()
        val telp = etTelp.text.toString()
        val alamat = etAlamat.text.toString()

        if (croppedImageUri != null) {
            MediaManager.get().upload(croppedImageUri)
                .callback(object : com.cloudinary.android.callback.UploadCallback {
                    override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                        val imageUrl = resultData?.get("secure_url").toString()
                        val data = mapOf(
                            "nama" to nama,
                            "email" to email,
                            "phone" to telp,
                            "address" to alamat,
                            "profileImageUrl" to imageUrl,
                            "uid" to uid
                        )
                        userRef.updateChildren(data)
                        Toast.makeText(this@ProfilActivity, "Profil berhasil disimpan", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        Toast.makeText(this@ProfilActivity, "Upload gagal: ${error?.description}", Toast.LENGTH_SHORT).show()
                    }

                    override fun onStart(requestId: String?) {}
                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                }).dispatch()
        } else {
            val data = mapOf(
                "nama" to nama,
                "email" to email,
                "phone" to telp,
                "address" to alamat,
                "uid" to uid
            )
            userRef.updateChildren(data)
            MotionToast.createToast(
                this,
                "Berhasil!",
                "Profil Berhasil Disimpan!",
                MotionToastStyle.SUCCESS,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(this, R.font.poppinsregular))
        }
    }



}
