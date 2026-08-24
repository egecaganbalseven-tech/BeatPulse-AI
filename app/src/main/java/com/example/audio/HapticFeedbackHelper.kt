package com.example.audio

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticFeedbackHelper(context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    var isHapticEnabled: Boolean = true

    fun tick(isAccent: Boolean) {
        if (!isHapticEnabled || vibrator == null || !vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val effectId = if (isAccent) {
                    VibrationEffect.EFFECT_HEAVY_CLICK
                } else {
                    VibrationEffect.EFFECT_TICK
                }
                vibrator.vibrate(VibrationEffect.createPredefined(effectId))
            } else {
                @Suppress("DEPRECATION")
                val duration = if (isAccent) 30L else 15L
                @Suppress("DEPRECATION")
                vibrator.vibrate(duration)
            }
        } catch (_: Exception) {
            // Ignored if permissions not granted or device limitation
        }
    }
}
