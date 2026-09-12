package com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels

import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.InsulinDose

data class InsulinUiState(
    val doses: List<InsulinDose> = emptyList(),
    val showInsulin: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class InsulinUiEvent {
    data class FetchDoses(val startMillis: Long, val endMillis: Long) : InsulinUiEvent()
    data class ToggleVisibility(val show: Boolean) : InsulinUiEvent()
}