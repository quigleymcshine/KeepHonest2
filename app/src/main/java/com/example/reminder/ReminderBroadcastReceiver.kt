package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = ReminderPreferences(context)
        val action = intent.action

        when (action) {
            ACTION_DAILY_REMINDER -> {
                if (prefs.isEnabled) {
                    ReminderScheduler.showNotification(context)
                    // Automatically reschedule for the next day at configured time
                    ReminderScheduler.scheduleDailyReminder(context, prefs.hour, prefs.minute)
                }
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                if (prefs.isEnabled) {
                    ReminderScheduler.scheduleDailyReminder(context, prefs.hour, prefs.minute)
                }
            }
        }
    }

    companion object {
        const val ACTION_DAILY_REMINDER = "com.example.ACTION_DAILY_REMINDER"
    }
}
