package com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val repository: AlarmRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AlarmUiState())
    val state = _state.asStateFlow()

    init {
        onEvent(AlarmUiEvent.FetchAlarms)
    }

    fun onEvent(event: AlarmUiEvent) {
        when (event) {
            is AlarmUiEvent.FetchAlarms -> fetchAlarms()
            is AlarmUiEvent.AcknowledgeAlarm -> acknowledgeAlarm(event.id)
            is AlarmUiEvent.AcknowledgeAllAlarms -> acknowledgeAllAlarms()
        }
    }

    private fun fetchAlarms() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getActiveAlarms().onSuccess { alarms ->
                _state.update { it.copy(alarms = alarms, isLoading = false, error = null) }
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }

    private fun acknowledgeAlarm(id: String) {
        viewModelScope.launch {
            repository.acknowledgeAlarm(id).onSuccess {
                fetchAlarms()
            }
        }
    }
    private fun acknowledgeAllAlarms() {
        viewModelScope.launch {
            repository.acknowledgeAllAlarms().onSuccess {
                fetchAlarms()
            }
        }
    }
}