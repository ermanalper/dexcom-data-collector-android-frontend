package com.alptrosoft.dexcom_data_collector_android_frontend.presentation.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.InsulinDose
import com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels.InsulinUiEvent
import com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels.InsulinUiState
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun GlucoseChartScreen(
    glucoseState: GlucoseUiState,
    insulinState: InsulinUiState,
    onGlucoseEvent: (GlucoseUiEvent) -> Unit,
    onInsulinEvent: (InsulinUiEvent) -> Unit,
    horizontalGridLines: List<Int> = listOf(55, 100, 200, 300, 400)
) {
    val visibleDurationMillis = glucoseState.scaleHours * 3600000L
    var localEndTime by remember { mutableLongStateOf(glucoseState.viewEndTimeMillis) }
    var isDragging by remember { mutableStateOf(false) }
    var selectedInsulinDose by remember { mutableStateOf<InsulinDose?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    var debounceJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(glucoseState.viewEndTimeMillis) {
        if (!isDragging) {
            localEndTime = glucoseState.viewEndTimeMillis
        }
    }

    // Debounce mekanizması
    LaunchedEffect(localEndTime, visibleDurationMillis) {
        debounceJob?.cancel()
        debounceJob = coroutineScope.launch {
            delay(300) // 300ms debounce
            val viewStartTimeMillis = localEndTime - visibleDurationMillis
            onGlucoseEvent(GlucoseUiEvent.OnVisibleRangeChanged(viewStartTimeMillis, localEndTime))
            onInsulinEvent(InsulinUiEvent.FetchDoses(viewStartTimeMillis, localEndTime))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            LatestGlucoseDisplay(latestReading = glucoseState.latestReading)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "İnsülin Verilerini Grafikte Göster",
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = insulinState.showInsulin,
                    onCheckedChange = { onInsulinEvent(InsulinUiEvent.ToggleVisibility(it)) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ModernScaleSelector(
                options = listOf(1, 3, 6, 12, 24),
                selectedOption = glucoseState.scaleHours,
                onOptionSelected = { onGlucoseEvent(GlucoseUiEvent.ChangeScale(it)) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.33f)
                    .pointerInput(visibleDurationMillis, glucoseState.oldestDataLimitMillis) {
                        detectHorizontalDragGestures(
                            onDragStart = { isDragging = true },
                            onDragEnd = {
                                isDragging = false
                                onGlucoseEvent(GlucoseUiEvent.OnChartDragged(localEndTime))
                            },
                            onDragCancel = { isDragging = false },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                val millisPerPixel = visibleDurationMillis / size.width
                                var newEndTime = localEndTime - (dragAmount * millisPerPixel).toLong()
                                val limit = glucoseState.oldestDataLimitMillis ?: 0L
                                val minAllowedEndTime = limit + visibleDurationMillis
                                if (newEndTime < minAllowedEndTime && limit > 0L) {
                                    newEndTime = minAllowedEndTime
                                }
                                val now = System.currentTimeMillis()
                                if (newEndTime > now) {
                                    newEndTime = now
                                }
                                localEndTime = newEndTime
                            }
                        )
                    }
                    .pointerInput(insulinState.doses, insulinState.showInsulin, localEndTime) {
                        detectTapGestures { tapOffset ->
                            if (!insulinState.showInsulin) return@detectTapGestures

                            val width = size.width
                            val height = size.height
                            val maxGlucose = 400f

                            insulinState.doses.forEach { dose ->
                                val x = width - ((localEndTime - dose.timestampMillis).toFloat() / visibleDurationMillis) * width
                                val isUnknown = dose.glucoseVal == null || dose.glucoseVal == 0f
                                val yGlucoseValue = if (isUnknown) 0f else dose.glucoseVal!!

                                // Güvenli y hesaplaması, en alt sınır height
                                val rawY = height - (yGlucoseValue / maxGlucose) * height
                                val y = rawY.coerceIn(0f, height.toFloat())

                                val dx = tapOffset.x - x
                                val dy = tapOffset.y - y
                                if (dx * dx + dy * dy <= 2500f) {
                                    selectedInsulinDose = dose
                                }
                            }
                        }
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

                val stepMillis = when (glucoseState.scaleHours) {
                    1 -> 1800000L
                    3 -> 3600000L
                    6 -> 7200000L
                    12 -> 14400000L
                    24 -> 21600000L
                    else -> 7200000L
                }

                val zone = ZoneId.systemDefault()
                val localEnd = Instant.ofEpochMilli(localEndTime).atZone(zone)

                val nextHour = localEnd.withMinute(0).withSecond(0).withNano(0).plusHours(1)
                val nextHourMillis = nextHour.toInstant().toEpochMilli()

                val textPaint = Paint().apply {
                    color = android.graphics.Color.LTGRAY
                    textSize = 32f
                    textAlign = Paint.Align.CENTER
                }

                val formatter = DateTimeFormatter.ofPattern("HH:mm").withZone(zone)
                var tickTime = nextHourMillis - stepMillis
                while (tickTime >= viewStartTimeMillis) {
                    val tickX = width - ((localEndTime - tickTime).toFloat() / visibleDurationMillis) * width
                    if (tickX in 0f..(width - 80f)) {
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.6f),
                            start = Offset(tickX, height - 40f),
                            end = Offset(tickX, height - 20f),
                            strokeWidth = 3f
                        )
                        drawContext.canvas.nativeCanvas.drawText(
                            formatter.format(Instant.ofEpochMilli(tickTime)), tickX, height - 5f, textPaint
                        )
                    }
                    tickTime -= stepMillis
                }

                val dateFormatter = DateTimeFormatter.ofPattern("dd MMM").withZone(zone)
                var dayTick = Instant.ofEpochMilli(viewStartTimeMillis)
                    .atZone(zone).toLocalDate().plusDays(1).atStartOfDay(zone)
                    .toInstant().toEpochMilli()

                while (dayTick <= localEndTime) {
                    val dayX = width - ((localEndTime - dayTick).toFloat() / visibleDurationMillis) * width
                    if (dayX in 0f..width) {
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.9f),
                            start = Offset(dayX, height - 55f),
                            end = Offset(dayX, height - 10f),
                            strokeWidth = 5f
                        )
                        textPaint.textAlign = Paint.Align.CENTER
                        drawContext.canvas.nativeCanvas.drawText(
                            dateFormatter.format(Instant.ofEpochMilli(dayTick)), dayX, height - 62f, textPaint
                        )
                    }
                    dayTick = Instant.ofEpochMilli(dayTick)
                        .atZone(zone).toLocalDate().plusDays(1).atStartOfDay(zone)
                        .toInstant().toEpochMilli()
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

                glucoseState.readings.filter { it.timestampMillis in viewStartTimeMillis..localEndTime }.forEach { reading ->
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

                if (insulinState.showInsulin) {
                    val questionPaint = Paint().apply {
                        color = android.graphics.Color.parseColor("#800080") // Mor
                        textSize = 50f
                        textAlign = Paint.Align.CENTER
                        isFakeBoldText = true
                    }

                    insulinState.doses.filter { it.timestampMillis in viewStartTimeMillis..localEndTime }.forEach { dose ->
                        val x = width - ((localEndTime - dose.timestampMillis).toFloat() / visibleDurationMillis) * width
                        val isUnknown = dose.glucoseVal == null || dose.glucoseVal == 0f

                        if (isUnknown) {
                            // Yüksekliğin en altı (0 noktası), yazının ortalanması için -10f
                            drawContext.canvas.nativeCanvas.drawText("?", x, height - 10f, questionPaint)
                        } else {
                            val yGlucoseValue = dose.glucoseVal!!
                            val rawY = height - (yGlucoseValue / maxGlucose) * height
                            val y = rawY.coerceIn(0f, height)

                            // Glukoz noktasından (8f) daha büyük (12f) ve mor renk
                            drawCircle(color = Color(0xFF800080), radius = 12f, center = Offset(x, y))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📅 Select Date")
            }
        }

        selectedInsulinDose?.let { dose ->
            val isUnknown = dose.glucoseVal == null || dose.glucoseVal == 0f

            AlertDialog(
                onDismissRequest = { selectedInsulinDose = null },
                title = { Text("Insulin Dose Details") },
                text = {
                    Column {
                        Text("Zaman: ${dose.timestamp}")
                        Text("Doz: ${dose.dose} Ünite")
                        Text("Tip: ${dose.insulinType}")
                        Spacer(modifier = Modifier.height(8.dp))

                        if (isUnknown) {
                            Text("İnsülin anındaki glukoz değeri bilinmiyor", color = Color.Red)
                        } else {
                            Text("Glukoz Değeri: ${dose.glucoseVal}", color = Color(0xFF800080))
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedInsulinDose = null }) {
                        Text("Kapat")
                    }
                }
            )
        }

        if (showDatePicker) {
            val zone = ZoneId.systemDefault()
            val oldestDate = glucoseState.oldestDataLimitMillis?.let {
                Instant.ofEpochMilli(it).atZone(zone).toLocalDate()
            }

            val selectableDates = remember(oldestDate) {
                object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        val date = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()
                        val today = LocalDate.now(zone)
                        return oldestDate?.let {
                            !date.isBefore(it) && !date.isAfter(today)
                        } ?: false
                    }
                }
            }

            val datePickerState = rememberDatePickerState(selectableDates = selectableDates)

            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { selectedDateMillis ->
                                onGlucoseEvent(GlucoseUiEvent.GoToDate(selectedDateMillis))
                            }
                            showDatePicker = false
                        },
                        enabled = datePickerState.selectedDateMillis != null
                    ) {
                        Text("Go")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        FloatingActionButton(
            onClick = { onGlucoseEvent(GlucoseUiEvent.RefreshRequested) },
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