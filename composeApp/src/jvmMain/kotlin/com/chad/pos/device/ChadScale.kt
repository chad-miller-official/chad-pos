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
  override fun statusUpdateOccurred(event: StatusUpdateEvent) {
    displayText.value = "--"

    var status = ""
    var weight: Double? = null

    when (event.status) {
      ScaleConst.SCAL_SUE_STABLE_WEIGHT -> {
        val liveWeight = try {
          scale.scaleLiveWeight
        } catch (ex: JposException) {
          System.err.println("ERROR: Could not get weight data: ${ex.message}")
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
      "$weight ${getWeightUnits()}"
    }

    displayText.value = "$status: $weightWithUnits"
  }

  fun getWeightUnits(): String {
    return when (scale.weightUnit) {
      ScaleConst.SCAL_WU_GRAM -> "grams"
      ScaleConst.SCAL_WU_KILOGRAM -> "kilograms"
      ScaleConst.SCAL_WU_OUNCE -> "ounces"
      ScaleConst.SCAL_WU_POUND -> "pounds"
      else -> "pounds"
    }
  }
}

object ChadScale {
  private val scale = Scale()
  private val statusUpdateListener = LiveWeightStatusUpdateListener(scale)

  fun getDisplayText(): MutableState<String> {
    return displayText
  }

  fun connect(profile: String, suppressErrors: Boolean = true): Boolean {
    println("INFO: Connecting to scale...")

    try {
      scale.open(profile)
    } catch (ex: JposException) {
      if (!suppressErrors) {
        System.err.println("ERROR: Failed to open scale: ${ex.message}")
      }

      return false
    }

    try {
      scale.claim(1000)
      scale.statusNotify = ScaleConst.SCAL_SN_ENABLED
      scale.deviceEnabled = true
    } catch (ex: JposException) {
      close()

      if (!suppressErrors) {
        System.err.println("ERROR: Failed to connect to scale: ${ex.message}")
      }

      return false
    }

    scale.addStatusUpdateListener(statusUpdateListener)
    println("INFO: Scale connected.")

    return true
  }

  fun disconnect(): Boolean {
    println("INFO: Disconnecting scale...")
    scale.removeStatusUpdateListener(statusUpdateListener)

    try {
      scale.deviceEnabled = false
      scale.statusNotify = ScaleConst.SCAL_SN_DISABLED
      scale.release()
    } catch (ex: JposException) {
      System.err.println("ERROR: Failed to disconnect from scale: ${ex.message}")
      return false
    }

    close()
    println("INFO: Scale disconnected.")

    return true
  }

  private fun close(): Boolean {
    return try {
      scale.close()
      true
    } catch (ex: JposException) {
      System.err.println("ERROR: Failed to close scale: ${ex.message}")
      false
    }
  }
}