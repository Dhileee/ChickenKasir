package com.fadhil.chickenkasir.utils

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import java.io.OutputStream
import java.util.UUID

class BluetoothPrinter(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    companion object {
        private val PRINTER_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    fun hasBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun getPairedDevices(): List<BluetoothDevice> {
        if (!hasBluetoothPermission()) return emptyList()
        return bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
    }

    fun connectToPrinter(device: BluetoothDevice): Boolean {
        if (!hasBluetoothPermission()) return false

        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(PRINTER_UUID)
            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun printText(text: String) {
        try {
            outputStream?.write(text.toByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun printLine(text: String) {
        printText(text + "\n")
    }

    fun printBold(text: String) {
        try {
            outputStream?.write(byteArrayOf(0x1B, 0x45, 0x01)) // Bold ON
            printText(text)
            outputStream?.write(byteArrayOf(0x1B, 0x45, 0x00)) // Bold OFF
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun printCenter(text: String) {
        try {
            outputStream?.write(byteArrayOf(0x1B, 0x61, 0x01)) // Center align
            printLine(text)
            outputStream?.write(byteArrayOf(0x1B, 0x61, 0x00)) // Left align
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun printDivider() {
        printLine("--------------------------------")
    }

    fun feedPaper(lines: Int = 3) {
        try {
            for (i in 0 until lines) {
                outputStream?.write("\n".toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        try {
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}