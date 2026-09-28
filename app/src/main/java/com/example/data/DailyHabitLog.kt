package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_habit_logs")
data class DailyHabitLog(
    @PrimaryKey
    val date: String,            // ISO date string: YYYY-MM-DD
    val epochDay: Long,          // Days since Unix epoch for quick chronological sorting & queries
    val bikedToday: Boolean,     // Whether user rode the exercise bike
    val bikeMinutes: Float,      // Duration on bike in minutes (supports 0.5 increments for 30s chunks)
    val drinkCount: Int,         // Number of alcoholic drinks had that day (0 for sober/dry day)
    val drinkNotes: String = "", // Optional drink types/notes (e.g. "2 IPAs", "Glass of wine")
    val notes: String = "",      // General personal notes for the day
    val updatedAt: Long = System.currentTimeMillis()
)
