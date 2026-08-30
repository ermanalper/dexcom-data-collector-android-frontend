package com.alptrosoft.dexcom_data_collector_android_frontend.presentation.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.GlucoseReading
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun GlucoseChartScreen(
    readings: List<GlucoseReading>,
    horizontalGridLines: List<Int> = listOf(55, 100, 200, 300, 400),
    onLoadMore: (Long, Long) -> Unit
) {
    var scaleHours by remember { mutableIntStateOf(6) }
    val visibleDurationMillis = scaleHours * 3600000L
    var viewEndTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var isDragging by remember { mutableStateOf(false) }

    val currentReadings by rememberUpdatedState(readings)

    LaunchedEffect(isDragging, viewEndTimeMillis, visibleDurationMillis, readings.isEmpty()) {
        if (isDragging) return@LaunchedEffect

        if (readings.isEmpty()) {
            val fetchEnd = viewEndTimeMillis
            val fetchStart = fetchEnd - (visibleDurationMillis * 2)
            onLoadMore(fetchStart, fetchEnd)
        } else {
            val viewStartTimeMillis = viewEndTimeMillis - visibleDurationMillis
            val earliestLoadedTime = readings.first().timestampMillis
            val latestLoadedTime = readings.last().timestampMillis

            if (viewStartTimeMillis < earliestLoadedTime + (visibleDurationMillis * 0.2)) {
                val fetchEnd = earliestLoadedTime
                val fetchStart = fetchEnd - (visibleDurationMillis * 2)
                onLoadMore(fetchStart, fetchEnd)
            }

            if (viewEndTimeMillis > latestLoadedTime - (visibleDurationMillis * 0.2)) {
                val fetchStart = latestLoadedTime
                val fetchEnd = fetchStart + (visibleDurationMillis * 2)
                val finalFetchEnd = if (fetchEnd > System.currentTimeMillis()) System.currentTimeMillis() else fetchEnd

                if (fetchStart < finalFetchEnd) {
                    onLoadMore(fetchStart, finalFetchEnd)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(3, 6, 12, 24).forEach { hours ->
                Button(onClick = { scaleHours = hours }) {
                    Text("${hours}s")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.33f)
                .pointerInput(visibleDurationMillis) {
                    detectHorizontalDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val millisPerPixel = visibleDurationMillis / size.width
                            var newEndTime = viewEndTimeMillis - (dragAmount * millisPerPixel).toLong()

                            if (currentReadings.isNotEmpty()) {
                                val earliestLoadedTime = currentReadings.first().timestampMillis

                                val minAllowedEndTime = earliestLoadedTime + visibleDurationMillis
                                if (newEndTime < minAllowedEndTime) {
                                    newEndTime = minAllowedEndTime
                                }

                                val maxAllowedEndTime = System.currentTimeMillis()
                                if (newEndTime > maxAllowedEndTime) {
                                    newEndTime = maxAllowedEndTime
                                }
                            }

                            viewEndTimeMillis = newEndTime
                        }
                    )
                }
        ) {
            val viewStartTimeMillis = viewEndTimeMillis - visibleDurationMillis
            val width = size.width
            val height = size.height
            val maxGlucose = 400f

            // horizontal lines
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

            // X axis time stamps
            val stepHours = when (scaleHours) {
                3 -> 1
                6 -> 2
                12 -> 4
                24 -> 6
                else -> 2
            }
            val stepMillis = stepHours * 3600000L
            val oneHourMillis = 3600000L

            val zoneOffset = ZoneId.systemDefault().rules.getOffset(Instant.now()).totalSeconds * 1000L
            val localTimeMillis = viewEndTimeMillis + zoneOffset
            val nextHourLocalMillis = ((localTimeMillis / oneHourMillis) + 1) * oneHourMillis
            val nextHourMillis = nextHourLocalMillis - zoneOffset

            val textPaint = Paint().apply {
                color = android.graphics.Color.LTGRAY
                textSize = 32f
                textAlign = Paint.Align.CENTER
            }

            val formatter = DateTimeFormatter.ofPattern("HH").withZone(ZoneId.systemDefault())

            // ticks
            var tickTime = nextHourMillis - stepMillis
            while (tickTime >= viewStartTimeMillis) {
                val tickX = width - ((viewEndTimeMillis - tickTime).toFloat() / visibleDurationMillis) * width
                if (tickX in 0f..(width - 80f)) {
                    drawLine(Color.LightGray.copy(alpha = 0.6f), Offset(tickX, height - 40f), Offset(tickX, height - 20f), strokeWidth = 3f)
                    drawContext.canvas.nativeCanvas.drawText(
                        formatter.format(Instant.ofEpochMilli(tickTime)), tickX, height - 5f, textPaint
                    )
                }
                tickTime -= stepMillis
            }

            val nowX = width - 15f
            drawLine(Color.LightGray.copy(alpha = 0.6f), Offset(nowX, height - 40f), Offset(nowX, height - 20f), strokeWidth = 3f)
            textPaint.textAlign = Paint.Align.RIGHT
            drawContext.canvas.nativeCanvas.drawText("Now", nowX + 10f, height - 5f, textPaint)

            // Glucose readings
            readings.filter { it.timestampMillis in viewStartTimeMillis..viewEndTimeMillis }.forEach { reading ->
                val x = width - ((viewEndTimeMillis - reading.timestampMillis).toFloat() / visibleDurationMillis) * width
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
}