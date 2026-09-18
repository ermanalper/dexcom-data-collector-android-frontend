package com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.repository.ClientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientViewModel @Inject constructor(
    private val repository: ClientRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ClientUiState())
    val state = _state.asStateFlow()

    init {
        onEvent(ClientUiEvent.FetchClients)
    }

    fun onEvent(event: ClientUiEvent) {
        when (event) {
            is ClientUiEvent.FetchClients -> fetchClients()
            is ClientUiEvent.TestClient -> testClient(event.clientName)
        }
    }

    private fun fetchClients() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getActiveClients().onSuccess { clients ->
                _state.update { it.copy(clients = clients, isLoading = false, error = null) }
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }

    private fun testClient(clientName: String) {
        viewModelScope.launch {
            repository.testClient(clientName).onSuccess {
                // Test başarılı, isterseniz state üzerinde güncellemeler yapabilirsiniz
            }.onFailure { error ->
                _state.update { it.copy(error = error.message) }
            }
        }
    }
}