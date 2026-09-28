package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.ChartFilter
import com.example.ui.ChartTimeRange
import com.example.ui.HabitStatistics
import com.example.ui.components.DailyGraphItem
import com.example.ui.components.LineGraph
import com.example.ui.components.StatCard
import com.example.ui.theme.BikeAccent
import com.example.ui.theme.BikePrimary
import com.example.ui.theme.DrinkAccent
import com.example.ui.theme.DrinkPrimary
import com.example.ui.theme.SoberGreen
import com.example.util.DurationUtils

@Composable
fun ChartsScreen(
    bikeItems: List<DailyGraphItem>,
    drinkItems: List<DailyGraphItem>,
    statistics: HabitStatistics,
    selectedTimeRange: ChartTimeRange,
    selectedFilter: ChartFilter,
    onTimeRangeChanged: (ChartTimeRange) -> Unit,
    onFilterChanged: (ChartFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val rangeScrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("charts_screen")
    ) {
        // Time Range Filter Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rangeScrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChartTimeRange.entries.forEach { range ->
                val isSelected = selectedTimeRange == range
                FilterChip(
                    selected = isSelected,
                    onClick = { onTimeRangeChanged(range) },
                    label = { Text(range.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("filter_range_${range.name}")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Habit Filter Selector (Both, Bike Only, Drinks Only)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChartFilter.entries.forEach { filter ->
                val isSelected = selectedFilter == filter
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("filter_habit_${filter.name}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = filter.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. EXERCISE BIKE LINE GRAPH
        if (selectedFilter == ChartFilter.BOTH || selectedFilter == ChartFilter.BIKE_ONLY) {
            LineGraph(
                items = bikeItems,
                lineColor = BikePrimary,
                gradientColor = BikeAccent,
                unitLabel = "min",
                title = "Exercise Bike (Minutes / Day)",
                isIntegerUnits = false
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bike Quick Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Total Time",
                    value = DurationUtils.formatCompact(statistics.totalBikeMinutes),
                    subtitle = "${statistics.totalRides} rides",
                    icon = Icons.Default.Timer,
                    iconColor = BikePrimary,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Avg / Ride",
                    value = DurationUtils.formatCompact(statistics.avgBikeMinutesPerRide),
                    subtitle = "Best: ${DurationUtils.formatCompact(statistics.bestRideMinutes)}",
                    icon = Icons.Default.Speed,
                    iconColor = BikePrimary,
                    badgeText = if (statistics.currentRideStreak > 1) "${statistics.currentRideStreak}d streak!" else null,
                    badgeColor = BikePrimary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // 2. ALCOHOLIC DRINKS LINE GRAPH
        if (selectedFilter == ChartFilter.BOTH || selectedFilter == ChartFilter.DRINKS_ONLY) {
            LineGraph(
                items = drinkItems,
                lineColor = DrinkPrimary,
                gradientColor = DrinkAccent,
                unitLabel = "drinks",
                title = "Alcoholic Drinks (Count / Day)",
                isIntegerUnits = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Average Drinks Per Week Display Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("avg_drinks_per_week_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DrinkPrimary.copy(alpha = 0.12f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = DrinkPrimary,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = androidx.compose.ui.graphics.Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Average Drinks / Week",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${String.format("%.1f", statistics.avgDrinksPerDay)} drinks/day (${selectedTimeRange.label})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = String.format("%.1f", statistics.avgDrinksPerWeek),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = DrinkPrimary
                        )
                        Text(
                            text = "drinks / wk",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = DrinkPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Drinks Quick Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Total Drinks",
                    value = "${statistics.totalDrinks}",
                    subtitle = "Avg: ${String.format("%.1f", statistics.avgDrinksPerDay)} / day",
                    icon = Icons.Default.LocalBar,
                    iconColor = DrinkPrimary,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Dry Days",
                    value = "${statistics.alcoholFreeDays}",
                    subtitle = "${statistics.alcoholFreePercentage}% alcohol-free",
                    icon = Icons.Default.Star,
                    iconColor = SoberGreen,
                    badgeText = if (statistics.currentDryStreak > 0) "${statistics.currentDryStreak}d streak" else null,
                    badgeColor = SoberGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // HABIT BALANCE & INSIGHTS CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier.padding(end = 10.dp)
                    ) {
                        Row(modifier = Modifier.padding(6.dp)) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.Equalizer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = "Habit Insights (${selectedTimeRange.label})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "• Logged ${statistics.totalRides} bike session${if (statistics.totalRides == 1) "" else "s"} totaling ${DurationUtils.formatDetailed(statistics.totalBikeMinutes)}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Maintained ${statistics.alcoholFreeDays} alcohol-free day${if (statistics.alcoholFreeDays == 1) "" else "s"} (${statistics.alcoholFreePercentage}% of period).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Alcohol intake: Average ${String.format("%.1f", statistics.avgDrinksPerWeek)} drinks per week (${String.format("%.1f", statistics.avgDrinksPerDay)}/day).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (statistics.bestDryStreak > 2) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Longest alcohol-free streak: ${statistics.bestDryStreak} consecutive days! 🌟",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = SoberGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
