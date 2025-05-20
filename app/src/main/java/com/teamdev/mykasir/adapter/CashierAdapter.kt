package com.teamdev.mykasir.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.teamdev.mykasir.R
import com.teamdev.mykasir.model.Cashier

class CashierAdapter(
    private val productList: MutableList<Cashier>,
    private val onQuantityChanged: (Cashier) -> Unit
) : RecyclerView.Adapter<CashierAdapter.CashierViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CashierViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cashier, parent, false)
        return CashierViewHolder(view)
    }

    override fun onBindViewHolder(holder: CashierViewHolder, position: Int) {
        val product = productList[position]
        holder.bind(product)
    }

    override fun getItemCount(): Int = productList.size

    fun updateData(newList: List<Cashier>) {
        productList.clear()
        productList.addAll(newList)
        notifyDataSetChanged()
    }

    inner class CashierViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageProduct: ImageView = itemView.findViewById(R.id.imageProduct)
        private val textName: TextView = itemView.findViewById(R.id.textName)
        private val textStock: TextView = itemView.findViewById(R.id.textStock)
        private val textPrice: TextView = itemView.findViewById(R.id.textPrice)
        private val btnMinus: Button = itemView.findViewById(R.id.btnMinus)
        private val textQty: TextView = itemView.findViewById(R.id.textQty)
        private val btnPlus: Button = itemView.findViewById(R.id.btnPlus)

        fun bind(product: Cashier) {
            textName.text = product.name
            textStock.text = "Stok: ${product.stock}"
            textPrice.text = "Rp ${product.price.toInt()}"

            Glide.with(itemView.context)
                .load(product.imageUrl?.replace("http://", "https://"))
                .into(imageProduct)

            // Set nilai awal quantity ke 0 jika belum disetel
            if (product.quantity <= 0) {
                product.quantity = 0
            }
            textQty.text = product.quantity.toString()

            // Atur tombol minus: nonaktif kalau quantity 0
            btnMinus.isEnabled = product.quantity > 0

            // Tombol plus
            btnPlus.setOnClickListener {
                if (product.stock == 0) {
                    // Jika stok produk = 0, menampilkan toast dan menonaktifkan tombol plus
                    Toast.makeText(itemView.context, "Produk Habis", Toast.LENGTH_SHORT).show()
                    btnPlus.isEnabled = false // Menonaktifkan tombol plus
                } else {
                    // Jika stok lebih dari 0, menambah quantity
                    product.quantity += 1
                    textQty.text = product.quantity.toString()
                    btnMinus.isEnabled = true // Mengaktifkan tombol minus
                    onQuantityChanged(product)
                }
            }

            // Tombol minus
            btnMinus.setOnClickListener {
                if (product.quantity > 0) {
                    product.quantity -= 1
                    textQty.text = product.quantity.toString()
                    btnMinus.isEnabled = product.quantity > 0
                    onQuantityChanged(product)
                }
            }
        }
    }
}

