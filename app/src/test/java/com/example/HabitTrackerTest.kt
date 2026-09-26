package com.example

import com.example.data.DailyHabitLog
import com.example.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HabitTrackerTest {

    @Test
    fun testDateUtilsTodayAndEpoch() {
        val today = DateUtils.getTodayDate()
        assertTrue(today.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))

        val epochDay = DateUtils.toEpochDay(today)
        assertTrue(epochDay > 19000L) // Valid contemporary epoch day

        val dateBack = DateUtils.epochDayToDateStr(epochDay)
        assertEquals(today, dateBack)
    }

    @Test
    fun testDaysInRange() {
        val start = "2026-09-20"
        val end = "2026-09-25"
        val days = DateUtils.getDaysInRange(start, end)
        assertEquals(6, days.size)
        assertEquals("2026-09-20", days.first())
        assertEquals("2026-09-25", days.last())
    }

    @Test
    fun testDailyHabitLogModel() {
        val log = DailyHabitLog(
            date = "2026-09-25",
            epochDay = DateUtils.toEpochDay("2026-09-25"),
            bikedToday = true,
            bikeMinutes = 45,
            drinkCount = 0,
            drinkNotes = "Sober day",
            notes = "Felt energized after cycling"
        )

        assertEquals("2026-09-25", log.date)
        assertTrue(log.bikedToday)
        assertEquals(45, log.bikeMinutes)
        assertEquals(0, log.drinkCount)
    }

    @Test
    fun testBackupExportAndParse() {
        val sampleLogs = listOf(
            DailyHabitLog(
                date = "2026-09-24",
                epochDay = DateUtils.toEpochDay("2026-09-24"),
                bikedToday = true,
                bikeMinutes = 40,
                drinkCount = 0,
                drinkNotes = "",
                notes = "Great session"
            ),
            DailyHabitLog(
                date = "2026-09-25",
                epochDay = DateUtils.toEpochDay("2026-09-25"),
                bikedToday = false,
                bikeMinutes = 0,
                drinkCount = 2,
                drinkNotes = "2 beers",
                notes = "Rest day"
            )
        )

        // Test CSV export
        val csv = com.example.util.BackupUtils.exportToCsv(sampleLogs)
        assertTrue(csv.contains("date,epoch_day,biked_today,bike_minutes,drink_count,drink_notes,notes,updated_at"))
        assertTrue(csv.contains("2026-09-24"))
        assertTrue(csv.contains("true,40,0,"))
        assertTrue(csv.contains("2026-09-25"))
        assertTrue(csv.contains("false,0,2,2 beers"))

        // Test JSON export & parse round-trip
        val json = com.example.util.BackupUtils.exportToJson(sampleLogs)
        assertTrue(json.contains("\"date\": \"2026-09-24\""))
        assertTrue(json.contains("\"bikeMinutes\": 40"))

        val restored = com.example.util.BackupUtils.parseJsonBackup(json)
        assertEquals(2, restored.size)
        assertEquals("2026-09-24", restored[0].date)
        assertEquals(40, restored[0].bikeMinutes)
        assertEquals(2, restored[1].drinkCount)
        assertEquals("2 beers", restored[1].drinkNotes)
    }
}
