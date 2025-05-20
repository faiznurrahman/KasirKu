package com.teamdev.mykasir.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.teamdev.mykasir.R
import com.teamdev.mykasir.model.DetailBarang

class DetailBarangAdapter : RecyclerView.Adapter<DetailBarangAdapter.ViewHolder>() {
    private val data = mutableListOf<DetailBarang>()

    fun setData(newData: List<DetailBarang>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nama = view.findViewById<TextView>(R.id.txtNamaBarang)
        val jumlah = view.findViewById<TextView>(R.id.txtJumlah)
        val harga = view.findViewById<TextView>(R.id.txtHarga)
        val subtotal = view.findViewById<TextView>(R.id.txtSubtotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_struk, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.nama.text = item.nama
        holder.jumlah.text = "${item.jumlah}x"
        holder.harga.text = "Rp${item.harga}"
        holder.subtotal.text = "Rp${item.subtotal}"
    }

    override fun getItemCount(): Int = data.size
}
