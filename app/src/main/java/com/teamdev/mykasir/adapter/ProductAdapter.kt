package com.teamdev.mykasir.adapter
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.teamdev.mykasir.R
import com.teamdev.mykasir.databinding.ItemProductBinding
import com.teamdev.mykasir.model.Product
import com.teamdev.mykasir.ui.product.EditProductActivity

class ProductAdapter(
    private val productList: MutableList<Product>,
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit,
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(private val binding: ItemProductBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            // Bind data
            Glide.with(binding.ivProduk.context)
                .load(product.imageUrl.replace("http://", "https://")) // otomatis pakai HTTPS
                .error(R.drawable.error_image)
                .into(binding.ivProduk)

            binding.tvNama.text = product.nama
            binding.tvHargaBeli.text = "Beli: Rp ${product.hargaBeli}"
            binding.tvHargaJual.text = "Jual: Rp ${product.hargaJual}"
            binding.tvStok.text = "Stok: ${product.stok}"
            binding.tvKategori.text = product.kategori
            binding.tvSKU.text = "SKU: ${product.sku}"
            binding.tvTanggal.text = "Terdaftar: ${product.tanggal}"

            // Set action for buttons
            binding.btnUbah.setOnClickListener {
                // Kirim SKU ke EditProductActivity
                val context = binding.root.context
                val intent = Intent(context, EditProductActivity::class.java)
                intent.putExtra("sku", product.sku) // Mengirim SKU produk
                context.startActivity(intent)
            }
            binding.btnHapus.setOnClickListener { onDeleteClick(product) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(productList[position])
    }

    override fun getItemCount(): Int = productList.size

    fun updateData(newList: List<Product>) {
        (productList as MutableList).clear()
        productList.addAll(newList)
        notifyDataSetChanged()
    }
}
