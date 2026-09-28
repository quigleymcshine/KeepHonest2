package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [DailyHabitLog::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitLogDao(): DailyHabitLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS daily_habit_logs_new (
                        date TEXT NOT NULL PRIMARY KEY,
                        epochDay INTEGER NOT NULL,
                        bikedToday INTEGER NOT NULL,
                        bikeMinutes REAL NOT NULL,
                        drinkCount INTEGER NOT NULL,
                        drinkNotes TEXT NOT NULL,
                        notes TEXT NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO daily_habit_logs_new (date, epochDay, bikedToday, bikeMinutes, drinkCount, drinkNotes, notes, updatedAt)
                    SELECT date, epochDay, bikedToday, CAST(bikeMinutes AS REAL), drinkCount, drinkNotes, notes, updatedAt FROM daily_habit_logs
                """.trimIndent())
                db.execSQL("DROP TABLE daily_habit_logs")
                db.execSQL("ALTER TABLE daily_habit_logs_new RENAME TO daily_habit_logs")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pedal_pour_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
