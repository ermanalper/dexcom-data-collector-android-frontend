package com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels

data class ClientUiState(
    val clients: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class ClientUiEvent {
    object FetchClients : ClientUiEvent()
    data class TestClient(val clientName: String) : ClientUiEvent()
}