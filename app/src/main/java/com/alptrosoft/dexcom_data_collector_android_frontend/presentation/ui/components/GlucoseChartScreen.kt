package com.alptrosoft.dexcom_data_collector_android_frontend.presentation.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels.GlucoseUiState
import com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels.GlucoseUiEvent
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
@Composable
fun GlucoseChartScreen(
    state: GlucoseUiState,
    onEvent: (GlucoseUiEvent) -> Unit,
    horizontalGridLines: List<Int> = listOf(55, 100, 200, 300, 400)
) {
    val visibleDurationMillis = state.scaleHours * 3600000L

    var localEndTime by remember { mutableLongStateOf(state.viewEndTimeMillis) }
    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(state.viewEndTimeMillis) {
        if (!isDragging) {
            localEndTime = state.viewEndTimeMillis
        }
    }

    LaunchedEffect(localEndTime, visibleDurationMillis) {
        delay(200) // debounce
        val viewStartTimeMillis = localEndTime - visibleDurationMillis
        onEvent(GlucoseUiEvent.OnVisibleRangeChanged(viewStartTimeMillis, localEndTime))
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            LatestGlucoseDisplay(latestReading = state.latestReading)

            Spacer(modifier = Modifier.height(16.dp))

            ModernScaleSelector(
                options = listOf(1, 3, 6, 12, 24),
                selectedOption = state.scaleHours,
                onOptionSelected = { onEvent(GlucoseUiEvent.ChangeScale(it)) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.33f)
                    .pointerInput(visibleDurationMillis, state.oldestDataLimitMillis) {
                        detectHorizontalDragGestures(
                            onDragStart = { isDragging = true },
                            onDragEnd = {
                                isDragging = false
                                // notify viewmodel after dragging
                                onEvent(GlucoseUiEvent.OnChartDragged(localEndTime))
                            },
                            onDragCancel = { isDragging = false },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()

                                val millisPerPixel = visibleDurationMillis / size.width
                                var newEndTime = localEndTime - (dragAmount * millisPerPixel).toLong()

                                // first data entry date (left limit)
                                val limit = state.oldestDataLimitMillis ?: 0L
                                val minAllowedEndTime = limit + visibleDurationMillis

                                if (newEndTime < minAllowedEndTime && limit > 0L) {
                                    newEndTime = minAllowedEndTime
                                }

                                // right limit
                                val now = System.currentTimeMillis()
                                if (newEndTime > now) {
                                    newEndTime = now
                                }

                                localEndTime = newEndTime
                            }
                        )
                    }
            ) {
                val viewStartTimeMillis = localEndTime - visibleDurationMillis
                val width = size.width
                val height = size.height
                val maxGlucose = 400f

                val gridTextPaint = Paint().apply {
                    color = android.graphics.Color.LTGRAY
                    textSize = 28f
                    textAlign = Paint.Align.RIGHT
                }

                horizontalGridLines.forEach { lineValue ->
                    val y = height - (lineValue.toFloat() / maxGlucose) * height
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.4f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 2f
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        lineValue.toString(), width - 10f, y - 8f, gridTextPaint
                    )
                }

                val stepMillis = when (state.scaleHours) {
                    1 -> 1800000L
                    3 -> 3600000L
                    6 -> 7200000L
                    12 -> 14400000L
                    24 -> 21600000L
                    else -> 7200000L
                }
                val oneHourMillis = 3600000L

                val zone = ZoneId.systemDefault()
                val localEnd = Instant.ofEpochMilli(localEndTime).atZone(zone)

                val nextHour = localEnd
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0)
                    .plusHours(1)

                val nextHourMillis = nextHour.toInstant().toEpochMilli()

                val textPaint = Paint().apply {
                    color = android.graphics.Color.LTGRAY
                    textSize = 32f
                    textAlign = Paint.Align.CENTER
                }

                val formatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

                var tickTime = nextHourMillis - stepMillis
                while (tickTime >= viewStartTimeMillis) {
                    val tickX =
                        width - ((localEndTime - tickTime).toFloat() / visibleDurationMillis) * width

                    if (tickX in 0f..(width - 80f)) {
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.6f),
                            start = Offset(tickX, height - 40f),
                            end = Offset(tickX, height - 20f),
                            strokeWidth = 3f
                        )

                        drawContext.canvas.nativeCanvas.drawText(
                            formatter.format(Instant.ofEpochMilli(tickTime)),
                            tickX,
                            height - 5f,
                            textPaint
                        )
                    }

                    tickTime -= stepMillis
                }
                val dateFormatter =
                    DateTimeFormatter.ofPattern("dd MMM").withZone(zone)

                var dayTick =
                    Instant.ofEpochMilli(viewStartTimeMillis)
                        .atZone(zone)
                        .toLocalDate()
                        .plusDays(1)
                        .atStartOfDay(zone)
                        .toInstant()
                        .toEpochMilli()

                while (dayTick <= localEndTime) {
                    val dayX =
                        width - ((localEndTime - dayTick).toFloat() / visibleDurationMillis) * width

                    if (dayX in 0f..width) {
                        // Daily separator
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.9f),
                            start = Offset(dayX, height - 55f),
                            end = Offset(dayX, height - 10f),
                            strokeWidth = 5f
                        )

                        // Date label
                        textPaint.textAlign = Paint.Align.CENTER
                        drawContext.canvas.nativeCanvas.drawText(
                            dateFormatter.format(Instant.ofEpochMilli(dayTick)),
                            dayX,
                            height - 62f,
                            textPaint
                        )
                    }

                    dayTick =
                        Instant.ofEpochMilli(dayTick)
                            .atZone(zone)
                            .toLocalDate()
                            .plusDays(1)
                            .atStartOfDay(zone)
                            .toInstant()
                            .toEpochMilli()
                }

                val nowX = width - ((localEndTime - System.currentTimeMillis()).toFloat() / visibleDurationMillis) * width
                if (nowX in 0f..width) {
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.6f),
                        start = Offset(nowX, height - 40f),
                        end = Offset(nowX, height - 20f),
                        strokeWidth = 3f
                    )
                    textPaint.textAlign = Paint.Align.RIGHT
                    drawContext.canvas.nativeCanvas.drawText("Now", nowX - 10f, height - 5f, textPaint)
                }

                // point glucose readings
                state.readings.filter { it.timestampMillis in viewStartTimeMillis..localEndTime }.forEach { reading ->
                    val x = width - ((localEndTime - reading.timestampMillis).toFloat() / visibleDurationMillis) * width
                    val y = height - (reading.value / maxGlucose) * height

                    val pointColor = when (reading.status.uppercase()) {
                        "NORMAL" -> Color.Green
                        "WARNING" -> Color.Yellow
                        "CRITICAL" -> Color.Red
                        else -> Color.Blue
                    }

                    drawCircle(color = pointColor, radius = 8f, center = Offset(x, y))
                }
            }
        }
        FloatingActionButton(
            onClick = { onEvent(GlucoseUiEvent.RefreshRequested) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh Data"
            )
        }

    }

}