package com.teamdev.mykasir.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Pesanan(
    val namaBarang: String,
    val qty: Int,
    val harga: Double,
    val subtotal: Double
) : Parcelable
