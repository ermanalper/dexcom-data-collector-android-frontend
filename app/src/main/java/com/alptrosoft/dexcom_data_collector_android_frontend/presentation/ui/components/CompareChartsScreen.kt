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
import androidx.hilt.navigation.compose.hiltViewModel
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.GlucoseReading
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.InsulinDose
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.Meal
import com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels.GlucoseUiEvent
import com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels.GlucoseViewModel
import com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels.InsulinDoseViewModel
import com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels.InsulinUiEvent
import com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels.MealUiEvent
import com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels.MealViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings

@Composable
fun CompareChartsScreen(
    topGlucoseVM: GlucoseViewModel = hiltViewModel(key = "top_glucose"),
    topInsulinVM: InsulinDoseViewModel = hiltViewModel(key = "top_insulin"),
    topMealVM: MealViewModel = hiltViewModel(key = "top_meal"),

    bottomGlucoseVM: GlucoseViewModel = hiltViewModel(key = "bottom_glucose"),
    bottomInsulinVM: InsulinDoseViewModel = hiltViewModel(key = "bottom_insulin"),
    bottomMealVM: MealViewModel = hiltViewModel(key = "bottom_meal")
) {
    val topGlucoseState by topGlucoseVM.state.collectAsState()
    val topInsulinState by topInsulinVM.state.collectAsState()
    val topMealState by topMealVM.state.collectAsState()

    val bottomGlucoseState by bottomGlucoseVM.state.collectAsState()
    val bottomInsulinState by bottomInsulinVM.state.collectAsState()
    val bottomMealState by bottomMealVM.state.collectAsState()

    var topEndTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var bottomEndTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var showSettingsDialog by remember { mutableStateOf(false) }
    var isTimeSynced by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Grafik Karşılaştırma",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = { showSettingsDialog = true }) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = "Ayarlar")
            }
        }

        ModernScaleSelector(
            options = listOf(1, 3, 6, 12, 24),
            selectedOption = topGlucoseState.scaleHours,
            onOptionSelected = { newScale ->
                topGlucoseVM.onEvent(GlucoseUiEvent.ChangeScale(newScale))
                bottomGlucoseVM.onEvent(GlucoseUiEvent.ChangeScale(newScale))
            },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 1. Üst Grafik
        ComparisonChart(
            glucoseReadings = topGlucoseState.readings,
            insulinDoses = topInsulinState.doses,
            meals = topMealState.meals,
            showInsulin = topInsulinState.showInsulin,
            showMeals = topMealState.showMeals,
            scaleHours = topGlucoseState.scaleHours,
            oldestDataLimitMillis = topGlucoseState.oldestDataLimitMillis,
            endTimeMillis = topEndTime,
            onEndTimeChange = { newEnd ->
                // Delta (değişim miktarı) hesapla ve eşitliyse alt grafiğe de aynı değişimi uygula
                val delta = newEnd - topEndTime
                topEndTime = newEnd
                if (isTimeSynced) {
                    bottomEndTime += delta
                }
            },
            onFetchData = { start, end ->
                topGlucoseVM.onEvent(GlucoseUiEvent.OnVisibleRangeChanged(start, end))
                topInsulinVM.onEvent(InsulinUiEvent.FetchDoses(start, end))
                topMealVM.onEvent(MealUiEvent.FetchMeals(start, end))
            },
            modifier = Modifier.weight(1f)
        )

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // 2. Alt Grafik
        ComparisonChart(
            glucoseReadings = bottomGlucoseState.readings,
            insulinDoses = bottomInsulinState.doses,
            meals = bottomMealState.meals,
            showInsulin = bottomInsulinState.showInsulin,
            showMeals = bottomMealState.showMeals,
            scaleHours = bottomGlucoseState.scaleHours,
            oldestDataLimitMillis = bottomGlucoseState.oldestDataLimitMillis,
            endTimeMillis = bottomEndTime,
            onEndTimeChange = { newEnd ->
                // Delta (değişim miktarı) hesapla ve eşitliyse üst grafiğe de aynı değişimi uygula
                val delta = newEnd - bottomEndTime
                bottomEndTime = newEnd
                if (isTimeSynced) {
                    topEndTime += delta
                }
            },
            onFetchData = { start, end ->
                bottomGlucoseVM.onEvent(GlucoseUiEvent.OnVisibleRangeChanged(start, end))
                bottomInsulinVM.onEvent(InsulinUiEvent.FetchDoses(start, end))
                bottomMealVM.onEvent(MealUiEvent.FetchMeals(start, end))
            },
            modifier = Modifier.weight(1f)
        )
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Karşılaştırma Ayarları") },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Saati Eşitle", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = isTimeSynced,
                            onCheckedChange = { synced ->
                                isTimeSynced = synced
                                if (synced) {
                                    val zone = ZoneId.systemDefault()
                                    val visibleDuration = topGlucoseState.scaleHours * 3600000L

                                    // Üst grafiği KENDİ gününün başlangıcına eşitle
                                    val topStartMillis = topEndTime - visibleDuration
                                    val topStartLocalDate = Instant.ofEpochMilli(topStartMillis).atZone(zone).toLocalDate()
                                    val topDayStartMillis = topStartLocalDate.atStartOfDay(zone).toInstant().toEpochMilli()
                                    topEndTime = topDayStartMillis + visibleDuration

                                    // Alt grafiği KENDİ gününün başlangıcına eşitle
                                    val bottomStartMillis = bottomEndTime - visibleDuration
                                    val bottomStartLocalDate = Instant.ofEpochMilli(bottomStartMillis).atZone(zone).toLocalDate()
                                    val bottomDayStartMillis = bottomStartLocalDate.atStartOfDay(zone).toInstant().toEpochMilli()
                                    bottomEndTime = bottomDayStartMillis + visibleDuration
                                }
                            }
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "İnsülin Göster", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = topInsulinState.showInsulin,
                            onCheckedChange = { show ->
                                topInsulinVM.onEvent(InsulinUiEvent.ToggleVisibility(show))
                                bottomInsulinVM.onEvent(InsulinUiEvent.ToggleVisibility(show))
                            }
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Öğün Göster", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = topMealState.showMeals,
                            onCheckedChange = { show ->
                                topMealVM.onEvent(MealUiEvent.ToggleVisibility(show))
                                bottomMealVM.onEvent(MealUiEvent.ToggleVisibility(show))
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) { Text("Kapat") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparisonChart(
    glucoseReadings: List<GlucoseReading>,
    insulinDoses: List<InsulinDose>,
    meals: List<Meal>,
    showInsulin: Boolean,
    showMeals: Boolean,
    scaleHours: Int,
    oldestDataLimitMillis: Long?,
    endTimeMillis: Long,
    onEndTimeChange: (Long) -> Unit,
    onFetchData: (start: Long, end: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val visibleDurationMillis = scaleHours * 3600000L
    var isDragging by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var selectedInsulinDose by remember { mutableStateOf<InsulinDose?>(null) }
    var selectedMeal by remember { mutableStateOf<Meal?>(null) }

    val coroutineScope = rememberCoroutineScope()
    var debounceJob by remember { mutableStateOf<Job?>(null) }

    val currentEndTime by rememberUpdatedState(endTimeMillis)

    LaunchedEffect(endTimeMillis, visibleDurationMillis) {
        debounceJob?.cancel()
        debounceJob = coroutineScope.launch {
            delay(300)
            val viewStartTimeMillis = endTimeMillis - visibleDurationMillis
            onFetchData(viewStartTimeMillis, endTimeMillis)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(visibleDurationMillis, oldestDataLimitMillis) {
                    detectHorizontalDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val millisPerPixel = visibleDurationMillis / size.width
                            var newEndTime = currentEndTime - (dragAmount * millisPerPixel).toLong()

                            val limit = oldestDataLimitMillis ?: 0L
                            val minAllowedEndTime = limit + visibleDurationMillis

                            if (newEndTime < minAllowedEndTime && limit > 0L) {
                                newEndTime = minAllowedEndTime
                            }
                            if (newEndTime > System.currentTimeMillis()) {
                                newEndTime = System.currentTimeMillis()
                            }
                            onEndTimeChange(newEndTime)
                        }
                    )
                }
                .pointerInput(insulinDoses, meals, showInsulin, showMeals) {
                    detectTapGestures { tapOffset ->
                        val width = size.width
                        val height = size.height
                        val maxGlucose = 400f

                        if (showInsulin) {
                            insulinDoses.forEach { dose ->
                                val x = width - ((currentEndTime - dose.timestampMillis).toFloat() / visibleDurationMillis) * width
                                val isUnknown = dose.glucoseVal == null || dose.glucoseVal == 0f
                                val yGlucoseValue = if (isUnknown) 0f else dose.glucoseVal!!
                                val rawY = height - (yGlucoseValue / maxGlucose) * height
                                val y = rawY.coerceIn(0f, height.toFloat())

                                val dx = tapOffset.x - x
                                val dy = tapOffset.y - y
                                if (dx * dx + dy * dy <= 2500f) {
                                    selectedInsulinDose = dose
                                    return@detectTapGestures
                                }
                            }
                        }

                        if (showMeals) {
                            meals.forEach { meal ->
                                val x = width - ((currentEndTime - meal.timestampMillis).toFloat() / visibleDurationMillis) * width
                                val isUnknown = meal.glucoseValue == null || meal.glucoseValue == 0f
                                val yGlucoseValue = if (isUnknown) 0f else meal.glucoseValue!!
                                val rawY = height - (yGlucoseValue / maxGlucose) * height
                                val y = rawY.coerceIn(0f, height.toFloat())

                                val dx = tapOffset.x - x
                                val dy = tapOffset.y - y
                                if (dx * dx + dy * dy <= 2500f) {
                                    selectedMeal = meal
                                    return@detectTapGestures
                                }
                            }
                        }
                    }
                }
        ) {
            val viewStartTimeMillis = endTimeMillis - visibleDurationMillis
            val width = size.width
            val height = size.height
            val maxGlucose = 400f

            val gridTextPaint = Paint().apply {
                color = android.graphics.Color.LTGRAY
                textSize = 28f
                textAlign = Paint.Align.RIGHT
            }

            listOf(55, 100, 200, 300, 400).forEach { lineValue ->
                val y = height - (lineValue.toFloat() / maxGlucose) * height
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.4f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 2f
                )
                drawContext.canvas.nativeCanvas.drawText(lineValue.toString(), width - 10f, y - 8f, gridTextPaint)
            }

            val stepMillis = when (scaleHours) {
                1 -> 1800000L
                3 -> 3600000L
                6 -> 7200000L
                12 -> 14400000L
                24 -> 21600000L
                else -> 7200000L
            }

            val zone = ZoneId.systemDefault()
            val localEnd = Instant.ofEpochMilli(endTimeMillis).atZone(zone)
            val nextHourMillis = localEnd.withMinute(0).withSecond(0).withNano(0).plusHours(1).toInstant().toEpochMilli()

            val textPaint = Paint().apply {
                color = android.graphics.Color.LTGRAY
                textSize = 32f
                textAlign = Paint.Align.CENTER
            }

            val formatter = DateTimeFormatter.ofPattern("HH:mm").withZone(zone)
            var tickTime = nextHourMillis - stepMillis
            while (tickTime >= viewStartTimeMillis) {
                val tickX = width - ((endTimeMillis - tickTime).toFloat() / visibleDurationMillis) * width
                if (tickX in 0f..(width - 80f)) {
                    drawLine(color = Color.LightGray.copy(alpha = 0.6f), start = Offset(tickX, height - 40f), end = Offset(tickX, height - 20f), strokeWidth = 3f)
                    drawContext.canvas.nativeCanvas.drawText(formatter.format(Instant.ofEpochMilli(tickTime)), tickX, height - 5f, textPaint)
                }
                tickTime -= stepMillis
            }

            val dateFormatter = DateTimeFormatter.ofPattern("dd MMM").withZone(zone)
            var dayTick = Instant.ofEpochMilli(viewStartTimeMillis).atZone(zone).toLocalDate().plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

            while (dayTick <= endTimeMillis) {
                val dayX = width - ((endTimeMillis - dayTick).toFloat() / visibleDurationMillis) * width
                if (dayX in 0f..width) {
                    drawLine(color = Color.LightGray.copy(alpha = 0.9f), start = Offset(dayX, height - 55f), end = Offset(dayX, height - 10f), strokeWidth = 5f)
                    textPaint.textAlign = Paint.Align.CENTER
                    drawContext.canvas.nativeCanvas.drawText(dateFormatter.format(Instant.ofEpochMilli(dayTick)), dayX, height - 62f, textPaint)
                }
                dayTick = Instant.ofEpochMilli(dayTick).atZone(zone).toLocalDate().plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            }

            glucoseReadings.filter { it.timestampMillis in viewStartTimeMillis..endTimeMillis }.forEach { reading ->
                val x = width - ((endTimeMillis - reading.timestampMillis).toFloat() / visibleDurationMillis) * width
                val y = height - (reading.value / maxGlucose) * height
                val pointColor = when (reading.status.uppercase()) {
                    "NORMAL" -> Color.Green
                    "WARNING" -> Color.Yellow
                    "CRITICAL" -> Color.Red
                    else -> Color.Blue
                }
                drawCircle(color = pointColor, radius = 8f, center = Offset(x, y))
            }

            if (showInsulin) {
                val insulinPaint = Paint().apply {
                    color = android.graphics.Color.parseColor("#800080")
                    textSize = 50f
                    textAlign = Paint.Align.CENTER
                    isFakeBoldText = true
                }
                insulinDoses.filter { it.timestampMillis in viewStartTimeMillis..endTimeMillis }.forEach { dose ->
                    val x = width - ((endTimeMillis - dose.timestampMillis).toFloat() / visibleDurationMillis) * width
                    val isUnknown = dose.glucoseVal == null || dose.glucoseVal == 0f

                    if (isUnknown) {
                        drawContext.canvas.nativeCanvas.drawText("?", x, height - 10f, insulinPaint)
                    } else {
                        val y = (height - (dose.glucoseVal!! / maxGlucose) * height).coerceIn(0f, height)
                        drawCircle(color = Color(0xFF800080), radius = 12f, center = Offset(x, y))
                    }
                }
            }

            if (showMeals) {
                val mealPaint = Paint().apply {
                    color = android.graphics.Color.parseColor("#FF9800")
                    textSize = 50f
                    textAlign = Paint.Align.CENTER
                    isFakeBoldText = true
                }
                meals.filter { it.timestampMillis in viewStartTimeMillis..endTimeMillis }.forEach { meal ->
                    val x = width - ((endTimeMillis - meal.timestampMillis).toFloat() / visibleDurationMillis) * width
                    val isUnknown = meal.glucoseValue == null || meal.glucoseValue == 0f

                    if (isUnknown) {
                        drawContext.canvas.nativeCanvas.drawText("?", x, height - 10f, mealPaint)
                    } else {
                        val y = (height - (meal.glucoseValue!! / maxGlucose) * height).coerceIn(0f, height)
                        drawCircle(color = Color(0xFFFF9800), radius = 12f, center = Offset(x, y))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📅 Tarih Seç (Başlangıç: 00:00)")
        }

        if (showDatePicker) {
            val zone = ZoneId.systemDefault()
            val oldestDate = oldestDataLimitMillis?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
            val selectableDates = remember(oldestDate) {
                object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        val date = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()
                        return oldestDate?.let { !date.isBefore(it) && !date.isAfter(LocalDate.now(zone)) } ?: false
                    }
                }
            }
            val datePickerState = rememberDatePickerState(selectableDates = selectableDates)
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                            val dayStartMillis = selectedDate.atStartOfDay(zone).toInstant().toEpochMilli()
                            onEndTimeChange(dayStartMillis + visibleDurationMillis)
                        }
                        showDatePicker = false
                    }, enabled = datePickerState.selectedDateMillis != null) { Text("Git") }
                },
                dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("İptal") } }
            ) { DatePicker(state = datePickerState) }
        }

        selectedInsulinDose?.let { dose ->
            val isUnknown = dose.glucoseVal == null || dose.glucoseVal == 0f
            AlertDialog(
                onDismissRequest = { selectedInsulinDose = null },
                title = { Text("İnsülin Detayları") },
                text = {
                    Column {
                        Text("Zaman: ${dose.timestamp}")
                        Text("Doz: ${dose.dose} Ünite")
                        Text("Tip: ${dose.insulinType}")
                        Spacer(modifier = Modifier.height(8.dp))
                        if (isUnknown) Text("Glukoz değeri bilinmiyor", color = Color.Red)
                        else Text("Glukoz Değeri: ${dose.glucoseVal}", color = Color(0xFF800080))
                    }
                },
                confirmButton = { TextButton(onClick = { selectedInsulinDose = null }) { Text("Kapat") } }
            )
        }

        selectedMeal?.let { meal ->
            val isUnknown = meal.glucoseValue == null || meal.glucoseValue == 0f
            AlertDialog(
                onDismissRequest = { selectedMeal = null },
                title = { Text("Öğün Detayları") },
                text = {
                    Column {
                        Text("Zaman: ${meal.timestamp}")
                        Text("Açıklama: ${meal.desc}")
                        Spacer(modifier = Modifier.height(8.dp))
                        if (isUnknown) Text("Glukoz değeri bilinmiyor", color = Color.Red)
                        else Text("Glukoz Değeri: ${meal.glucoseValue}", color = Color(0xFFFF9800))
                    }
                },
                confirmButton = { TextButton(onClick = { selectedMeal = null }) { Text("Kapat") } }
            )
        }
    }
}