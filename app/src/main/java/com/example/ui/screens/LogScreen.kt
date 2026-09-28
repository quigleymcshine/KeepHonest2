package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.LogFormState
import com.example.ui.theme.BikePrimary
import com.example.ui.theme.DrinkPrimary
import com.example.ui.theme.SoberGreen
import com.example.util.DateUtils
import com.example.util.DurationUtils
import java.util.Calendar
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(
    formState: LogFormState,
    onDateSelected: (String) -> Unit,
    onBikedTodayChanged: (Boolean) -> Unit,
    onBikeMinutesChanged: (Float) -> Unit,
    onAdjustBikeMinutes: (Float) -> Unit,
    onDrinkCountChanged: (Int) -> Unit,
    onAdjustDrinkCount: (Int) -> Unit,
    onDrinkNotesChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onSaveLog: () -> Unit,
    onDeleteLog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showCustomTimeDialog by remember { mutableStateOf(false) }
    var customMinutesInput by remember { mutableStateOf("") }
    var customSecondsOption by remember { mutableStateOf(0) }

    val today = DateUtils.getTodayDate()
    val yesterday = DateUtils.getYesterdayDate()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("log_screen")
    ) {
        // Top Date Bar with Presets & Date Picker
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LOGGING FOR",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = DateUtils.formatDisplayDate(formState.date),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick "Today" chip
                    if (formState.date != today) {
                        OutlinedButton(
                            onClick = { onDateSelected(today) },
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("select_today_button"),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                        ) {
                            Text("Today", style = MaterialTheme.typography.labelMedium)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    // Calendar icon button for picking any date
                    IconButton(
                        onClick = {
                            val cal = Calendar.getInstance()
                            val dateParts = formState.date.split("-")
                            if (dateParts.size == 3) {
                                cal.set(Calendar.YEAR, dateParts[0].toIntOrNull() ?: cal.get(Calendar.YEAR))
                                cal.set(Calendar.MONTH, (dateParts[1].toIntOrNull() ?: 1) - 1)
                                cal.set(Calendar.DAY_OF_MONTH, dateParts[2].toIntOrNull() ?: cal.get(Calendar.DAY_OF_MONTH))
                            }
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val selectedIso = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                                    onDateSelected(selectedIso)
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("open_calendar_picker")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Pick date from calendar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 1: EXERCISE BIKE
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("bike_section_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header with Bike Icon & Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = BikePrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
                                    contentDescription = null,
                                    tint = BikePrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Exercise Bike",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (formState.bikedToday) "Rode today" else "Rest day / No ride",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (formState.bikedToday) BikePrimary else MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Switch(
                        checked = formState.bikedToday,
                        onCheckedChange = { onBikedTodayChanged(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BikePrimary
                        ),
                        modifier = Modifier.testTag("bike_switch_toggle")
                    )
                }

                if (formState.bikedToday) {
                    val (currMins, currSecs) = DurationUtils.toMinutesAndSeconds(formState.bikeMinutes)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Prominent Readout and Primary 30s / 1m Steppers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Minus Steppers
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedButton(
                                onClick = { onAdjustBikeMinutes(-1f) },
                                modifier = Modifier
                                    .height(44.dp)
                                    .testTag("bike_minus_1m_button"),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("-1m", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            }
                            OutlinedButton(
                                onClick = { onAdjustBikeMinutes(-0.5f) },
                                modifier = Modifier
                                    .height(44.dp)
                                    .testTag("bike_minus_30s_button"),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("-30s", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        // Time Display (clickable to enter exact time)
                        Surface(
                            onClick = {
                                customMinutesInput = currMins.toString()
                                customSecondsOption = if (currSecs >= 15) 30 else 0
                                showCustomTimeDialog = true
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = BikePrimary.copy(alpha = 0.08f),
                            modifier = Modifier.testTag("bike_time_readout_surface")
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "$currMins",
                                        style = MaterialTheme.typography.displaySmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = BikePrimary
                                    )
                                    Text(
                                        text = "m",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = BikePrimary,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = String.format("%02d", currSecs),
                                        style = MaterialTheme.typography.displaySmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (currSecs > 0) BikePrimary else BikePrimary.copy(alpha = 0.45f)
                                    )
                                    Text(
                                        text = "s",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (currSecs > 0) BikePrimary else BikePrimary.copy(alpha = 0.45f),
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                                Text(
                                    text = "Tap to type exact",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        // Plus Steppers
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilledTonalButton(
                                onClick = { onAdjustBikeMinutes(0.5f) },
                                modifier = Modifier
                                    .height(44.dp)
                                    .testTag("bike_plus_30s_button"),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = BikePrimary.copy(alpha = 0.18f),
                                    contentColor = BikePrimary
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("+30s", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            }
                            FilledTonalButton(
                                onClick = { onAdjustBikeMinutes(1f) },
                                modifier = Modifier
                                    .height(44.dp)
                                    .testTag("bike_plus_1m_button"),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = BikePrimary.copy(alpha = 0.18f),
                                    contentColor = BikePrimary
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("+1m", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Secondary adjustments: 5m quick buttons & direct :00s / :30s toggle chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { onAdjustBikeMinutes(-5f) },
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("bike_minus_5_button"),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("-5 min", style = MaterialTheme.typography.labelSmall)
                        }

                        // Quick 00 / 30 second chunk toggle
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Seconds:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            FilterChip(
                                selected = currSecs == 0,
                                onClick = {
                                    onBikeMinutesChanged(currMins.toFloat())
                                },
                                label = { Text(":00") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BikePrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("chip_sec_00")
                            )
                            FilterChip(
                                selected = currSecs == 30,
                                onClick = {
                                    onBikeMinutesChanged(currMins.toFloat() + 0.5f)
                                },
                                label = { Text(":30") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BikePrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("chip_sec_30")
                            )
                        }

                        OutlinedButton(
                            onClick = { onAdjustBikeMinutes(5f) },
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("bike_plus_5_button"),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("+5 min", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Slider snapped to 30-second chunks (0.5 minute steps)
                    Slider(
                        value = formState.bikeMinutes,
                        onValueChange = {
                            val snapped = (it * 2f).roundToInt() / 2f
                            onBikeMinutesChanged(snapped)
                        },
                        valueRange = 0.5f..120f,
                        colors = SliderDefaults.colors(
                            thumbColor = BikePrimary,
                            activeTrackColor = BikePrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bike_minutes_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("30 sec", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text("30-sec intervals", style = MaterialTheme.typography.labelSmall, color = BikePrimary, fontWeight = FontWeight.Bold)
                        Text("120 min", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Preset Chips (including 30s interval workouts)
                    Text(
                        text = "Quick Presets:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val presets = listOf(
                            Pair(15f, "15 min"),
                            Pair(20f, "20 min"),
                            Pair(22.5f, "22m 30s"),
                            Pair(30f, "30 min"),
                            Pair(45f, "45 min"),
                            Pair(60f, "60 min")
                        )
                        presets.forEach { (presetValue, presetLabel) ->
                            val isSelected = formState.bikeMinutes == presetValue
                            FilterChip(
                                selected = isSelected,
                                onClick = { onBikeMinutesChanged(presetValue) },
                                label = { Text(presetLabel) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BikePrimary.copy(alpha = 0.2f),
                                    selectedLabelColor = BikePrimary
                                ),
                                modifier = Modifier.testTag("preset_bike_${presetLabel.replace(" ", "_")}")
                            )
                        }
                    }
                }
            }
        }

        // Custom Exact Time Dialog
        if (showCustomTimeDialog) {
            AlertDialog(
                onDismissRequest = { showCustomTimeDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
                            contentDescription = null,
                            tint = BikePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Set Exact Bike Time")
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "Enter workout duration in minutes and 30-second chunks:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = customMinutesInput,
                            onValueChange = { customMinutesInput = it.filter { ch -> ch.isDigit() }.take(3) },
                            label = { Text("Minutes") },
                            placeholder = { Text("e.g. 22") },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_bike_minutes_input")
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Seconds (30s chunks):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(0, 30).forEach { secOption ->
                                val isSel = customSecondsOption == secOption
                                FilterChip(
                                    selected = isSel,
                                    onClick = { customSecondsOption = secOption },
                                    label = { Text(if (secOption == 0) ":00 sec" else ":30 sec") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BikePrimary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("custom_seconds_$secOption")
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val parsedMin = customMinutesInput.toIntOrNull() ?: 0
                            val finalMins = DurationUtils.fromMinutesAndSeconds(parsedMin, customSecondsOption)
                            onBikeMinutesChanged(finalMins)
                            showCustomTimeDialog = false
                        },
                        modifier = Modifier.testTag("apply_custom_time_button")
                    ) {
                        Text("Set Time")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCustomTimeDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 2: ALCOHOLIC DRINKS
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("drink_section_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header with Drink Icon & Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val isDry = formState.drinkCount == 0
                        Surface(
                            shape = CircleShape,
                            color = if (isDry) SoberGreen.copy(alpha = 0.15f) else DrinkPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isDry) Icons.Default.Star else Icons.Default.LocalBar,
                                    contentDescription = null,
                                    tint = if (isDry) SoberGreen else DrinkPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Alcoholic Drinks",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (formState.drinkCount == 0) "Zero drinks (Alcohol-free day! ⭐)" else "${formState.drinkCount} standard drink(s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (formState.drinkCount == 0) SoberGreen else DrinkPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Drink Counter Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onAdjustDrinkCount(-1) },
                        enabled = formState.drinkCount > 0,
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            .testTag("drink_minus_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease drink count",
                            tint = if (formState.drinkCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${formState.drinkCount}",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (formState.drinkCount == 0) SoberGreen else DrinkPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (formState.drinkCount == 1) "drink" else "drinks",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { onAdjustDrinkCount(1) },
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            .testTag("drink_plus_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase drink count",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Presets: 0 (Zero/Dry), 1, 2, 3, 4
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0, 1, 2, 3, 4).forEach { count ->
                        val isSelected = formState.drinkCount == count
                        FilterChip(
                            selected = isSelected,
                            onClick = { onDrinkCountChanged(count) },
                            label = {
                                Text(if (count == 0) "0 (Dry ⭐)" else "$count")
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (count == 0) SoberGreen.copy(alpha = 0.2f) else DrinkPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = if (count == 0) SoberGreen else DrinkPrimary
                            ),
                            modifier = Modifier
                                .weight(if (count == 0) 1.5f else 1f)
                                .testTag("preset_drink_$count")
                        )
                    }
                }

                if (formState.drinkCount > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = formState.drinkNotes,
                        onValueChange = onDrinkNotesChanged,
                        label = { Text("Drink details (optional)") },
                        placeholder = { Text("e.g. 2 IPAs, 1 glass red wine") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("drink_notes_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 3: OPTIONAL GENERAL NOTES
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Daily Notes (optional)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = formState.notes,
                    onValueChange = onNotesChanged,
                    placeholder = { Text("How was your workout or day? Add any context...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("general_notes_input"),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SAVE & DELETE ACTIONS
        Button(
            onClick = onSaveLog,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("save_log_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (formState.isExistingLog) "Update Entry" else "Save Daily Log",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (formState.isExistingLog) {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onDeleteLog,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("delete_current_log_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete This Entry")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
