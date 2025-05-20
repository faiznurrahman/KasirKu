package com.teamdev.mykasir.ui.product

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.teamdev.mykasir.R
import com.teamdev.mykasir.config.CloudinaryConfig
import com.teamdev.mykasir.databinding.ActivityAddProductBinding
import com.yalantis.ucrop.UCrop
import java.io.File as JavaFile
import java.text.SimpleDateFormat
import java.util.*

class AddProductActivity : AppCompatActivity() {

    private val CAMERA_PERMISSION_REQUEST_CODE = 100
    private var selectedImageUri: Uri? = null

    private lateinit var binding: ActivityAddProductBinding
    private lateinit var cloudinary: Cloudinary

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Cloudinary
        cloudinary = CloudinaryConfig.cloudinary

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }

        binding.btnCamera.setOnClickListener { openCamera() }
        binding.btnGallery.setOnClickListener { openGallery() }
        binding.btnSimpan.setOnClickListener { saveProduct() }

        // Setup Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar) // Set Toolbar sebagai ActionBar

        // Tambahkan tombol navigasi
        toolbar.setNavigationIcon(R.drawable.vector) // pastikan vector adalah gambar untuk tombol navigasi
        toolbar.setNavigationOnClickListener {
            // Navigasi kembali ke ProductActivity
            onBackPressed()
        }
    }

    private fun openCamera() {
        val imageFile = JavaFile(cacheDir, "camera_${System.currentTimeMillis()}.jpg")
        val imageUri = FileProvider.getUriForFile(this, "${packageName}.provider", imageFile)
        selectedImageUri = imageUri
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        }
        cameraResultLauncher.launch(cameraIntent)
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryResultLauncher.launch(galleryIntent)
    }

    private val cameraResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && selectedImageUri != null) {
            startCrop(selectedImageUri!!)
        }
    }

    private val galleryResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val photoUri: Uri? = result.data?.data
            if (photoUri != null) startCrop(photoUri)
        }
    }

    private fun startCrop(uri: Uri) {
        val destinationUri = Uri.fromFile(JavaFile(cacheDir, "cropped_${System.currentTimeMillis()}.jpg"))
        val options = UCrop.Options().apply {
            setCompressionFormat(Bitmap.CompressFormat.JPEG)
            setCompressionQuality(75) // Angka 1–100, makin rendah makin kecil file
        }

        val cropIntent = UCrop.of(uri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(800, 800)
            .withOptions(options)
            .getIntent(this)

        cropResultLauncher.launch(cropIntent)
    }

    private val cropResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val resultUri = UCrop.getOutput(result.data!!)
            if (resultUri != null) {
                binding.imagePreview.setImageURI(resultUri)
                selectedImageUri = resultUri
            }
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(result.data!!)
            Toast.makeText(this, "Crop error: ${cropError?.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveProduct() {
        val nama = binding.etNama.text.toString().trim()
        val kategori = binding.etKategori.text.toString().trim()
        val hargaBeli = binding.etHargaBeli.text.toString().trim()
        val hargaJual = binding.etHargaJual.text.toString().trim()
        val stok = binding.etStok.text.toString().trim()
        val sku = binding.etSKU.text.toString().trim()

        if (nama.isEmpty() || kategori.isEmpty() || hargaBeli.isEmpty() || hargaJual.isEmpty() || stok.isEmpty() || sku.isEmpty()) {
            Toast.makeText(this, "Semua kolom harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageUri != null) {
            showLoading(true)
            uploadImageToCloudinary(selectedImageUri!!, nama, kategori, hargaBeli, hargaJual, stok, sku)
        } else {
            Toast.makeText(this, "Pilih gambar produk", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToCloudinary(uri: Uri, nama: String, kategori: String, hargaBeli: String, hargaJual: String, stok: String, sku: String) {
        val inputStream = contentResolver.openInputStream(uri)
        val file = JavaFile.createTempFile("image_", ".jpg", cacheDir)

        inputStream?.copyTo(file.outputStream())

        val params = ObjectUtils.asMap(
            "public_id", "produk_${System.currentTimeMillis()}",
            "folder", "produk_images"
        )

        Thread {
            try {
                val uploadResult = cloudinary.uploader().upload(file, params)
                val imageUrl = uploadResult["url"] as String
                runOnUiThread {
                    saveProductToDatabase(imageUrl, nama, kategori, hargaBeli, hargaJual, stok, sku)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    showLoading(false)
                    Toast.makeText(this, "Gagal upload ke Cloudinary: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun saveProductToDatabase(imageUrl: String, nama: String, kategori: String, hargaBeli: String, hargaJual: String, stok: String, sku: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid != null) {
            // Mendapatkan tanggal saat ini
            val currentDate = getCurrentDateInIndonesian()

            val productData = mapOf(
                "nama" to nama,
                "kategori" to kategori,
                "hargaBeli" to hargaBeli,
                "hargaJual" to hargaJual,
                "stok" to stok,
                "sku" to sku,
                "imageUrl" to imageUrl,
                "tanggal" to currentDate  // Menambahkan tanggal dimasukkan
            )

            val productRef = FirebaseDatabase
                .getInstance("https://mykasir-cae5d-default-rtdb.asia-southeast1.firebasedatabase.app")
                .reference
                .child("users")
                .child(uid)
                .child("products")
                .child(sku)

            productRef.setValue(productData)
                .addOnSuccessListener {
                    showLoading(false)
                    Toast.makeText(this, "Produk berhasil disimpan", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, ProductActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    showLoading(false)
                    Toast.makeText(this, "Gagal menyimpan produk: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            showLoading(false)
            Toast.makeText(this, "User tidak terautentikasi", Toast.LENGTH_SHORT).show()
        }
    }

    // Fungsi untuk mendapatkan tanggal dalam format Indonesia
    private fun getCurrentDateInIndonesian(): String {
        val localeID = Locale("id", "ID")  // Bahasa Indonesia
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", localeID) // Format tanggal (tanggal bulan tahun)
        val currentDate = Calendar.getInstance().time
        return dateFormat.format(currentDate)  // Mengembalikan tanggal dalam format dd MMMM yyyy
    }

    private fun showLoading(isLoading: Boolean) {
        binding.btnSimpan.isEnabled = !isLoading
        binding.btnSimpan.text = if (isLoading) "Menyimpan..." else "Simpan"
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Izin Kamera Diberikan", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Izin Kamera Ditolak", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
