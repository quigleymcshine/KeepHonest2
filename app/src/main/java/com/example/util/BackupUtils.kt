package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.DailyHabitLog
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object BackupUtils {

    fun exportToCsv(logs: List<DailyHabitLog>): String {
        val sb = StringBuilder()
        sb.append("date,epoch_day,biked_today,bike_minutes,drink_count,drink_notes,notes,updated_at\n")
        val sorted = logs.sortedBy { it.epochDay }
        for (log in sorted) {
            sb.append(escapeCsv(log.date)).append(",")
            sb.append(log.epochDay).append(",")
            sb.append(log.bikedToday).append(",")
            sb.append(log.bikeMinutes).append(",")
            sb.append(log.drinkCount).append(",")
            sb.append(escapeCsv(log.drinkNotes)).append(",")
            sb.append(escapeCsv(log.notes)).append(",")
            sb.append(log.updatedAt).append("\n")
        }
        return sb.toString()
    }

    fun exportToJson(logs: List<DailyHabitLog>): String {
        val jsonArray = JSONArray()
        val sorted = logs.sortedBy { it.epochDay }
        for (log in sorted) {
            val obj = JSONObject().apply {
                put("date", log.date)
                put("epochDay", log.epochDay)
                put("bikedToday", log.bikedToday)
                put("bikeMinutes", log.bikeMinutes)
                put("drinkCount", log.drinkCount)
                put("drinkNotes", log.drinkNotes)
                put("notes", log.notes)
                put("updatedAt", log.updatedAt)
            }
            jsonArray.put(obj)
        }
        return jsonArray.toString(2)
    }

    fun parseJsonBackup(jsonStr: String): List<DailyHabitLog> {
        val list = mutableListOf<DailyHabitLog>()
        val trimmed = jsonStr.trim()
        val array = if (trimmed.startsWith("[")) {
            JSONArray(trimmed)
        } else if (trimmed.startsWith("{")) {
            val root = JSONObject(trimmed)
            root.optJSONArray("logs") ?: JSONArray()
        } else {
            return emptyList()
        }

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val date = obj.getString("date")
            val epochDay = if (obj.has("epochDay")) obj.getLong("epochDay") else DateUtils.toEpochDay(date)
            val bikedToday = obj.optBoolean("bikedToday", false)
            val bikeMinutes = obj.optInt("bikeMinutes", 0)
            val drinkCount = obj.optInt("drinkCount", 0)
            val drinkNotes = obj.optString("drinkNotes", "")
            val notes = obj.optString("notes", "")
            val updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())

            list.add(
                DailyHabitLog(
                    date = date,
                    epochDay = epochDay,
                    bikedToday = bikedToday,
                    bikeMinutes = bikeMinutes,
                    drinkCount = drinkCount,
                    drinkNotes = drinkNotes,
                    notes = notes,
                    updatedAt = updatedAt
                )
            )
        }
        return list
    }

    fun shareBackup(
        context: Context,
        content: String,
        mimeType: String,
        filename: String,
        title: String
    ) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, content)
            putExtra(Intent.EXTRA_TITLE, filename)
        }
        val chooser = Intent.createChooser(sendIntent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    fun copyToClipboard(context: Context, label: String, content: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, content)
        clipboard.setPrimaryClip(clip)
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}
