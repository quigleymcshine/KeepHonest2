package com.example.reminder

import android.content.Context
import android.content.SharedPreferences

class ReminderPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("pedal_pour_reminder_prefs", Context.MODE_PRIVATE)

    var isEnabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_ENABLED, value).apply()

    var hour: Int
        get() = prefs.getInt(KEY_HOUR, 20) // Default 8:00 PM (20:00)
        set(value) = prefs.edit().putInt(KEY_HOUR, value).apply()

    var minute: Int
        get() = prefs.getInt(KEY_MINUTE, 0)
        set(value) = prefs.edit().putInt(KEY_MINUTE, value).apply()

    fun getFormattedTime(): String {
        val h = hour
        val m = minute
        val isPm = h >= 12
        val displayHour = when {
            h == 0 -> 12
            h > 12 -> h - 12
            else -> h
        }
        val displayMinute = String.format("%02d", m)
        val amPm = if (isPm) "PM" else "AM"
        return "$displayHour:$displayMinute $amPm"
    }

    companion object {
        private const val KEY_ENABLED = "key_reminder_enabled"
        private const val KEY_HOUR = "key_reminder_hour"
        private const val KEY_MINUTE = "key_reminder_minute"
    }
}
