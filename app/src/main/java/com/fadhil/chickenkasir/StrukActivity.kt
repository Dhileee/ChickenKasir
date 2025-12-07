package com.fadhil.chickenkasir

import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.fadhil.chickenkasir.utils.BluetoothPrinter
import com.fadhil.chickenkasir.utils.CartItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StrukActivity : AppCompatActivity() {

    private lateinit var bluetoothPrinter: BluetoothPrinter
    private val BLUETOOTH_PERMISSION_CODE = 100

    private var transaksiId: String = ""
    private var cartItems: List<CartItem> = emptyList()
    private var total: Int = 0
    private var uangBayar: Int = 0
    private var kembalian: Int = 0
    private var metodePembayaran: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_struk)

        bluetoothPrinter = BluetoothPrinter(this)

        // Get data dari intent
        transaksiId = intent.getStringExtra("transaksiId") ?: ""
        val cartItemsJson = intent.getStringExtra("cartItems") ?: "[]"
        val gson = Gson()
        val type = object : TypeToken<List<CartItem>>() {}.type
        cartItems = gson.fromJson(cartItemsJson, type)
        total = intent.getIntExtra("total", 0)
        uangBayar = intent.getIntExtra("uangBayar", 0)
        kembalian = intent.getIntExtra("kembalian", 0)
        metodePembayaran = intent.getStringExtra("metodePembayaran") ?: ""

        setupViews()
    }

    private fun setupViews() {
        val tvNoTransaksi = findViewById<TextView>(R.id.tvNoTransaksi)
        val tvTanggal = findViewById<TextView>(R.id.tvTanggal)
        val llItemContainer = findViewById<LinearLayout>(R.id.llItemContainer)
        val tvTotalStruk = findViewById<TextView>(R.id.tvTotalStruk)
        val tvMetodePembayaran = findViewById<TextView>(R.id.tvMetodePembayaran)
        val llCashDetails = findViewById<LinearLayout>(R.id.llCashDetails)
        val tvUangBayar = findViewById<TextView>(R.id.tvUangBayar)
        val tvKembalianStruk = findViewById<TextView>(R.id.tvKembalianStruk)
        val btnPrintStruk = findViewById<Button>(R.id.btnPrintStruk)
        val btnSaveStruk = findViewById<Button>(R.id.btnSaveStruk)
        val btnTutupStruk = findViewById<Button>(R.id.btnTutupStruk)

        tvNoTransaksi.text = "No: #${transaksiId.take(8)}"

        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        tvTanggal.text = "Tanggal: ${dateFormat.format(Date())}"

        cartItems.forEach { item ->
            val itemView = layoutInflater.inflate(android.R.layout.simple_list_item_2, llItemContainer, false)
            val text1 = itemView.findViewById<TextView>(android.R.id.text1)
            val text2 = itemView.findViewById<TextView>(android.R.id.text2)

            text1.text = "${item.menu.nama} x${item.jumlah}"
            text1.textSize = 14f
            text1.setTextColor(resources.getColor(android.R.color.black, null))
            text1.typeface = Typeface.MONOSPACE

            text2.text = "Rp ${String.format("%,d", item.getSubtotal())}"
            text2.textSize = 14f
            text2.setTextColor(resources.getColor(android.R.color.black, null))
            text2.typeface = Typeface.MONOSPACE

            llItemContainer.addView(itemView)
        }

        tvTotalStruk.text = "Total: Rp ${String.format("%,d", total)}"
        tvMetodePembayaran.text = "Metode: $metodePembayaran"

        if (metodePembayaran == "Cash") {
            llCashDetails.visibility = android.view.View.VISIBLE
            tvUangBayar.text = "Bayar: Rp ${String.format("%,d", uangBayar)}"
            tvKembalianStruk.text = "Kembali: Rp ${String.format("%,d", kembalian)}"
        }

        btnPrintStruk.setOnClickListener {
            if (checkBluetoothPermission()) {
                showPrinterDialog()
            } else {
                requestBluetoothPermission()
            }
        }

        btnSaveStruk.setOnClickListener {
            saveStrukAsPDF()
        }

        btnTutupStruk.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun checkBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.BLUETOOTH_CONNECT,
                    android.Manifest.permission.BLUETOOTH_SCAN
                ),
                BLUETOOTH_PERMISSION_CODE
            )
        }
    }

    private fun showPrinterDialog() {
        if (!checkBluetoothPermission()) {
            requestBluetoothPermission()
            return
        }

        val devices = bluetoothPrinter.getPairedDevices()

        if (devices.isEmpty()) {
            Toast.makeText(this, "Tidak ada printer Bluetooth yang terhubung.\nSilakan pair printer di Settings Bluetooth.", Toast.LENGTH_LONG).show()
            return
        }

        val deviceNames = devices.map {
            if (checkBluetoothPermission()) it.name else "Unknown Device"
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Pilih Printer")
            .setItems(deviceNames) { _, which ->
                val selectedDevice = devices[which]
                printStruk(selectedDevice)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun printStruk(device: BluetoothDevice) {
        Thread {
            try {
                val connected = bluetoothPrinter.connectToPrinter(device)

                if (!connected) {
                    runOnUiThread {
                        Toast.makeText(this, "Gagal terhubung ke printer", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }

                val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                val tanggal = dateFormat.format(Date())

                bluetoothPrinter.printCenter("Chicken d'Kriuk")
                bluetoothPrinter.printCenter("Jl. Ayam No. 123, Jakarta")
                bluetoothPrinter.printDivider()
                bluetoothPrinter.printLine("No: #${transaksiId.take(8)}")
                bluetoothPrinter.printLine("Tanggal: $tanggal")
                bluetoothPrinter.printDivider()

                cartItems.forEach { item ->
                    bluetoothPrinter.printLine("${item.menu.nama} x${item.jumlah}")
                    bluetoothPrinter.printLine("  Rp ${String.format("%,d", item.getSubtotal())}")
                }

                bluetoothPrinter.printDivider()
                bluetoothPrinter.printBold("Total: Rp ${String.format("%,d", total)}")
                bluetoothPrinter.printLine("Metode: $metodePembayaran")

                if (metodePembayaran == "Cash") {
                    bluetoothPrinter.printLine("Bayar: Rp ${String.format("%,d", uangBayar)}")
                    bluetoothPrinter.printLine("Kembali: Rp ${String.format("%,d", kembalian)}")
                }

                bluetoothPrinter.printDivider()
                bluetoothPrinter.printCenter("Terima kasih!")
                bluetoothPrinter.printCenter("Chicken d'Kriuk")
                bluetoothPrinter.feedPaper(4)

                bluetoothPrinter.disconnect()

                runOnUiThread {
                    Toast.makeText(this, "Struk berhasil dicetak!", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Error print: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun saveStrukAsPDF() {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()

            paint.typeface = Typeface.MONOSPACE
            paint.textSize = 12f

            var yPos = 50f
            val xPos = 50f

            paint.textSize = 20f
            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            canvas.drawText("Chicken d'Kriuk", xPos, yPos, paint)
            yPos += 30f

            paint.textSize = 12f
            paint.typeface = Typeface.MONOSPACE
            canvas.drawText("Jl. Ayam Goreng No. 123, Jakarta", xPos, yPos, paint)
            yPos += 30f

            canvas.drawLine(xPos, yPos, 545f, yPos, paint)
            yPos += 20f

            canvas.drawText("No: #${transaksiId.take(8)}", xPos, yPos, paint)
            yPos += 20f

            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
            canvas.drawText("Tanggal: ${dateFormat.format(Date())}", xPos, yPos, paint)
            yPos += 30f

            canvas.drawLine(xPos, yPos, 545f, yPos, paint)
            yPos += 20f

            cartItems.forEach { item ->
                canvas.drawText("${item.menu.nama} x${item.jumlah}", xPos, yPos, paint)
                yPos += 20f
                canvas.drawText("  Rp ${String.format("%,d", item.getSubtotal())}", xPos, yPos, paint)
                yPos += 25f
            }

            canvas.drawLine(xPos, yPos, 545f, yPos, paint)
            yPos += 20f

            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            canvas.drawText("Total: Rp ${String.format("%,d", total)}", xPos, yPos, paint)
            yPos += 20f

            paint.typeface = Typeface.MONOSPACE
            canvas.drawText("Metode: $metodePembayaran", xPos, yPos, paint)
            yPos += 20f

            if (metodePembayaran == "Cash") {
                canvas.drawText("Bayar: Rp ${String.format("%,d", uangBayar)}", xPos, yPos, paint)
                yPos += 20f
                canvas.drawText("Kembali: Rp ${String.format("%,d", kembalian)}", xPos, yPos, paint)
                yPos += 20f
            }

            canvas.drawLine(xPos, yPos, 545f, yPos, paint)
            yPos += 20f

            canvas.drawText("Terima kasih!", xPos + 180f, yPos, paint)

            pdfDocument.finishPage(page)

            val filename = "Struk_${transaksiId.take(8)}_${System.currentTimeMillis()}.pdf"
            val file = File(getExternalFilesDir(null), filename)
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            pdfDocument.close()
            fos.close()

            Toast.makeText(this, "PDF tersimpan: ${file.name}", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            Toast.makeText(this, "Gagal simpan PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}