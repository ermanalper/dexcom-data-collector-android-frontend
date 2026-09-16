// app/src/main/java/com/.../presentation/ui/components/AlarmsScreen.kt
package com.alptrosoft.dexcom_data_collector_android_frontend.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.Alarm
import com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels.AlarmUiEvent
import com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels.AlarmUiState

@Composable
fun AlarmsScreen(
    state: AlarmUiState,
    onEvent: (AlarmUiEvent) -> Unit
) {
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (state.alarms.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Aktif alarm bulunmamaktadır.", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
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

@Composable
fun AlarmCard(alarm: Alarm, onAcknowledge: () -> Unit) {
    // Seviyeye göre kart rengi ayarlayabilirsin (Örn: CRITICAL ise kırmızımtırak)
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = "Alarm Icon",
                    tint = if (alarm.level == 3) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = alarm.message, style = MaterialTheme.typography.titleMedium)
                    // İstersen burada timestamp string'ini de daha şık bir saate formatlayabilirsin
                }
            }
            Button(
                onClick = onAcknowledge,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Kapat")
            }
        }
    }
}