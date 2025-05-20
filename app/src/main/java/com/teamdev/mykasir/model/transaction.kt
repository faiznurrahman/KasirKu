package com.teamdev.mykasir.model

data class Transaksi(
    val kodeTransaksi: String = "",
    val tanggal: String = "",
    val metodePembayaran: String = "",
    val jumlahUang: Int = 0,
    val totalHarga: Int = 0,
    val kembalian: Int = 0,
    val detailBarang: List<DetailBarang> = listOf()
)
