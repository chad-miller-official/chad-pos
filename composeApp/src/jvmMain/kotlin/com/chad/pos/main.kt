package com.chad.pos

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.chad.pos.device.ChadScale
import com.chad.pos.device.ChadScanner

fun main() {
  Runtime.getRuntime().addShutdownHook(Thread {
    println("Disconnecting...")
    ChadScanner.disconnect()
    ChadScale.disconnect()
  })

  application {
    Window(
      onCloseRequest = {
        exitApplication()
      },
      state = rememberWindowState(placement = WindowPlacement.Fullscreen),
      title = "ChadPOS",
    ) {
      App(onExitApp = ::exitApplication)
    }
  }
}