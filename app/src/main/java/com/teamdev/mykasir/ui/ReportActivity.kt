package com.teamdev.mykasir.ui

import android.os.Bundle
import android.view.View
import android.widget.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.teamdev.mykasir.R
import com.teamdev.mykasir.base.BaseActivity
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class ReportActivity : BaseActivity() {

    private var selectedOption: String = "Harian"
    private lateinit var btnOpenBottomSheet: Button

    // TextViews untuk laporan
    private lateinit var tvOmzet: TextView
    private lateinit var tvProfit: TextView
    private lateinit var tvJumlahTransaksi: TextView
    private lateinit var tvProdukTerjual: TextView

    override fun onResume() {
        super.onResume()
        setCheckedNavigationItem(R.id.nav_laporan)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentLayout(R.layout.activity_report)
        setToolbarTitle("Laporan")
        setCheckedNavigationItem(R.id.nav_laporan)

        btnOpenBottomSheet = findViewById(R.id.btnOpenBottomSheet)
        tvOmzet = findViewById(R.id.tvOmzet)
        tvProfit = findViewById(R.id.tvProfit)
        tvJumlahTransaksi = findViewById(R.id.tvJumlahTransaksi)
        tvProdukTerjual = findViewById(R.id.tvProdukTerjual)

        btnOpenBottomSheet.text = "Periode $selectedOption"

        btnOpenBottomSheet.setOnClickListener {
            showPeriodBottomSheet()
        }

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        loadReportData("Harian", today)

    }


    private fun showPeriodBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_period_picker, null)
        dialog.setContentView(view)

        val optionDaily = view.findViewById<TextView>(R.id.optionDaily)
        val optionMonthly = view.findViewById<TextView>(R.id.optionMonthly)
        val optionYearly = view.findViewById<TextView>(R.id.optionYearly)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnSelect = view.findViewById<Button>(R.id.btnSelect)

        val resetSelection: () -> Unit = {
            optionDaily.setBackgroundResource(R.color.white)
            optionMonthly.setBackgroundResource(R.color.white)
            optionYearly.setBackgroundResource(R.color.white)
        }

        optionDaily.setOnClickListener {
            resetSelection()
            optionDaily.setBackgroundResource(R.drawable.bglightblue)
            selectedOption = "Harian"
        }

        optionMonthly.setOnClickListener {
            resetSelection()
            optionMonthly.setBackgroundResource(R.drawable.bglightblue)
            selectedOption = "Bulanan"
        }

        optionYearly.setOnClickListener {
            resetSelection()
            optionYearly.setBackgroundResource(R.drawable.bglightblue)
            selectedOption = "Tahunan"
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSelect.setOnClickListener {
            dialog.dismiss()
            btnOpenBottomSheet.text = "Periode $selectedOption"
            Toast.makeText(this, "Dipilih: $selectedOption", Toast.LENGTH_SHORT).show()

            when (selectedOption) {
                "Harian" -> showBottomSheetDayPicker()
                "Bulanan" -> showBottomSheetMonthPicker()
                "Tahunan" -> showBottomSheetYearPicker()
            }
        }

        dialog.show()
    }

    private fun showBottomSheetDayPicker() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_day, null)
        dialog.setContentView(view)

        val datePicker = view.findViewById<DatePicker>(R.id.datePicker)
        val btnPilih = view.findViewById<Button>(R.id.btnSelect)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)

        btnPilih.setOnClickListener {
            val day = datePicker.dayOfMonth
            val month = datePicker.month + 1
            val year = datePicker.year
            // Format tanggal jadi yyyy-MM-dd supaya cocok dengan format di Firebase
            val selected = "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
            Toast.makeText(this, "Tanggal dipilih: $selected", Toast.LENGTH_SHORT).show()
            dialog.dismiss()

            loadReportData("Harian", selected)
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun showBottomSheetMonthPicker() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_month, null)
        dialog.setContentView(view)

        val textYear = view.findViewById<TextView>(R.id.textYear)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnSelect = view.findViewById<Button>(R.id.btnSelect)
        val monthGrid = view.findViewById<GridLayout>(R.id.monthGrid)

        val calendar = Calendar.getInstance()
        var selectedMonth = calendar.get(Calendar.MONTH) + 1
        val selectedYear = calendar.get(Calendar.YEAR)
        textYear.text = selectedYear.toString()

        fun resetMonthButtonsBackground() {
            for (i in 0 until monthGrid.childCount) {
                val btn = monthGrid.getChildAt(i) as Button
                btn.setBackgroundColor(resources.getColor(android.R.color.white))
                btn.setTextColor(resources.getColor(android.R.color.black))
            }
        }

        resetMonthButtonsBackground()
        val defaultBtn = monthGrid.getChildAt(selectedMonth - 1) as Button
        defaultBtn.setBackgroundResource(R.color.blue)
        defaultBtn.setTextColor(resources.getColor(android.R.color.white))

        for (i in 0 until monthGrid.childCount) {
            val btn = monthGrid.getChildAt(i) as Button
            btn.setOnClickListener {
                resetMonthButtonsBackground()
                btn.setBackgroundResource(R.color.blue)
                btn.setTextColor(resources.getColor(android.R.color.white))
                selectedMonth = i + 1
            }
        }

        btnSelect.setOnClickListener {
            val result = "${selectedMonth.toString().padStart(2, '0')}/$selectedYear"
            Toast.makeText(this, "Periode Bulanan: $result", Toast.LENGTH_SHORT).show()
            dialog.dismiss()

            // Load data laporan bulanan berdasarkan periode MM/yyyy
            loadReportData("Bulanan", result)
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showBottomSheetYearPicker() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_year, null)
        dialog.setContentView(view)

        val yearPicker = view.findViewById<NumberPicker>(R.id.yearPicker)
        val btnPilih = view.findViewById<Button>(R.id.btnSelect)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        yearPicker.minValue = currentYear - 10
        yearPicker.maxValue = currentYear
        yearPicker.value = currentYear

        btnPilih.setOnClickListener {
            val selectedYear = yearPicker.value
            Toast.makeText(this, "Tahun dipilih: $selectedYear", Toast.LENGTH_SHORT).show()
            dialog.dismiss()

            // Load data laporan tahunan berdasarkan periode yyyy
            loadReportData("Tahunan", selectedYear.toString())
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun loadReportData(period: String, selectedDate: String) {
        tvOmzet.text = "Rp0"
        tvProfit.text = "Rp0"
        tvJumlahTransaksi.text = "0"
        tvProdukTerjual.text = "0"

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance().reference
        val userRef = db.child("users").child(uid)

        val sdfFull = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val sdfDateOnly = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        userRef.get().addOnSuccessListener { userSnapshot ->
            val purchaseSnapshot = userSnapshot.child("purchaseHistory")
            val productsSnapshot = userSnapshot.child("products")

            var omzet = 0L
            var profit = 0L
            var jumlahTransaksi = 0
            var produkTerjual = 0

            for (transSnap in purchaseSnapshot.children) {
                val tanggal = transSnap.child("tanggal").getValue(String::class.java) ?: continue

                val dateObj = try {
                    sdfFull.parse(tanggal)
                } catch (e: Exception) {
                    null
                }

                val isMatch = when (period) {
                    "Harian" -> {
                        if (dateObj != null) {
                            val dateOnly = sdfDateOnly.format(dateObj)
                            dateOnly == selectedDate
                        } else false
                    }
                    "Bulanan" -> {
                        if (dateObj != null) {
                            val cal = Calendar.getInstance()
                            cal.time = dateObj
                            val bulan = (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
                            val tahun = cal.get(Calendar.YEAR).toString()
                            "$bulan/$tahun" == selectedDate
                        } else false
                    }
                    "Tahunan" -> {
                        if (dateObj != null) {
                            val cal = Calendar.getInstance()
                            cal.time = dateObj
                            val tahun = cal.get(Calendar.YEAR).toString()
                            tahun == selectedDate
                        } else false
                    }
                    else -> false
                }

                if (!isMatch) continue

                jumlahTransaksi++

                val detailBarangSnap = transSnap.child("detailBarang")
                for (barangSnap in detailBarangSnap.children) {
                    val sku = barangSnap.child("sku").getValue(String::class.java) ?: continue
                    val hargaJual = barangSnap.child("harga").getValue(Long::class.java) ?: 0L
                    val jumlah = barangSnap.child("jumlah").getValue(Int::class.java) ?: 0

                    produkTerjual += jumlah
                    omzet += hargaJual * jumlah

                    // Ambil hargaBeli dari node /products/{sku}
                    val productNode = productsSnapshot.child(sku)
                    val hargaBeliStr = productNode.child("hargaBeli").getValue(String::class.java) ?: "0"
                    val hargaBeli = hargaBeliStr.toLongOrNull() ?: 0L

                    profit += (hargaJual - hargaBeli) * jumlah
                }
            }

            runOnUiThread {
                tvOmzet.text = "Rp${formatRupiah(omzet)}"
                tvProfit.text = "Rp${formatRupiah(profit)}"
                tvJumlahTransaksi.text = jumlahTransaksi.toString()
                tvProdukTerjual.text = produkTerjual.toString()

                val tvEmptyData = findViewById<TextView>(R.id.tvEmptyData)
                tvEmptyData.visibility = if (jumlahTransaksi == 0) View.VISIBLE else View.GONE
            }

        }.addOnFailureListener {
            Toast.makeText(this, "Gagal memuat data laporan", Toast.LENGTH_SHORT).show()
        }
    }



    private fun formatRupiah(value: Long): String {
        return String.format("%,d", value).replace(',', '.')
    }

}
