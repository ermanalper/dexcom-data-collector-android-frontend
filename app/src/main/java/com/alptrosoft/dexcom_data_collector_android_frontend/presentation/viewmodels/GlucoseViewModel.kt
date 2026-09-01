package com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.GlucoseReading
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.repository.GlucoseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class GlucoseViewModel @Inject constructor(
    private val repository: GlucoseRepository
) : ViewModel() {

    private val _state = MutableStateFlow(GlucoseUiState())
    val state = _state.asStateFlow()

    private var earliestFetchedBound: Long = System.currentTimeMillis()

    init {
        viewModelScope.launch {
            fetchOldestDataLimit() // get the first data ever recorded
            fetchLatestGlucose()
        }
    }

    fun onEvent(event: GlucoseUiEvent) {
        when (event) {
            is GlucoseUiEvent.OnVisibleRangeChanged -> checkAndFetchMissingData(event.startMillis)
            is GlucoseUiEvent.OnChartDragged -> handleDrag(event.newEndTimeMillis)
            is GlucoseUiEvent.SnapToLatest -> snapToLatest()
            is GlucoseUiEvent.ChangeScale -> _state.update { it.copy(scaleHours = event.newScaleHours) }
            is GlucoseUiEvent.RealtimeDataReceived -> handleNewRealtimeData(event.newReading)
        }
    }

    private suspend fun fetchOldestDataLimit() {
        repository.fetchFirstDataDate().onSuccess { timestamp ->
            _state.update { it.copy(oldestDataLimitMillis = timestamp) }
        }
    }

    private fun checkAndFetchMissingData(viewStartMillis: Long) {
        val state = _state.value
        val fetchWindow = state.scaleHours * 3600000L * 2
        val neededStart = viewStartMillis - fetchWindow
        val limit = state.oldestDataLimitMillis ?: 0L

        if (neededStart < earliestFetchedBound && earliestFetchedBound > limit) {

            // fetch in 7-days big blocks
            val minimumChunkMillis = 7L * 24L * 3600000L

            val theoreticalStart = earliestFetchedBound - minimumChunkMillis

            // if needed start is more than 7 days ago, start from there, else 7 days (minimum)
            val targetStart = minOf(neededStart, theoreticalStart)

            val fetchStart = maxOf(targetStart, limit)
            val fetchEnd = earliestFetchedBound

            earliestFetchedBound = fetchStart

            viewModelScope.launch {
                repository.fetchGlucoseHistory(fetchStart, fetchEnd).onSuccess { newData ->
                    _state.update { currentState ->
                        val combined = (currentState.readings + newData)
                            .distinctBy { it.timestampMillis }
                            .sortedBy { it.timestampMillis }
                        currentState.copy(readings = combined)
                    }
                }
            }
        }
    }
    private fun handleDrag(newEndTimeMillis: Long) {
        val currentState = _state.value
        val now = System.currentTimeMillis()
        val snapThreshold = 2 * 60 * 1000L // 2 dakika

        val limit = currentState.oldestDataLimitMillis ?: 0L
        val visibleDuration = currentState.scaleHours * 3600000L
        val minAllowedEndTime = limit + visibleDuration

        var boundedEndTime = newEndTimeMillis
        if (boundedEndTime < minAllowedEndTime && limit > 0L) {
            boundedEndTime = minAllowedEndTime
        }

        if (boundedEndTime >= now - snapThreshold) {
            snapToLatest()
        } else {
            _state.update { it.copy(viewEndTimeMillis = boundedEndTime, isAutoScroll = false) }
        }
    }

    private fun snapToLatest() {
        _state.update { it.copy(viewEndTimeMillis = System.currentTimeMillis(), isAutoScroll = true) }
    }

    private fun handleNewRealtimeData(reading: GlucoseReading) {
        _state.update { currentState ->
            val updatedReadings = (currentState.readings + reading)
                .distinctBy { it.timestampMillis }
                .sortedBy { it.timestampMillis }

            val nextEndTime = if (currentState.isAutoScroll) reading.timestampMillis else currentState.viewEndTimeMillis

            currentState.copy(
                readings = updatedReadings,
                latestReading = reading,
                viewEndTimeMillis = nextEndTime
            )
        }
    }

    private suspend fun fetchLatestGlucose() {
        repository.fetchLatestGlucose().onSuccess { newestReading ->
            onEvent(GlucoseUiEvent.RealtimeDataReceived(newestReading))
        }
    }
}