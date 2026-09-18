// app/src/main/java/com/.../presentation/ui/components/AlarmsScreen.kt
package com.alptrosoft.dexcom_data_collector_android_frontend.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.Alarm
import com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels.AlarmUiEvent
import com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels.AlarmUiState

@Composable
fun AlarmsScreen(
    state: AlarmUiState,
    onEvent: (AlarmUiEvent) -> Unit
) {
    // 1. Ekran ilk açıldığında listeyi getir
    LaunchedEffect(Unit) {
        onEvent(AlarmUiEvent.FetchAlarms)
    }

    // Scaffold kullanarak FAB (Floating Action Button) yerleşimini sağlıyoruz
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(AlarmUiEvent.FetchAlarms) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                // Yenileme ikonu
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Alarmları Yenile"
                )
            }
        }
    ) { paddingValues ->
        // Scaffold'un padding değerlerini içeriğe uyguluyoruz (FAB'ın listenin üstüne binmemesi için)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                // Yükleniyor animasyonu ekranın ortasında gösterilecek
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.alarms.isEmpty()) {
                // Liste boşsa
                Text(
                    text = "Aktif alarm bulunmamaktadır.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                // Liste doluysa Butonu ve Listeyi göster
                Column(modifier = Modifier.fillMaxSize()) {

                    // Tüm Alarmları Kapat Butonu
                    Button(
                        onClick = { onEvent(AlarmUiEvent.AcknowledgeAllAlarms) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Tüm Alarmları Kapat")
                    }

                    // Alarmların Listesi
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.alarms) { alarm ->
                            AlarmCard(alarm = alarm, onAcknowledge = {
                                onEvent(AlarmUiEvent.AcknowledgeAlarm(alarm.id))
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlarmCard(alarm: Alarm, onAcknowledge: () -> Unit) {
    val cardColor = if (alarm.level == 3) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f) // Yazının butonu sıkıştırmaması için
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = "Alarm Icon",
                    tint = if (alarm.level == 3) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = alarm.message, style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onAcknowledge,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Kapat")
            }
        }
    }
}