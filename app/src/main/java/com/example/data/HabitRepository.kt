package com.example.data

import com.example.util.DateUtils
import kotlinx.coroutines.flow.Flow

class HabitRepository(private val dao: DailyHabitLogDao) {
    val allLogs: Flow<List<DailyHabitLog>> = dao.getAllLogs()

    fun getLogsBetween(startEpochDay: Long, endEpochDay: Long): Flow<List<DailyHabitLog>> {
        return dao.getLogsBetween(startEpochDay, endEpochDay)
    }

    suspend fun getLogByDate(date: String): DailyHabitLog? {
        return dao.getLogByDate(date)
    }

    fun getLogByDateFlow(date: String): Flow<DailyHabitLog?> {
        return dao.getLogByDateFlow(date)
    }

    suspend fun saveLog(log: DailyHabitLog) {
        dao.insertOrUpdate(log)
    }

    suspend fun insertAll(logs: List<DailyHabitLog>) {
        dao.insertAll(logs)
    }

    suspend fun deleteLog(log: DailyHabitLog) {
        dao.delete(log)
    }

    suspend fun deleteByDate(date: String) {
        dao.deleteByDate(date)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }

    suspend fun populateSampleData() {
        val sampleList = mutableListOf<DailyHabitLog>()
        // Generate past 14 days of realistic workout & social balance data (with some 30s interval sessions)
        val samples = listOf(
            Triple(13, 0f, 0),     // 13 days ago: rest day, 0 drinks
            Triple(12, 30.5f, 1),  // 12 days ago: 30m 30s bike, 1 beer
            Triple(11, 45f, 0),    // 11 days ago: 45m bike, dry day
            Triple(10, 0f, 2),     // 10 days ago: rest day, 2 glasses wine
            Triple(9, 22.5f, 0),   // 9 days ago: 22m 30s HIIT, dry day
            Triple(8, 35f, 0),     // 8 days ago: 35m bike, dry day
            Triple(7, 40f, 3),     // 7 days ago: 40m bike, 3 drinks (weekend)
            Triple(6, 0f, 1),      // 6 days ago: rest day, 1 cider
            Triple(5, 50.5f, 0),   // 5 days ago: 50m 30s endurance ride, 0 drinks
            Triple(4, 30f, 0),     // 4 days ago: 30m ride, 0 drinks
            Triple(3, 20f, 2),     // 3 days ago: 20m light ride, 2 beers
            Triple(2, 45.5f, 0),   // 2 days ago: 45m 30s workout, 0 drinks
            Triple(1, 0f, 0),      // Yesterday: rest day, dry day
            Triple(0, 35.5f, 1)    // Today: 35m 30s bike, 1 drink
        )

        for ((daysAgo, minutes, drinks) in samples) {
            val date = DateUtils.getDateDaysAgo(daysAgo)
            val epochDay = DateUtils.toEpochDay(date)
            sampleList.add(
                DailyHabitLog(
                    date = date,
                    epochDay = epochDay,
                    bikedToday = minutes > 0f,
                    bikeMinutes = minutes,
                    drinkCount = drinks,
                    drinkNotes = if (drinks > 0) "$drinks standard drink(s)" else "Alcohol-free day",
                    notes = if (minutes > 0f) "Exercise bike session" else "Rest day"
                )
            )
        }
        dao.insertAll(sampleList)
    }
}
