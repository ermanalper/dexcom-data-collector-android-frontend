package com.alptrosoft.dexcom_data_collector_android_frontend.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels.ClientUiEvent
import com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels.ClientUiState

@Composable
fun ClientsScreen(
    state: ClientUiState,
    onEvent: (ClientUiEvent) -> Unit
) {
    LaunchedEffect(Unit) {
        onEvent(ClientUiEvent.FetchClients)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(ClientUiEvent.FetchClients) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Cihazları Yenile"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.clients.isEmpty()) {
                Text(
                    text = "Aktif cihaz bulunmamaktadır.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.clients) { clientName ->
                        ClientCard(clientName = clientName, onTest = {
                            onEvent(ClientUiEvent.TestClient(clientName))
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun ClientCard(clientName: String, onTest: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Devices,
                    contentDescription = "Device Icon",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = clientName, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onTest,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Test et")
            }
        }
    }
}