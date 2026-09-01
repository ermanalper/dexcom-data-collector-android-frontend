package com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels

import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.GlucoseReading

data class GlucoseUiState(
    val readings: List<GlucoseReading> = emptyList(),
    val latestReading: GlucoseReading? = null,
    val viewEndTimeMillis: Long = System.currentTimeMillis(),
    val isAutoScroll: Boolean = true,
    val scaleHours: Int = 6,
    val oldestDataLimitMillis: Long? = null // YENİ: Backend'den gelecek en eski kayıt tarihi
)

sealed class GlucoseUiEvent {
    // YENİ: UI sadece baktığı pencereyi raporlar
    data class OnVisibleRangeChanged(val startMillis: Long, val endMillis: Long) : GlucoseUiEvent()
    data class OnChartDragged(val newEndTimeMillis: Long) : GlucoseUiEvent()
    data class ChangeScale(val newScaleHours: Int) : GlucoseUiEvent()
    object SnapToLatest : GlucoseUiEvent()
    data class RealtimeDataReceived(val newReading: GlucoseReading) : GlucoseUiEvent()
}