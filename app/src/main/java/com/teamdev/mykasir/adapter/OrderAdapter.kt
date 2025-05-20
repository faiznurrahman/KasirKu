package com.teamdev.mykasir.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.teamdev.mykasir.R
import com.teamdev.mykasir.model.Pesanan
import java.text.NumberFormat
import java.util.*

class PesananAdapter(private val list: List<Pesanan>) : RecyclerView.Adapter<PesananAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val namaBarang: TextView = view.findViewById(R.id.namaBarang)
        val harga: TextView = view.findViewById(R.id.harga)
        val qty: TextView = view.findViewById(R.id.qty)
        val subtotal: TextView = view.findViewById(R.id.subtotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.namaBarang.text = item.namaBarang
        holder.harga.text = formatRupiah(item.harga)
        holder.qty.text = "x${item.qty}"
        holder.subtotal.text = formatRupiah(item.subtotal)
    }

    // Fungsi untuk format Rupiah
    private fun formatRupiah(number: Double): String {
        val format = NumberFormat.getInstance(Locale("in", "ID"))
        return "Rp ${format.format(number)}"
    }
}
