package com.chad.pos.device

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import jpos.JposException
import jpos.Scanner
import jpos.ScannerConst
import jpos.events.DataEvent
import jpos.events.DataListener
import kotlinx.coroutines.delay

private val displayText = mutableStateOf("--")

class BarcodeReaderDataListener(val scanner: Scanner) : DataListener {
  companion object {
    val barcodeSymbology = mapOf(
      ScannerConst.SCAN_SDT_UPCA to "UPC-A",
      ScannerConst.SCAN_SDT_UPCE to "UPC-E",
      ScannerConst.SCAN_SDT_UPCA_S to "UPC-A with Supplemental",
      ScannerConst.SCAN_SDT_UPCE_S to "UPC-E with Supplemental",
      ScannerConst.SCAN_SDT_UPCD2 to "UPC Add-on sub 2",
      ScannerConst.SCAN_SDT_UPCD5 to "UPC Add-on sub 5",
      ScannerConst.SCAN_SDT_EAN8 to "EAN-8/JAN-8",
      ScannerConst.SCAN_SDT_EAN13 to "EAN-13/JAN-13",
      ScannerConst.SCAN_SDT_EAN128 to "EAN-128",
      ScannerConst.SCAN_SDT_Code39 to "Code 39",
      ScannerConst.SCAN_SDT_Code128 to "Code 128",
      ScannerConst.SCAN_SDT_Code93 to "Code 93",
      ScannerConst.SCAN_SDT_ITF to "Interleaved 2 of 5",
      ScannerConst.SCAN_SDT_Code32 to "Italian Pharmacode",
      ScannerConst.SCAN_SDT_Codabar to "Codabar/NW7",
      ScannerConst.SCAN_SDT_MSI to "MSI",
      ScannerConst.SCAN_SDT_PLESSEY to "Plessey",
      ScannerConst.SCAN_SDT_GS1DATABAR to "GS1 DataBar",
    )
  }

  override fun dataOccurred(event: DataEvent?) {
    displayText.value = "--"

    try {
      val scanDataLabelBytes = scanner.scanDataLabel
      val scanDataTypeCode = scanner.scanDataType

      if (scanDataLabelBytes.isNotEmpty()) {
        val scanDataLabel = scanDataLabelBytes.toString(Charsets.US_ASCII)
        val barcodeTypeName = barcodeSymbology.getOrDefault(scanDataTypeCode, "(unknown)")
        displayText.value = "$scanDataLabel [$barcodeTypeName]"
      }
    } catch (ex: JposException) {
      System.err.println("JposException during DataEvent: ${ex.message}")
    }

    // Data events are auto-disabled after event trigger, must re-enable to get future barcode reads
    try {
      scanner.dataEventEnabled = true
    } catch (ex: JposException) {
      System.err.println("Could not enable data event after event trigger, will be unable to receive barcodes: ${ex.message}")
    }
  }
}

object ChadScanner {
  private val scanner = Scanner()
  private val dataListener = BarcodeReaderDataListener(scanner)

  fun getDisplayText(): MutableState<String> {
    return displayText
  }

  suspend fun connect(profile: String): Boolean {
    displayText.value = "Opening scanner..."

    try {
      scanner.open(profile)
    } catch (ex: JposException) {
      displayText.value = "Failed to open scanner: ${ex.message}"
      return false
    }

    displayText.value = "Claiming scanner..."

    try {
      scanner.claim(1000)
    } catch (ex: JposException) {
      displayText.value = "Failed to claim scanner: ${ex.message}"
      close()
      return false
    }

    displayText.value = "Enabling scanner..."

    try {
      scanner.deviceEnabled = true
      scanner.dataEventEnabled = true
    } catch (ex: JposException) {
      displayText.value = "Failed to enable scanner: ${ex.message}"
      close()
      return false
    }

    scanner.addDataListener(dataListener)
    displayText.value = "Scanner connected."

    return true
  }

  fun disconnect(): Boolean {
    displayText.value = "Disconnecting scanner..."
    scanner.removeDataListener(dataListener)

    try {
      displayText.value = "Disabling scanner..."
      scanner.deviceEnabled = false
    } catch (ex: JposException) {
      displayText.value = "Failed to disable scanner: ${ex.message}"
    }

    try {
      displayText.value = "Releasing scanner..."
      scanner.release()
    } catch (ex: JposException) {
      System.err.println("Failed to disconnect from scanner: ${ex.message}")
      return false
    }

    close()
    displayText.value = "Scanner disconnected."

    return true
  }

  private fun close(): Boolean {
    displayText.value = "Closing scanner..."

    return try {
      scanner.close()
      displayText.value = "Scanner closed."
      true
    } catch (ex: JposException) {
      displayText.value = "Failed to close scanner: ${ex.message}"
      false
    }
  }
}
