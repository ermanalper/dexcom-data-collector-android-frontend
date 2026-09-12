package com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.InsulinDose
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.repository.InsulinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InsulinDoseViewModel @Inject constructor(
    private val repository: InsulinRepository
) : ViewModel() {

    private val _state = MutableStateFlow(InsulinUiState())
    val state = _state.asStateFlow()

    private val fetchedTimeRanges = mutableListOf<LongRange>()
    private val maxRetentionMillis = 15L * 24L * 3600000L // 15 gün

    fun onEvent(event: InsulinUiEvent) {
        when (event) {
            is InsulinUiEvent.FetchDoses -> checkAndFetchMissingData(event.startMillis, event.endMillis)
            is InsulinUiEvent.ToggleVisibility -> _state.update { it.copy(showInsulin = event.show) }
        }
    }

    private fun checkAndFetchMissingData(viewStartMillis: Long, viewEndMillis: Long) {
        val neededStart = viewStartMillis
        val neededEnd = minOf(viewEndMillis, System.currentTimeMillis())

        val isCovered = fetchedTimeRanges.any { it.contains(neededStart) && it.contains(neededEnd) }

        if (isCovered) {
            pruneRam(viewStartMillis, viewEndMillis)
            return
        }

        val chunkMillis = 7L * 24L * 3600000L
        val fetchStart = neededStart - chunkMillis
        val fetchEnd = minOf(neededEnd + chunkMillis, System.currentTimeMillis())

        fetchedTimeRanges.add(fetchStart..fetchEnd)

        viewModelScope.launch {
            repository.getInsulinHistory(fetchStart, fetchEnd).onSuccess { newData ->
                _state.update { currentState ->
                    val combined = (currentState.doses + newData)
                        .distinctBy { it.timestampMillis }
                        .sortedBy { it.timestampMillis }

                    currentState.copy(
                        doses = applyRamPruning(combined, viewStartMillis, viewEndMillis)
                    )
                }
                cleanupFetchedRanges(viewStartMillis, viewEndMillis)
            }
        }
    }

    private fun pruneRam(viewStartMillis: Long, viewEndMillis: Long) {
        _state.update { it.copy(doses = applyRamPruning(it.doses, viewStartMillis, viewEndMillis)) }
    }

    private fun applyRamPruning(doses: List<InsulinDose>, viewStart: Long, viewEnd: Long): List<InsulinDose> {
        val keepStart = viewStart - maxRetentionMillis
        val keepEnd = viewEnd + maxRetentionMillis
        return doses.filter { it.timestampMillis in keepStart..keepEnd }
    }

    private fun cleanupFetchedRanges(viewStart: Long, viewEnd: Long) {
        val keepStart = viewStart - maxRetentionMillis
        val keepEnd = viewEnd + maxRetentionMillis
        fetchedTimeRanges.removeAll { it.last < keepStart || it.first > keepEnd }
    }
}