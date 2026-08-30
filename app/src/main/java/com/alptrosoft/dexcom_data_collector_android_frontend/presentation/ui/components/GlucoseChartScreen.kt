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

            // load more past
            if (viewStartTimeMillis < earliestLoadedTime + (visibleDurationMillis * 0.2)) {
                val fetchEnd = earliestLoadedTime
                val fetchStart = fetchEnd - (visibleDurationMillis * 2)
                onLoadMore(fetchStart, fetchEnd)
            }

            // load more newer
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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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
                .fillMaxSize()
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

            // print times (00:00, 03:00, 06:00...)
            val stepMillis = 3 * 3600000L
            val firstTick = viewStartTimeMillis - (viewStartTimeMillis % stepMillis)
            val textPaint = Paint().apply { color = android.graphics.Color.GRAY; textSize = 32f }
            val formatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

            for (tickTime in firstTick..viewEndTimeMillis step stepMillis) {
                val tickX = width - ((viewEndTimeMillis - tickTime).toFloat() / visibleDurationMillis) * width
                if (tickX in 0f..width) {
                    drawLine(Color.LightGray, Offset(tickX, 0f), Offset(tickX, height))
                    drawContext.canvas.nativeCanvas.drawText(
                        formatter.format(Instant.ofEpochMilli(tickTime)), tickX, height - 10f, textPaint
                    )
                }
            }

            val maxGlucose = 400f

            // print values
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