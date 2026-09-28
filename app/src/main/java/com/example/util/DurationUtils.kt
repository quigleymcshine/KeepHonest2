package com.example.util

import java.util.Locale
import kotlin.math.roundToInt

object DurationUtils {

    /**
     * Formats minutes (e.g. 15.5f, 0.5f, 30f) into a compact readable string:
     * - 0.5f -> "30s"
     * - 15.0f -> "15 min"
     * - 15.5f -> "15m 30s"
     */
    fun formatCompact(minutes: Float): String {
        val totalSeconds = (minutes * 60f).roundToInt()
        val mins = totalSeconds / 60
        val secs = totalSeconds % 60
        return when {
            mins > 0 && secs > 0 -> "${mins}m ${secs}s"
            mins > 0 -> "$mins min"
            secs > 0 -> "${secs}s"
            else -> "0 min"
        }
    }

    /**
     * Formats minutes into detailed text for summaries:
     * - 0.5f -> "30 seconds"
     * - 1.0f -> "1 minute"
     * - 15.0f -> "15 minutes"
     * - 15.5f -> "15 minutes 30 seconds"
     */
    fun formatDetailed(minutes: Float): String {
        val totalSeconds = (minutes * 60f).roundToInt()
        val mins = totalSeconds / 60
        val secs = totalSeconds % 60
        return when {
            mins > 0 && secs > 0 -> "$mins min $secs sec"
            mins == 1 -> "1 minute"
            mins > 1 -> "$mins minutes"
            secs > 0 -> "$secs seconds"
            else -> "0 minutes"
        }
    }

    /**
     * Extracts minutes and seconds pair (e.g. 15.5f -> (15, 30))
     */
    fun toMinutesAndSeconds(minutes: Float): Pair<Int, Int> {
        val totalSeconds = (minutes * 60f).roundToInt().coerceAtLeast(0)
        val mins = totalSeconds / 60
        val secs = totalSeconds % 60
        return Pair(mins, secs)
    }

    /**
     * Combines minutes and seconds into Float minutes (e.g. 15m 30s -> 15.5f)
     */
    fun fromMinutesAndSeconds(minutes: Int, seconds: Int): Float {
        val validMins = minutes.coerceIn(0, 360)
        val validSecs = seconds.coerceIn(0, 59)
        val roundedSecs = if (validSecs >= 45) 60 else if (validSecs >= 15) 30 else 0
        return validMins.toFloat() + (roundedSecs / 60f)
    }

    /**
     * Formats as CSV value: e.g. "15" or "15.5"
     */
    fun formatCsv(minutes: Float): String {
        return if (minutes % 1f == 0f) {
            minutes.toInt().toString()
        } else {
            String.format(Locale.US, "%.1f", minutes)
        }
    }
}
