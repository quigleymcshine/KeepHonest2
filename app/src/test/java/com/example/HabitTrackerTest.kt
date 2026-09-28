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
            bikeMinutes = 45.5f,
            drinkCount = 0,
            drinkNotes = "Sober day",
            notes = "Felt energized after cycling"
        )

        assertEquals("2026-09-25", log.date)
        assertTrue(log.bikedToday)
        assertEquals(45.5f, log.bikeMinutes, 0.001f)
        assertEquals(0, log.drinkCount)
    }

    @Test
    fun testDurationUtilsThirtySecondIntervals() {
        val halfMin = 0.5f // 30 seconds
        val (mins0, secs30) = com.example.util.DurationUtils.toMinutesAndSeconds(halfMin)
        assertEquals(0, mins0)
        assertEquals(30, secs30)
        assertEquals("30s", com.example.util.DurationUtils.formatCompact(halfMin))
        assertEquals("30 seconds", com.example.util.DurationUtils.formatDetailed(halfMin))

        val rideTime = 22.5f // 22 min 30 sec
        val (mins22, secsAfter) = com.example.util.DurationUtils.toMinutesAndSeconds(rideTime)
        assertEquals(22, mins22)
        assertEquals(30, secsAfter)
        assertEquals("22m 30s", com.example.util.DurationUtils.formatCompact(rideTime))
        assertEquals("22 min 30 sec", com.example.util.DurationUtils.formatDetailed(rideTime))

        val reconstructed = com.example.util.DurationUtils.fromMinutesAndSeconds(22, 30)
        assertEquals(22.5f, reconstructed, 0.001f)
    }

    @Test
    fun testBackupExportAndParse() {
        val sampleLogs = listOf(
            DailyHabitLog(
                date = "2026-09-24",
                epochDay = DateUtils.toEpochDay("2026-09-24"),
                bikedToday = true,
                bikeMinutes = 40.5f,
                drinkCount = 0,
                drinkNotes = "",
                notes = "Great session"
            ),
            DailyHabitLog(
                date = "2026-09-25",
                epochDay = DateUtils.toEpochDay("2026-09-25"),
                bikedToday = false,
                bikeMinutes = 0f,
                drinkCount = 2,
                drinkNotes = "2 beers",
                notes = "Rest day"
            )
        )

        // Test CSV export
        val csv = com.example.util.BackupUtils.exportToCsv(sampleLogs)
        assertTrue(csv.contains("date,epoch_day,biked_today,bike_minutes,drink_count,drink_notes,notes,updated_at"))
        assertTrue(csv.contains("2026-09-24"))
        assertTrue(csv.contains("40.5"))
        assertTrue(csv.contains("2026-09-25"))
        assertTrue(csv.contains("false,0,2,2 beers"))

        // Test JSON export & parse round-trip
        val json = com.example.util.BackupUtils.exportToJson(sampleLogs)
        assertTrue(json.contains("\"date\": \"2026-09-24\""))
        assertTrue(json.contains("\"bikeMinutes\": 40.5"))

        val restored = com.example.util.BackupUtils.parseJsonBackup(json)
        assertEquals(2, restored.size)
        assertEquals("2026-09-24", restored[0].date)
        assertEquals(40.5f, restored[0].bikeMinutes, 0.001f)
        assertEquals(2, restored[1].drinkCount)
        assertEquals("2 beers", restored[1].drinkNotes)
    }
}
