package com.chad.pos.device

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import jpos.JposException
import jpos.Scanner
import jpos.ScannerConst
import jpos.events.DataEvent
import jpos.events.DataListener

private val displayText = mutableStateOf("--")

class BarCodeReaderDataListener(val scanner: Scanner) : DataListener {
  override fun dataOccurred(event: DataEvent?) {
    displayText.value = "--"

    try {
      val scanDataLabelBytes = scanner.scanDataLabel
      val scanDataTypeCode = scanner.scanDataType

      if (scanDataLabelBytes.isNotEmpty()) {
        val scanDataLabel = scanDataLabelBytes.toString(Charsets.US_ASCII)
        val barCodeTypeName = getBarCodeTypeName(scanDataTypeCode)
        println("Scanned $barCodeTypeName: $scanDataLabel")

        displayText.value = "$scanDataLabel [$barCodeTypeName]"
      }
    } catch (ex: JposException) {
      System.err.println("ERROR: JposException during DataEvent: ${ex.message}")
    }

    // Data events are auto-disabled after event trigger, must re-enable to get future barcode reads
    try {
      scanner.dataEventEnabled = true
    } catch (ex: JposException) {
      System.err.println("ERROR: Could not enable data event after event trigger, will be unable to receive barcodes: ${ex.message}")
    }
  }

  private fun getBarCodeTypeName(code: Int): String {
    return when (code) {
      ScannerConst.SCAN_SDT_UPCA -> "UPC-A"
      ScannerConst.SCAN_SDT_UPCE -> " UPC-E"
      ScannerConst.SCAN_SDT_Code39 -> "Code 39"
      ScannerConst.SCAN_SDT_Code128 -> "Code 128"
      ScannerConst.SCAN_SDT_EAN8_S -> "EAN-8 with Supplemental"
      ScannerConst.SCAN_SDT_EAN13_S -> "EAN-13 with Supplemental"
      ScannerConst.SCAN_SDT_EAN128 -> "EAN-128"
      ScannerConst.SCAN_SDT_QRCODE -> "QR Code"
      ScannerConst.SCAN_SDT_OTHER -> "Other"
      else -> "Unknown"
    }
  }
}

object ChadScanner {
  private val scanner = Scanner()
  private val dataListener = BarCodeReaderDataListener(scanner)

  fun getDisplayText(): MutableState<String> {
    return displayText
  }

  fun connect(profile: String): Boolean {
    println("INFO: Connecting to scanner...")

    try {
      scanner.open(profile)
    } catch (ex: JposException) {
      System.err.println("ERROR: Failed to open scanner: ${ex.message}")
      return false
    }

    try {
      scanner.claim(1000)
      scanner.deviceEnabled = true
      scanner.dataEventEnabled = true
    } catch (ex: JposException) {
      close()
      System.err.println("ERROR: Failed to connect to scanner: ${ex.message}")
      return false
    }

    scanner.addDataListener(dataListener)
    println("INFO: Scanner connected.")

    return true
  }

  fun disconnect(): Boolean {
    println("INFO: Disconnecting scanner...")
    scanner.removeDataListener(dataListener)

    try {
      scanner.deviceEnabled = false
      scanner.release()
    } catch (ex: JposException) {
      System.err.println("ERROR: Failed to disconnect from scanner: ${ex.message}")
      return false
    }

    close()
    println("INFO: Scanner disconnected.")

    return true
  }

  private fun close(): Boolean {
    return try {
      scanner.close()
      true
    } catch (ex: JposException) {
      System.err.println("ERROR: Failed to close scanner: ${ex.message}")
      false
    }
  }
}
