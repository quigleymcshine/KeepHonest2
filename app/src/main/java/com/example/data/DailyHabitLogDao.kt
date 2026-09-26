package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyHabitLogDao {
    @Query("SELECT * FROM daily_habit_logs ORDER BY epochDay DESC")
    fun getAllLogs(): Flow<List<DailyHabitLog>>

    @Query("SELECT * FROM daily_habit_logs WHERE epochDay BETWEEN :startEpochDay AND :endEpochDay ORDER BY epochDay ASC")
    fun getLogsBetween(startEpochDay: Long, endEpochDay: Long): Flow<List<DailyHabitLog>>

    @Query("SELECT * FROM daily_habit_logs WHERE date = :date LIMIT 1")
    suspend fun getLogByDate(date: String): DailyHabitLog?

    @Query("SELECT * FROM daily_habit_logs WHERE date = :date LIMIT 1")
    fun getLogByDateFlow(date: String): Flow<DailyHabitLog?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(log: DailyHabitLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<DailyHabitLog>)

    @Delete
    suspend fun delete(log: DailyHabitLog)

    @Query("DELETE FROM daily_habit_logs WHERE date = :date")
    suspend fun deleteByDate(date: String)

    @Query("DELETE FROM daily_habit_logs")
    suspend fun clearAll()
}
