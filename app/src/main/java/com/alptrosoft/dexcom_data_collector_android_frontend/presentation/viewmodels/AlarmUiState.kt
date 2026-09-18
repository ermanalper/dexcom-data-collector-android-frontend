package com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels

import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.Alarm

data class AlarmUiState(
    val alarms: List<Alarm> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class AlarmUiEvent {
    object FetchAlarms : AlarmUiEvent()
    data class AcknowledgeAlarm(val id: String) : AlarmUiEvent()
    object AcknowledgeAllAlarms : AlarmUiEvent()
}