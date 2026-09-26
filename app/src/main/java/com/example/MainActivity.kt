package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.reminder.ReminderScheduler
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.screens.ChartsScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.LogScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.BikePrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.util.DateUtils

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create notification channel on launch
        ReminderScheduler.createNotificationChannel(this)

        handleIntent(intent)

        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_OPEN_LOG, false) == true) {
            viewModel.navigateTo(AppScreen.LOG)
            viewModel.loadLogForDate(DateUtils.getTodayDate())
        }
    }

    companion object {
        const val EXTRA_OPEN_LOG = "extra_open_log"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val logFormState by viewModel.logFormState.collectAsStateWithLifecycle()
    val allLogs by viewModel.allLogs.collectAsStateWithLifecycle()
    val chartData by viewModel.chartData.collectAsStateWithLifecycle()
    val statistics by viewModel.statistics.collectAsStateWithLifecycle()
    val selectedTimeRange by viewModel.selectedTimeRange.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedChartFilter.collectAsStateWithLifecycle()

    val reminderEnabled by viewModel.reminderEnabled.collectAsStateWithLifecycle()
    val reminderHour by viewModel.reminderHour.collectAsStateWithLifecycle()
    val reminderMinute by viewModel.reminderMinute.collectAsStateWithLifecycle()

    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle back button on secondary screens
    if (currentScreen != AppScreen.LOG) {
        BackHandler {
            viewModel.navigateTo(AppScreen.LOG)
        }
    }

    // Display snackbar feedback
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "KeepHonest",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = currentScreen == AppScreen.LOG,
                    onClick = { viewModel.navigateTo(AppScreen.LOG) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Log Today",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Log Day") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BikePrimary,
                        selectedTextColor = BikePrimary,
                        indicatorColor = BikePrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_log")
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.CHARTS,
                    onClick = { viewModel.navigateTo(AppScreen.CHARTS) },
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ShowChart,
                            contentDescription = "Line Graphs & Stats",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Trends") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BikePrimary,
                        selectedTextColor = BikePrimary,
                        indicatorColor = BikePrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_charts")
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.HISTORY,
                    onClick = { viewModel.navigateTo(AppScreen.HISTORY) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Past History Logs",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("History") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BikePrimary,
                        selectedTextColor = BikePrimary,
                        indicatorColor = BikePrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_history")
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.SETTINGS,
                    onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Reminder Settings",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BikePrimary,
                        selectedTextColor = BikePrimary,
                        indicatorColor = BikePrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_settings")
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        when (currentScreen) {
            AppScreen.LOG -> {
                LogScreen(
                    formState = logFormState,
                    onDateSelected = { viewModel.loadLogForDate(it) },
                    onBikedTodayChanged = { viewModel.setBikedToday(it) },
                    onBikeMinutesChanged = { viewModel.setBikeMinutes(it) },
                    onAdjustBikeMinutes = { viewModel.adjustBikeMinutes(it) },
                    onDrinkCountChanged = { viewModel.setDrinkCount(it) },
                    onAdjustDrinkCount = { viewModel.adjustDrinkCount(it) },
                    onDrinkNotesChanged = { viewModel.setDrinkNotes(it) },
                    onNotesChanged = { viewModel.setNotes(it) },
                    onSaveLog = { viewModel.saveCurrentLog() },
                    onDeleteLog = { viewModel.deleteCurrentLog() },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppScreen.CHARTS -> {
                ChartsScreen(
                    bikeItems = chartData.first,
                    drinkItems = chartData.second,
                    statistics = statistics,
                    selectedTimeRange = selectedTimeRange,
                    selectedFilter = selectedFilter,
                    onTimeRangeChanged = { viewModel.setTimeRange(it) },
                    onFilterChanged = { viewModel.setChartFilter(it) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppScreen.HISTORY -> {
                HistoryScreen(
                    logs = allLogs,
                    onSelectLogForEdit = { date ->
                        viewModel.loadLogForDate(date)
                        viewModel.navigateTo(AppScreen.LOG)
                    },
                    onDeleteLog = { viewModel.deleteLog(it) },
                    onAddSampleData = { viewModel.populateSampleData() },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppScreen.SETTINGS -> {
                SettingsScreen(
                    reminderEnabled = reminderEnabled,
                    reminderHour = reminderHour,
                    reminderMinute = reminderMinute,
                    logs = allLogs,
                    onReminderToggle = { viewModel.setReminderEnabled(it) },
                    onTimeSelected = { h, m -> viewModel.setReminderTime(h, m) },
                    onSendTestNotification = { viewModel.triggerTestNotification() },
                    onPopulateSampleData = { viewModel.populateSampleData() },
                    onClearAllData = { viewModel.clearAllData() },
                    onRestoreBackup = { viewModel.restoreFromJson(it) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
