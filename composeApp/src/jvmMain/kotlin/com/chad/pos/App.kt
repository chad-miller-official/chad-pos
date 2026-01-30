package com.chad.pos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chad.pos.device.ChadScale
import com.chad.pos.device.ChadScanner
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import pos.composeapp.generated.resources.Res
import pos.composeapp.generated.resources.scale_name
import pos.composeapp.generated.resources.scanner_name

private const val MAX_SCALE_CONNECT_TRIES = 3

suspend fun initScanner(jposScannerName: String) {
  ChadScanner.connect(jposScannerName)
}

suspend fun initScale(jposScaleName: String) {
  var scaleConnectCount = 1

  while (scaleConnectCount <= MAX_SCALE_CONNECT_TRIES) {
    if (ChadScale.connect(
        jposScaleName, suppressErrors = scaleConnectCount != MAX_SCALE_CONNECT_TRIES
      )
    ) {
      break
    } else {
      println("Failed to connect to scale - trying again... ($scaleConnectCount / $MAX_SCALE_CONNECT_TRIES attempts)")
    }

    scaleConnectCount++
  }
}

@Composable
@Preview
fun App(onExitApp: () -> Unit = {}) {
  val jposScannerName = stringResource(Res.string.scanner_name)
  val jposScaleName = stringResource(Res.string.scale_name)
  val coroutineScope = rememberCoroutineScope()

  coroutineScope.launch {
    initScanner(jposScannerName)
  }

  coroutineScope.launch {
    initScale(jposScaleName)
  }

  MaterialTheme {
    Row(modifier = Modifier.padding(4.dp)) {
      Button(onClick = onExitApp) {
        Text("Exit")
      }
    }
    Row(
      horizontalArrangement = Arrangement.Center,
      modifier = Modifier.fillMaxSize(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(
        modifier = Modifier.padding(16.dp).weight(1f),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        val scannerDisplay by ChadScanner.getDisplayText()

        Text(
          text = "Scanner",
          fontWeight = FontWeight.Bold,
        )

        Text(scannerDisplay)
      }
      Column(
        modifier = Modifier.padding(16.dp).weight(1f),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        val scaleDisplay by ChadScale.getDisplayText()

        Text(
          text = "Scale",
          fontWeight = FontWeight.Bold,
        )

        Text(scaleDisplay)
      }
    }
  }
}