package com.chad.pos

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.chad.pos.device.ChadScale
import com.chad.pos.device.ChadScanner

fun main() {
  application {
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
}