package com.teamdev.mykasir.ui.product

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.cloudinary.Cloudinary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.teamdev.mykasir.R
import com.teamdev.mykasir.config.CloudinaryConfig
import com.teamdev.mykasir.databinding.ActivityEditProductBinding
import com.teamdev.mykasir.model.Product

class EditProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProductBinding
    private var selectedImageUri: Uri? = null
    private lateinit var cloudinary: Cloudinary
    private val CAMERA_PERMISSION_REQUEST_CODE = 100
    private var currentImageUrl: String = "" // untuk simpan image lama



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inisialisasi binding
        binding = ActivityEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cloudinary = CloudinaryConfig.cloudinary

        binding.btnCamera.setOnClickListener { openCamera() }
        binding.btnGallery.setOnClickListener { openGallery() }

        // Setup Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar) // Set Toolbar sebagai ActionBar

        // Tambahkan tombol navigasi
        toolbar.setNavigationIcon(R.drawable.vector) // pastikan vector adalah gambar untuk tombol navigasi
        toolbar.setNavigationOnClickListener {
            // Navigasi kembali ke ProductActivity
            onBackPressed()
        }


        // Set listener untuk window insets agar tampilan lebih bersih
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sku = intent.getStringExtra("sku") ?: run {
            Log.e("EditProductActivity", "SKU tidak ditemukan di intent")
            Toast.makeText(this, "SKU tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        Log.d("EditProductActivity", "SKU yang diterima: $sku")


        // Memuat data produk dari Firebase
        fetchProductData(sku)

        // Tombol Simpan
        binding.btnSimpan.setOnClickListener {
            saveProductData(sku)
        }
    }

    private fun openCamera() {
        val imageFile = java.io.File(cacheDir, "camera_${System.currentTimeMillis()}.jpg")
        val imageUri = androidx.core.content.FileProvider.getUriForFile(this, "$packageName.provider", imageFile)
        selectedImageUri = imageUri
        val cameraIntent = android.content.Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageUri)
        }
        cameraResultLauncher.launch(cameraIntent)
    }

    private fun openGallery() {
        val galleryIntent = android.content.Intent(android.content.Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryResultLauncher.launch(galleryIntent)
    }

    private val cameraResultLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && selectedImageUri != null) {
            startCrop(selectedImageUri!!)
        }
    }

    private val galleryResultLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val photoUri = result.data?.data
            if (photoUri != null) startCrop(photoUri)
        }
    }

    private fun startCrop(uri: Uri) {
        val destinationUri = Uri.fromFile(java.io.File(cacheDir, "cropped_${System.currentTimeMillis()}.jpg"))
        val options = com.yalantis.ucrop.UCrop.Options().apply {
            setCompressionFormat(android.graphics.Bitmap.CompressFormat.JPEG)
            setCompressionQuality(75)
        }

        val cropIntent = com.yalantis.ucrop.UCrop.of(uri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(800, 800)
            .withOptions(options)
            .getIntent(this)

        cropResultLauncher.launch(cropIntent)
    }

    private val cropResultLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val resultUri = com.yalantis.ucrop.UCrop.getOutput(result.data!!)
            if (resultUri != null) {
                selectedImageUri = resultUri
                binding.imagePreview.setImageURI(resultUri)
            }
        } else if (result.resultCode == com.yalantis.ucrop.UCrop.RESULT_ERROR) {
            val cropError = com.yalantis.ucrop.UCrop.getError(result.data!!)
            Toast.makeText(this, "Crop error: ${cropError?.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun fetchProductData(sku: String) {
        val database = FirebaseDatabase.getInstance("https://mykasir-cae5d-default-rtdb.asia-southeast1.firebasedatabase.app")
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Log.e("EditProductActivity", "User tidak login")
            Toast.makeText(this@EditProductActivity, "User tidak login", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val ref = database.getReference("users").child(userId).child("products").child(sku)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("EditProductActivity", "Snapshot: ${snapshot.value}")
                if (snapshot.exists()) {
                    val product = snapshot.getValue(Product::class.java)
                    product?.let {
                        Log.d("EditProductActivity", "Produk ditemukan: ${it.nama}, ${it.imageUrl}")
                        binding.etNama.setText(it.nama)
                        binding.etKategori.setText(it.kategori)
                        binding.etHargaBeli.setText(it.hargaBeli)
                        binding.etHargaJual.setText(it.hargaJual)
                        binding.etStok.setText(it.stok)
                        binding.etSKU.setText(it.sku)

                        if (it.imageUrl.isNotEmpty()) {
                            currentImageUrl = it.imageUrl
                            Glide.with(this@EditProductActivity)
                                .load(currentImageUrl.replace("http://", "https://"))
                                .error(R.drawable.error_image)
                                .into(binding.imagePreview)
                        }

                    }
                } else {
                    Toast.makeText(this@EditProductActivity, "Produk tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("EditProductActivity", "Gagal memuat data: ${error.message}")
                Toast.makeText(this@EditProductActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveProductData(sku: String) {
        val nama = binding.etNama.text.toString()
        val kategori = binding.etKategori.text.toString()
        val hargaBeli = binding.etHargaBeli.text.toString()
        val hargaJual = binding.etHargaJual.text.toString()
        val stok = binding.etStok.text.toString()
        val tanggal = getCurrentDateInIndonesian()

        if (nama.isEmpty() || kategori.isEmpty() || hargaBeli.isEmpty() || hargaJual.isEmpty() || stok.isEmpty()) {
            Toast.makeText(this, "Semua kolom harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageUri != null) {
            showLoading(true)
            uploadImageToCloudinary(selectedImageUri!!, sku, nama, kategori, hargaBeli, hargaJual, stok, tanggal)
        } else {
            // Gambar tidak diubah, pakai gambar lama dari tag
            val imageUrl = currentImageUrl
            Glide.with(this)
                .load(imageUrl.replace("http://", "https://"))
                .error(R.drawable.error_image)
                .into(binding.imagePreview)

            updateProductToDatabase(imageUrl, sku, nama, kategori, hargaBeli, hargaJual, stok, tanggal)
        }

    }

    private fun uploadImageToCloudinary(uri: Uri, sku: String, nama: String, kategori: String, hargaBeli: String, hargaJual: String, stok: String, tanggal: String) {
        val inputStream = contentResolver.openInputStream(uri)
        val file = java.io.File.createTempFile("image_", ".jpg", cacheDir)
        inputStream?.copyTo(file.outputStream())

        val params = com.cloudinary.utils.ObjectUtils.asMap(
            "public_id", "produk_$sku",
            "folder", "produk_images"
        )

        Thread {
            try {
                val result = cloudinary.uploader().upload(file, params)
                val imageUrl = result["url"] as String
                runOnUiThread {
                    updateProductToDatabase(imageUrl, sku, nama, kategori, hargaBeli, hargaJual, stok, tanggal)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    showLoading(false)
                    Toast.makeText(this, "Gagal upload gambar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun updateProductToDatabase(imageUrl: String, sku: String, nama: String, kategori: String, hargaBeli: String, hargaJual: String, stok: String, tanggal: String) {
        val updatedProduct = Product(
            sku = sku,
            nama = nama,
            kategori = kategori,
            hargaBeli = hargaBeli,
            hargaJual = hargaJual,
            stok = stok,
            tanggal = tanggal,
            imageUrl = imageUrl
        )

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance("https://mykasir-cae5d-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users").child(userId).child("products").child(sku)

        ref.setValue(updatedProduct).addOnSuccessListener {
            showLoading(false)
            Toast.makeText(this, "Produk berhasil diperbarui", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener {
            showLoading(false)
            Toast.makeText(this, "Gagal memperbarui produk", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.btnSimpan.isEnabled = !isLoading
        binding.btnSimpan.text = if (isLoading) "Menyimpan..." else "Simpan"
    }

    private fun getCurrentDateInIndonesian(): String {
        val locale = java.util.Locale("id", "ID")
        val dateFormat = java.text.SimpleDateFormat("dd MMMM yyyy", locale)
        return dateFormat.format(java.util.Calendar.getInstance().time)
    }

}