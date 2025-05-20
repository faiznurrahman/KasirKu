package com.teamdev.mykasir.model

import com.google.firebase.database.Exclude

data class Cashier(
    var nama: String? = null,
    var hargaJual: String? = "0",
    var stok: String? = "0",
    var sku: String? = null,
    var imageUrl: String? = null,
    var quantity: Int = 0
) {
    val price: Double
        get() = hargaJual?.toDoubleOrNull() ?: 0.0

    val stock: Int
        get() = stok?.toIntOrNull() ?: 0

    val name: String
        get() = nama ?: ""
}

