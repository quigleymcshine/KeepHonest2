package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {
    private val isoFormat: SimpleDateFormat
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getDefault()
        }

    private val displayFormat: SimpleDateFormat
        get() = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }

    private val chartLabelFormat: SimpleDateFormat
        get() = SimpleDateFormat("MMM d", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }

    private val shortDayFormat: SimpleDateFormat
        get() = SimpleDateFormat("EEE", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }

    fun getTodayDate(): String {
        return isoFormat.format(Date())
    }

    fun getYesterdayDate(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return isoFormat.format(cal.time)
    }

    fun formatDisplayDate(dateStr: String): String {
        return try {
            val date = isoFormat.parse(dateStr) ?: return dateStr
            val today = getTodayDate()
            val yesterday = getYesterdayDate()
            when (dateStr) {
                today -> "Today (${displayFormat.format(date)})"
                yesterday -> "Yesterday (${displayFormat.format(date)})"
                else -> displayFormat.format(date)
            }
        } catch (_: Exception) {
            dateStr
        }
    }

    fun formatChartDate(dateStr: String): String {
        return try {
            val date = isoFormat.parse(dateStr) ?: return dateStr
            chartLabelFormat.format(date)
        } catch (_: Exception) {
            dateStr
        }
    }

    fun formatShortDay(dateStr: String): String {
        return try {
            val date = isoFormat.parse(dateStr) ?: return dateStr
            shortDayFormat.format(date)
        } catch (_: Exception) {
            dateStr
        }
    }

    fun toEpochDay(dateStr: String): Long {
        return try {
            val date = isoFormat.parse(dateStr) ?: return 0L
            val cal = Calendar.getInstance()
            cal.time = date
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis / (24L * 60L * 60L * 1000L)
        } catch (_: Exception) {
            0L
        }
    }

    fun epochDayToDateStr(epochDay: Long): String {
        val millis = epochDay * (24L * 60L * 60L * 1000L)
        return isoFormat.format(Date(millis))
    }

    fun getDateDaysAgo(daysAgo: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return isoFormat.format(cal.time)
    }

    fun getDaysInRange(startDateStr: String, endDateStr: String): List<String> {
        val dates = mutableListOf<String>()
        try {
            val startCal = Calendar.getInstance().apply {
                time = isoFormat.parse(startDateStr) ?: return emptyList()
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val endCal = Calendar.getInstance().apply {
                time = isoFormat.parse(endDateStr) ?: return emptyList()
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            while (!startCal.after(endCal)) {
                dates.add(isoFormat.format(startCal.time))
                startCal.add(Calendar.DAY_OF_YEAR, 1)
            }
        } catch (_: Exception) {
            return emptyList()
        }
        return dates
    }
}
