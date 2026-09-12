package com.alptrosoft.dexcom_data_collector_android_frontend.presentation.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.InsulinType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInsulinDialog(
    insulinTypes: List<InsulinType>,
    onDismiss: () -> Unit,
    onConfirm: (typeId: Int, dose: Float, timestampMillis: Long) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    val zone = ZoneId.systemDefault()
    var selectedDate by remember { mutableStateOf(LocalDate.now(zone)) }

    var selectedHour by remember { mutableIntStateOf(Instant.now().atZone(zone).hour) }
    var selectedMinute by remember { mutableIntStateOf(Instant.now().atZone(zone).minute) }

    var selectedDose by remember { mutableFloatStateOf(1.0f) }
    var selectedType by remember { mutableStateOf(insulinTypes.firstOrNull()) }

    var doseDropdownExpanded by remember { mutableStateOf(false) }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tarih (Yukarıda, küçük)
                val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
                Text(
                    text = selectedDate.format(dateFormatter),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { showDatePicker = true }
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Dikey Kaydırılabilir Saat / Dakika Seçici
                Row(
                    modifier = Modifier.height(120.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WheelPicker(
                        count = 24,
                        initialIndex = selectedHour,
                        onScrollFinished = { selectedHour = it }
                    )
                    Text(":", fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                    WheelPicker(
                        count = 60,
                        initialIndex = selectedMinute,
                        onScrollFinished = { selectedMinute = it }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Doz Seçimi
                ExposedDropdownMenuBox(
                    expanded = doseDropdownExpanded,
                    onExpandedChange = { doseDropdownExpanded = !doseDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = "$selectedDose Ünite",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Doz") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = doseDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = doseDropdownExpanded,
                        onDismissRequest = { doseDropdownExpanded = false }
                    ) {
                        val doses = generateSequence(0.5f) { it + 0.5f }.take(40).toList()
                        doses.forEach { dose ->
                            DropdownMenuItem(
                                text = { Text("$dose") },
                                onClick = {
                                    selectedDose = dose
                                    doseDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Marka Seçimi
                ExposedDropdownMenuBox(
                    expanded = typeDropdownExpanded,
                    onExpandedChange = { typeDropdownExpanded = !typeDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedType?.type ?: "Seçiniz",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("İnsülin Markası") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = typeDropdownExpanded,
                        onDismissRequest = { typeDropdownExpanded = false }
                    ) {
                        insulinTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.type) },
                                onClick = {
                                    selectedType = type
                                    typeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("İptal") }
                    Button(
                        onClick = {
                            selectedType?.let { type ->
                                val timestampMillis = selectedDate.atTime(selectedHour, selectedMinute)
                                    .atZone(zone)
                                    .toInstant()
                                    .toEpochMilli()
                                onConfirm(type.id, selectedDose, timestampMillis)
                            }
                        },
                        enabled = selectedType != null
                    ) {
                        Text("Kaydet")
                    }
                }
            }
        }
    }

    // Takvim Dialogun Üstüne Çıkar
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        }
                        showDatePicker = false
                    }
                ) { Text("Tamam") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("İptal") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPicker(
    count: Int,
    initialIndex: Int,
    onScrollFinished: (Int) -> Unit
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val centerItem = listState.firstVisibleItemIndex
            onScrollFinished(centerItem % count)
        }
    }

    LazyColumn(
        state = listState,
        flingBehavior = flingBehavior,
        modifier = Modifier.width(60.dp).height(120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Sonsuz döngü hissi vermek için büyük bir sayı
        items(count * 100) { index ->
            val value = index % count
            val isCenter = index == listState.firstVisibleItemIndex + 1

            Box(
                modifier = Modifier.height(40.dp).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = value.toString().padStart(2, '0'),
                    fontSize = if (isCenter) 28.sp else 20.sp,
                    fontWeight = if (isCenter) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCenter) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
        }
    }
}