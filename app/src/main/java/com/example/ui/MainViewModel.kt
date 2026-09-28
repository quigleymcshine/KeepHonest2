package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DailyHabitLog
import com.example.data.HabitRepository
import com.example.reminder.ReminderPreferences
import com.example.reminder.ReminderScheduler
import com.example.ui.components.DailyGraphItem
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

enum class AppScreen {
    LOG,
    CHARTS,
    HISTORY,
    SETTINGS
}

enum class ChartTimeRange(val days: Int, val label: String) {
    DAYS_7(7, "7 Days"),
    DAYS_14(14, "14 Days"),
    DAYS_30(30, "30 Days"),
    DAYS_90(90, "90 Days"),
    ALL(365, "All Time")
}

enum class ChartFilter(val label: String) {
    BOTH("Both Habits"),
    BIKE_ONLY("Bike Minutes"),
    DRINKS_ONLY("Drinks")
}

data class HabitStatistics(
    val totalBikeMinutes: Float = 0f,
    val totalRides: Int = 0,
    val avgBikeMinutesPerRide: Float = 0f,
    val avgBikeMinutesOverall: Float = 0f,
    val currentRideStreak: Int = 0,
    val bestRideMinutes: Float = 0f,
    val totalDrinks: Int = 0,
    val avgDrinksPerDay: Float = 0f,
    val avgDrinksPerWeek: Float = 0f,
    val alcoholFreeDays: Int = 0,
    val alcoholFreePercentage: Int = 0,
    val currentDryStreak: Int = 0,
    val bestDryStreak: Int = 0
)

data class LogFormState(
    val date: String = DateUtils.getTodayDate(),
    val bikedToday: Boolean = false,
    val bikeMinutes: Float = 30f,
    val drinkCount: Int = 0,
    val drinkNotes: String = "",
    val notes: String = "",
    val isExistingLog: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HabitRepository
    private val reminderPrefs: ReminderPreferences = ReminderPreferences(application)

    init {
        val database = AppDatabase.getDatabase(application)
        repository = HabitRepository(database.habitLogDao())
    }

    // Navigation state
    private val _currentScreen = MutableStateFlow(AppScreen.LOG)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Form state for logging
    private val _logFormState = MutableStateFlow(LogFormState())
    val logFormState: StateFlow<LogFormState> = _logFormState.asStateFlow()

    // Status message for save feedback
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Chart filter states
    private val _selectedTimeRange = MutableStateFlow(ChartTimeRange.DAYS_14)
    val selectedTimeRange: StateFlow<ChartTimeRange> = _selectedTimeRange.asStateFlow()

    private val _selectedChartFilter = MutableStateFlow(ChartFilter.BOTH)
    val selectedChartFilter: StateFlow<ChartFilter> = _selectedChartFilter.asStateFlow()

    // All logs from Room
    val allLogs: StateFlow<List<DailyHabitLog>> = repository.allLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Chart data items (continuous timeline without gaps)
    val chartData: StateFlow<Pair<List<DailyGraphItem>, List<DailyGraphItem>>> = combine(
        allLogs,
        _selectedTimeRange
    ) { logs, range ->
        generateTimelineData(logs, range)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Pair(emptyList(), emptyList())
    )

    // Aggregate statistics
    val statistics: StateFlow<HabitStatistics> = combine(
        allLogs,
        _selectedTimeRange
    ) { logs, range ->
        computeStatistics(logs, range)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HabitStatistics()
    )

    // Reminder state
    private val _reminderEnabled = MutableStateFlow(reminderPrefs.isEnabled)
    val reminderEnabled: StateFlow<Boolean> = _reminderEnabled.asStateFlow()

    private val _reminderHour = MutableStateFlow(reminderPrefs.hour)
    val reminderHour: StateFlow<Int> = _reminderHour.asStateFlow()

    private val _reminderMinute = MutableStateFlow(reminderPrefs.minute)
    val reminderMinute: StateFlow<Int> = _reminderMinute.asStateFlow()

    init {
        // Load initial state for today
        loadLogForDate(DateUtils.getTodayDate())
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setTimeRange(range: ChartTimeRange) {
        _selectedTimeRange.value = range
    }

    fun setChartFilter(filter: ChartFilter) {
        _selectedChartFilter.value = filter
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun loadLogForDate(dateStr: String) {
        viewModelScope.launch {
            val existing = repository.getLogByDate(dateStr)
            if (existing != null) {
                _logFormState.value = LogFormState(
                    date = existing.date,
                    bikedToday = existing.bikedToday,
                    bikeMinutes = existing.bikeMinutes,
                    drinkCount = existing.drinkCount,
                    drinkNotes = existing.drinkNotes,
                    notes = existing.notes,
                    isExistingLog = true
                )
            } else {
                _logFormState.value = LogFormState(
                    date = dateStr,
                    bikedToday = false,
                    bikeMinutes = 30f, // Default preset when user enables bike
                    drinkCount = 0,   // Default sober / 0 drinks
                    drinkNotes = "",
                    notes = "",
                    isExistingLog = false
                )
            }
        }
    }

    fun setBikedToday(biked: Boolean) {
        _logFormState.update { current ->
            current.copy(
                bikedToday = biked,
                bikeMinutes = if (biked && current.bikeMinutes <= 0f) 30f else current.bikeMinutes
            )
        }
    }

    fun setBikeMinutes(minutes: Float) {
        // Snap to nearest 30-second (0.5 minute) chunk
        val snapped = (minutes * 2f).roundToInt() / 2f
        val clamped = snapped.coerceIn(0f, 360f)
        _logFormState.update { current ->
            current.copy(
                bikeMinutes = clamped,
                bikedToday = clamped > 0f
            )
        }
    }

    fun setBikeMinutes(minutes: Int) {
        setBikeMinutes(minutes.toFloat())
    }

    fun adjustBikeMinutes(deltaMinutes: Float) {
        val current = _logFormState.value.bikeMinutes
        setBikeMinutes(current + deltaMinutes)
    }

    fun adjustBikeMinutes(deltaMinutes: Int) {
        adjustBikeMinutes(deltaMinutes.toFloat())
    }

    fun setDrinkCount(count: Int) {
        val coerced = count.coerceIn(0, 50)
        _logFormState.update { current ->
            current.copy(drinkCount = coerced)
        }
    }

    fun adjustDrinkCount(delta: Int) {
        val newCount = (_logFormState.value.drinkCount + delta).coerceIn(0, 50)
        setDrinkCount(newCount)
    }

    fun setDrinkNotes(notes: String) {
        _logFormState.update { it.copy(drinkNotes = notes) }
    }

    fun setNotes(notes: String) {
        _logFormState.update { it.copy(notes = notes) }
    }

    fun saveCurrentLog() {
        val state = _logFormState.value
        val epochDay = DateUtils.toEpochDay(state.date)
        val minutes = if (state.bikedToday) state.bikeMinutes else 0f

        val log = DailyHabitLog(
            date = state.date,
            epochDay = epochDay,
            bikedToday = state.bikedToday && minutes > 0f,
            bikeMinutes = minutes,
            drinkCount = state.drinkCount,
            drinkNotes = state.drinkNotes.trim(),
            notes = state.notes.trim()
        )

        viewModelScope.launch {
            repository.saveLog(log)
            _logFormState.update { it.copy(isExistingLog = true) }
            _snackbarMessage.value = "Log saved for ${DateUtils.formatDisplayDate(state.date)}! \uD83C\uDF89"
        }
    }

    fun deleteLog(log: DailyHabitLog) {
        viewModelScope.launch {
            repository.deleteLog(log)
            if (_logFormState.value.date == log.date) {
                loadLogForDate(log.date)
            }
            _snackbarMessage.value = "Deleted log for ${log.date}"
        }
    }

    fun deleteCurrentLog() {
        val date = _logFormState.value.date
        viewModelScope.launch {
            repository.deleteByDate(date)
            loadLogForDate(date)
            _snackbarMessage.value = "Deleted entry for $date"
        }
    }

    fun setReminderEnabled(enabled: Boolean) {
        reminderPrefs.isEnabled = enabled
        _reminderEnabled.value = enabled
        val context = getApplication<Application>()
        if (enabled) {
            ReminderScheduler.scheduleDailyReminder(context, reminderPrefs.hour, reminderPrefs.minute)
            _snackbarMessage.value = "Daily reminder set for ${reminderPrefs.getFormattedTime()}"
        } else {
            ReminderScheduler.cancelReminder(context)
            _snackbarMessage.value = "Daily reminder turned off"
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        reminderPrefs.hour = hour
        reminderPrefs.minute = minute
        _reminderHour.value = hour
        _reminderMinute.value = minute

        val context = getApplication<Application>()
        if (reminderPrefs.isEnabled) {
            ReminderScheduler.scheduleDailyReminder(context, hour, minute)
        }
        _snackbarMessage.value = "Reminder scheduled for ${reminderPrefs.getFormattedTime()}"
    }

    fun triggerTestNotification() {
        val context = getApplication<Application>()
        ReminderScheduler.showNotification(context)
        _snackbarMessage.value = "Test notification sent! Check your notification bar."
    }

    fun populateSampleData() {
        viewModelScope.launch {
            repository.populateSampleData()
            _snackbarMessage.value = "Sample habit data loaded for the past 14 days!"
            loadLogForDate(_logFormState.value.date)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAll()
            _snackbarMessage.value = "All logs cleared."
            loadLogForDate(_logFormState.value.date)
        }
    }

    fun restoreFromJson(jsonStr: String): Boolean {
        return try {
            val logs = com.example.util.BackupUtils.parseJsonBackup(jsonStr)
            if (logs.isNotEmpty()) {
                viewModelScope.launch {
                    repository.insertAll(logs)
                    _snackbarMessage.value = "Successfully restored ${logs.size} log entries! \uD83C\uDF89"
                    loadLogForDate(_logFormState.value.date)
                }
                true
            } else {
                _snackbarMessage.value = "No valid logs found in backup data."
                false
            }
        } catch (e: Exception) {
            _snackbarMessage.value = "Error reading backup: ${e.localizedMessage}"
            false
        }
    }

    private fun generateTimelineData(
        logs: List<DailyHabitLog>,
        range: ChartTimeRange
    ): Pair<List<DailyGraphItem>, List<DailyGraphItem>> {
        val todayStr = DateUtils.getTodayDate()
        val daysCount = range.days
        val startDateStr = DateUtils.getDateDaysAgo(daysCount - 1)
        val allDates = DateUtils.getDaysInRange(startDateStr, todayStr)

        val logMap = logs.associateBy { it.date }

        val bikePoints = mutableListOf<DailyGraphItem>()
        val drinkPoints = mutableListOf<DailyGraphItem>()

        for (dateStr in allDates) {
            val log = logMap[dateStr]
            val minutes = log?.bikeMinutes?.toFloat() ?: 0f
            val drinks = log?.drinkCount?.toFloat() ?: 0f

            bikePoints.add(
                DailyGraphItem(
                    dateStr = dateStr,
                    value = minutes,
                    isRiddenOrDry = minutes > 0
                )
            )

            drinkPoints.add(
                DailyGraphItem(
                    dateStr = dateStr,
                    value = drinks,
                    isRiddenOrDry = drinks == 0f
                )
            )
        }

        return Pair(bikePoints, drinkPoints)
    }

    private fun computeStatistics(
        logs: List<DailyHabitLog>,
        range: ChartTimeRange
    ): HabitStatistics {
        if (logs.isEmpty()) return HabitStatistics()

        val todayEpoch = DateUtils.toEpochDay(DateUtils.getTodayDate())
        val startEpoch = todayEpoch - (range.days - 1)

        val filteredLogs = logs.filter { it.epochDay in startEpoch..todayEpoch }

        val totalBikeMin = filteredLogs.sumOf { it.bikeMinutes.toDouble() }.toFloat()
        val rides = filteredLogs.filter { it.bikedToday && it.bikeMinutes > 0f }
        val totalRidesCount = rides.size
        val avgBikeMinPerRide = if (totalRidesCount > 0) totalBikeMin / totalRidesCount else 0f
        val avgBikeMinOverall = if (range.days > 0) totalBikeMin / range.days else 0f
        val bestRide = filteredLogs.maxOfOrNull { it.bikeMinutes } ?: 0f

        val totalDrinksCount = filteredLogs.sumOf { it.drinkCount }
        val avgDrinks = if (range.days > 0) totalDrinksCount.toFloat() / range.days else 0f
        val avgDrinksWeekly = avgDrinks * 7f
        val alcoholFree = filteredLogs.count { it.drinkCount == 0 }
        val alcoholFreePct = if (filteredLogs.isNotEmpty()) (alcoholFree * 100) / filteredLogs.size else 0

        // Calculate current streaks (consecutive days ending today/yesterday)
        val logMap = logs.associateBy { it.epochDay }
        var currentRideStreak = 0
        var checkEpoch = todayEpoch

        // If not biked today, check if streak was active yesterday
        if (logMap[checkEpoch]?.bikedToday != true) {
            checkEpoch--
        }
        while (logMap[checkEpoch]?.bikedToday == true && (logMap[checkEpoch]?.bikeMinutes ?: 0f) > 0f) {
            currentRideStreak++
            checkEpoch--
        }

        var currentDryStreak = 0
        var dryCheckEpoch = todayEpoch
        if (logMap[dryCheckEpoch]?.let { it.drinkCount == 0 } != true) {
            dryCheckEpoch--
        }
        while (logMap[dryCheckEpoch]?.let { it.drinkCount == 0 } == true) {
            currentDryStreak++
            dryCheckEpoch--
        }

        // Calculate best dry streak in filtered range
        var bestDry = 0
        var tempDry = 0
        val sortedAsc = filteredLogs.sortedBy { it.epochDay }
        for (log in sortedAsc) {
            if (log.drinkCount == 0) {
                tempDry++
                if (tempDry > bestDry) bestDry = tempDry
            } else {
                tempDry = 0
            }
        }

        return HabitStatistics(
            totalBikeMinutes = totalBikeMin,
            totalRides = totalRidesCount,
            avgBikeMinutesPerRide = avgBikeMinPerRide,
            avgBikeMinutesOverall = avgBikeMinOverall,
            currentRideStreak = currentRideStreak,
            bestRideMinutes = bestRide,
            totalDrinks = totalDrinksCount,
            avgDrinksPerDay = avgDrinks,
            avgDrinksPerWeek = avgDrinksWeekly,
            alcoholFreeDays = alcoholFree,
            alcoholFreePercentage = alcoholFreePct,
            currentDryStreak = currentDryStreak,
            bestDryStreak = max(bestDry, currentDryStreak)
        )
    }
}
