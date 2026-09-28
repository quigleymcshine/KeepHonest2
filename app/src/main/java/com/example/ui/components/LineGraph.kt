package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.DateUtils
import com.example.util.DurationUtils
import kotlin.math.max
import kotlin.math.roundToInt

data class DailyGraphItem(
    val dateStr: String,
    val value: Float,
    val isRiddenOrDry: Boolean = false // e.g. true if biked > 0 or drinks == 0
)

@Composable
fun LineGraph(
    items: List<DailyGraphItem>,
    lineColor: Color,
    gradientColor: Color,
    unitLabel: String,
    title: String,
    modifier: Modifier = Modifier,
    isIntegerUnits: Boolean = true,
    targetGoalValue: Float? = null,
    targetGoalLabel: String? = null
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(items) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(1f, animationSpec = tween(durationMillis = 600))
        selectedIndex = null
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("chart_card_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with Title & Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(lineColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (items.isNotEmpty()) {
                    val avg = items.map { it.value }.average().toFloat()
                    val displayAvgText = if (unitLabel == "min") {
                        DurationUtils.formatCompact(avg)
                    } else {
                        "${String.format("%.1f", avg)} $unitLabel"
                    }
                    Text(
                        text = "Avg: $displayAvgText",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tooltip inspection banner if touched
            val selectedItem = selectedIndex?.let { idx -> items.getOrNull(idx) }
            if (selectedItem != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = DateUtils.formatDisplayDate(selectedItem.dateStr),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        val formattedValText = if (unitLabel == "min") {
                            DurationUtils.formatCompact(selectedItem.value)
                        } else {
                            val numStr = if (isIntegerUnits) {
                                selectedItem.value.roundToInt().toString()
                            } else {
                                String.format("%.1f", selectedItem.value)
                            }
                            "$numStr $unitLabel"
                        }
                        Text(
                            text = formattedValText,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = lineColor
                        )
                    }
                }
            } else {
                Text(
                    text = "Tap or drag on chart to inspect days",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data recorded for this period",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Main Canvas Chart
                val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                val surfaceColor = MaterialTheme.colorScheme.surface
                val outlineColor = MaterialTheme.colorScheme.outline

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(items) {
                                detectTapGestures(
                                    onPress = { offset ->
                                        val paddingLeft = 40.dp.toPx()
                                        val paddingRight = 16.dp.toPx()
                                        val graphWidth = size.width - paddingLeft - paddingRight
                                        if (items.size > 1 && offset.x >= paddingLeft && offset.x <= size.width - paddingRight) {
                                            val stepX = graphWidth / (items.size - 1)
                                            val index = ((offset.x - paddingLeft) / stepX).roundToInt().coerceIn(0, items.size - 1)
                                            selectedIndex = index
                                        } else if (items.size == 1) {
                                            selectedIndex = 0
                                        }
                                    }
                                )
                            }
                            .pointerInput(items) {
                                detectDragGestures(
                                    onDrag = { change, _ ->
                                        change.consume()
                                        val paddingLeft = 40.dp.toPx()
                                        val paddingRight = 16.dp.toPx()
                                        val graphWidth = size.width - paddingLeft - paddingRight
                                        if (items.size > 1) {
                                            val x = change.position.x.coerceIn(paddingLeft, size.width - paddingRight)
                                            val stepX = graphWidth / (items.size - 1)
                                            val index = ((x - paddingLeft) / stepX).roundToInt().coerceIn(0, items.size - 1)
                                            selectedIndex = index
                                        }
                                    }
                                )
                            }
                    ) {
                        val paddingLeft = 40.dp.toPx()
                        val paddingRight = 16.dp.toPx()
                        val paddingTop = 16.dp.toPx()
                        val paddingBottom = 28.dp.toPx()

                        val graphWidth = size.width - paddingLeft - paddingRight
                        val graphHeight = size.height - paddingTop - paddingBottom

                        if (graphWidth <= 0 || graphHeight <= 0) return@Canvas

                        val rawMax = items.maxOfOrNull { it.value } ?: 0f
                        // Choose a friendly ceiling for Y axis
                        val maxY = when {
                            rawMax <= 0f -> 5f
                            rawMax <= 5f -> 5f
                            rawMax <= 10f -> 10f
                            rawMax <= 30f -> 30f
                            rawMax <= 60f -> 60f
                            rawMax <= 90f -> 90f
                            else -> ((rawMax / 10).toInt() + 1) * 10f
                        }

                        // Draw Horizontal Gridlines (4 levels: 0, 1/3, 2/3, maxY)
                        val gridSteps = 3
                        val textPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 10.sp.toPx()
                            isAntiAlias = true
                            textAlign = android.graphics.Paint.Align.RIGHT
                        }

                        for (i in 0..gridSteps) {
                            val fraction = i / gridSteps.toFloat()
                            val y = paddingTop + graphHeight * (1f - fraction)
                            val gridVal = (maxY * fraction)
                            val gridLabel = if (isIntegerUnits) gridVal.roundToInt().toString() else String.format("%.0f", gridVal)

                            // Gridline
                            drawLine(
                                color = gridColor,
                                start = Offset(paddingLeft, y),
                                end = Offset(size.width - paddingRight, y),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = if (i > 0) PathEffect.dashPathEffect(floatArrayOf(8f, 8f)) else null
                            )

                            // Y-axis label
                            drawContext.canvas.nativeCanvas.drawText(
                                gridLabel,
                                paddingLeft - 8.dp.toPx(),
                                y + 4.dp.toPx(),
                                textPaint
                            )
                        }

                        // Compute points for data
                        val count = items.size
                        val stepX = if (count > 1) graphWidth / (count - 1) else graphWidth / 2

                        val points = items.mapIndexed { idx, item ->
                            val x = if (count > 1) paddingLeft + idx * stepX else paddingLeft + graphWidth / 2
                            val normalizedVal = (item.value / maxY).coerceIn(0f, 1f)
                            val animatedVal = normalizedVal * animatedProgress.value
                            val y = paddingTop + graphHeight * (1f - animatedVal)
                            Offset(x, y)
                        }

                        // Draw Area Gradient Fill
                        if (points.isNotEmpty()) {
                            val fillPath = Path().apply {
                                moveTo(points.first().x, paddingTop + graphHeight)
                                lineTo(points.first().x, points.first().y)
                                for (i in 1 until points.size) {
                                    val prev = points[i - 1]
                                    val curr = points[i]
                                    val controlX1 = prev.x + (curr.x - prev.x) / 2
                                    val controlY1 = prev.y
                                    val controlX2 = prev.x + (curr.x - prev.x) / 2
                                    val controlY2 = curr.y
                                    cubicTo(controlX1, controlY1, controlX2, controlY2, curr.x, curr.y)
                                }
                                lineTo(points.last().x, paddingTop + graphHeight)
                                close()
                            }

                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        gradientColor.copy(alpha = 0.35f),
                                        gradientColor.copy(alpha = 0.05f),
                                        Color.Transparent
                                    ),
                                    startY = paddingTop,
                                    endY = paddingTop + graphHeight
                                )
                            )

                            // Draw Line Stroke
                            val linePath = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                for (i in 1 until points.size) {
                                    val prev = points[i - 1]
                                    val curr = points[i]
                                    val controlX1 = prev.x + (curr.x - prev.x) / 2
                                    val controlY1 = prev.y
                                    val controlX2 = prev.x + (curr.x - prev.x) / 2
                                    val controlY2 = curr.y
                                    cubicTo(controlX1, controlY1, controlX2, controlY2, curr.x, curr.y)
                                }
                            }

                            drawPath(
                                path = linePath,
                                color = lineColor,
                                style = Stroke(
                                    width = 3.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            )

                            // Draw Points
                            points.forEachIndexed { idx, point ->
                                val isSelected = selectedIndex == idx
                                val hasActivity = items[idx].value > 0

                                // Outer glow if selected
                                if (isSelected) {
                                    drawCircle(
                                        color = lineColor.copy(alpha = 0.25f),
                                        radius = 10.dp.toPx(),
                                        center = point
                                    )
                                }

                                drawCircle(
                                    color = if (hasActivity) lineColor else outlineColor.copy(alpha = 0.6f),
                                    radius = if (isSelected) 5.dp.toPx() else 3.5.dp.toPx(),
                                    center = point
                                )
                                drawCircle(
                                    color = surfaceColor,
                                    radius = if (isSelected) 3.dp.toPx() else 2.dp.toPx(),
                                    center = point
                                )
                            }

                            // Selected scrub line
                            selectedIndex?.let { selIdx ->
                                if (selIdx in points.indices) {
                                    val selPoint = points[selIdx]
                                    drawLine(
                                        color = lineColor.copy(alpha = 0.6f),
                                        start = Offset(selPoint.x, paddingTop),
                                        end = Offset(selPoint.x, paddingTop + graphHeight),
                                        strokeWidth = 1.5.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                                    )
                                }
                            }
                        }

                        // Draw X-axis Date Labels (sample evenly so labels do not overlap)
                        val labelInterval = when {
                            count <= 7 -> 1
                            count <= 14 -> 2
                            count <= 30 -> 5
                            else -> 7
                        }

                        val xTextPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 9.sp.toPx()
                            isAntiAlias = true
                            textAlign = android.graphics.Paint.Align.CENTER
                        }

                        for (idx in 0 until count) {
                            if (idx % labelInterval == 0 || idx == count - 1) {
                                val x = if (count > 1) paddingLeft + idx * stepX else paddingLeft + graphWidth / 2
                                val dateLabel = DateUtils.formatChartDate(items[idx].dateStr)
                                drawContext.canvas.nativeCanvas.drawText(
                                    dateLabel,
                                    x,
                                    size.height - 6.dp.toPx(),
                                    xTextPaint
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
