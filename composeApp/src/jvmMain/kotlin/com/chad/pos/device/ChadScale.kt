package com.chad.pos.device

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import jpos.JposException
import jpos.Scale
import jpos.ScaleConst
import jpos.events.StatusUpdateEvent
import jpos.events.StatusUpdateListener

private val displayText = mutableStateOf("--")

class LiveWeightStatusUpdateListener(val scale: Scale) : StatusUpdateListener {
  companion object {
    val weightUnits = mapOf(
      ScaleConst.SCAL_WU_GRAM to "grams",
      ScaleConst.SCAL_WU_KILOGRAM to "kilograms",
      ScaleConst.SCAL_WU_OUNCE to "ounces",
      ScaleConst.SCAL_WU_POUND to "pounds",
    )
  }

  override fun statusUpdateOccurred(event: StatusUpdateEvent) {
    displayText.value = "--"

    var status = ""
    var weight: Double? = null

    when (event.status) {
      ScaleConst.SCAL_SUE_STABLE_WEIGHT -> {
        val liveWeight = try {
          scale.scaleLiveWeight
        } catch (ex: JposException) {
          displayText.value = "Could not get weight data: ${ex.message}"
          return
        }

        if (!scale.asyncMode) {
          status = "Stable Weight"
          weight = liveWeight.toDouble() / 1000
        }
      }

      ScaleConst.SCAL_SUE_WEIGHT_OVERWEIGHT -> status = "Over Weight"
      ScaleConst.SCAL_SUE_WEIGHT_UNDER_ZERO -> status = "Under Zero"
      ScaleConst.SCAL_SUE_WEIGHT_UNSTABLE -> status = "Unstable Weight"

      ScaleConst.SCAL_SUE_WEIGHT_ZERO -> {
        status = "Zero Weight"
        weight = 0.0
      }

      ScaleConst.SCAL_SUE_NOT_READY -> status = "Scale Not Ready"
      else -> return
    }

    val weightWithUnits = if (weight == null) {
      "--.--"
    } else {
      "$weight ${weightUnits.getOrDefault(scale.weightUnit, "(unknown)")}"
    }

    displayText.value = "$weightWithUnits [$status]"
  }
}

object ChadScale {
  private val scale = Scale()
  private val statusUpdateListener = LiveWeightStatusUpdateListener(scale)

  fun getDisplayText(): MutableState<String> {
    return displayText
  }

  suspend fun connect(profile: String, suppressErrors: Boolean = true): Boolean {
    displayText.value = "Connecting to scale..."

    try {
      scale.open(profile)
    } catch (ex: JposException) {
      if (!suppressErrors) {
        displayText.value = "Failed to open scale: ${ex.message}"
      }

      return false
    }

    displayText.value = "Claiming scale..."

    try {
      scale.claim(1000)
    } catch (ex: JposException) {
      if (!suppressErrors) {
        displayText.value = "Failed to claim scale: ${ex.message}"
      }

      close()
      return false
    }

    displayText.value = "Enabling scale..."

    try {
      scale.statusNotify = ScaleConst.SCAL_SN_ENABLED
      scale.deviceEnabled = true
    } catch (ex: JposException) {
      if (!suppressErrors) {
        displayText.value = "Failed to enable scale: ${ex.message}"
      }

      close()
      return false
    }

    scale.addStatusUpdateListener(statusUpdateListener)
    displayText.value = "Scale connected."

    return true
  }

  fun disconnect(): Boolean {
    displayText.value = "Disconnecting scale..."
    scale.removeStatusUpdateListener(statusUpdateListener)

    try {
      scale.deviceEnabled = false
      scale.statusNotify = ScaleConst.SCAL_SN_DISABLED
    } catch (ex: JposException) {
      System.err.println("Failed to disconnect from scale: ${ex.message}")
      return false
    }

    println("Releasing scale...")

    try {
      scale.release()
    } catch (ex: JposException) {
      System.err.println("Failed to release scale: ${ex.message}")
      return false
    }

    close()

    println("Scale disconnected.")
    return true
  }

  private fun close(): Boolean {
    println("Closing scale...")

    return try {
      scale.close()
      println("Scale closed.")
      true
    } catch (ex: JposException) {
      println("Failed to close scale: ${ex.message}")
      false
    }
  }
}