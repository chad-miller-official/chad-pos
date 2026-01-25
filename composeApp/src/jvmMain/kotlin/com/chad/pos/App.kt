package com.chad.pos

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chad.pos.device.ChadScale
import com.chad.pos.device.ChadScanner

@Composable
fun ScannerText() {
  val scannerDisplay by ChadScanner.getDisplayText()
  Text(scannerDisplay)
}

@Composable
fun ScaleText() {
  val scaleDisplay by ChadScale.getDisplayText()
  Text(scaleDisplay)
}

@Composable
@Preview
fun App() {
  MaterialTheme {
    Row(modifier = Modifier.fillMaxWidth()) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp),
      ) {
        Text("Scanner")
        ScannerText()
      }
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp),
      ) {
        Text("Scale")
        ScaleText()
      }
    }
  }
}