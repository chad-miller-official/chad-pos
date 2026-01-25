package com.chad.pos

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.chad.pos.device.ChadScale
import com.chad.pos.device.ChadScanner
import org.jetbrains.compose.resources.stringResource
import pos.composeapp.generated.resources.Res
import pos.composeapp.generated.resources.scale_name
import pos.composeapp.generated.resources.scanner_name

fun main() = application {
  val jposScannerName = stringResource(Res.string.scanner_name)
  val jposScaleName = stringResource(Res.string.scale_name)

  if (!(ChadScanner.connect(jposScannerName))) {
    exitApplication()
  }

  var count = 1
  var scaleConnected = false

  while (count <= 3) {
    scaleConnected = ChadScale.connect(jposScaleName, suppressErrors = count != 3)

    if (scaleConnected) {
      break
    } else {
      println("Failed to connect to scale - trying again... ($count / 3 attempts)")
    }

    count++
  }

  if (!scaleConnected) {
    exitApplication()
  }

  Window(
    onCloseRequest = {
      ChadScanner.disconnect()
      ChadScale.disconnect()
      exitApplication()
    },
    title = "ChadPOS",
  ) {
    App()
  }
}