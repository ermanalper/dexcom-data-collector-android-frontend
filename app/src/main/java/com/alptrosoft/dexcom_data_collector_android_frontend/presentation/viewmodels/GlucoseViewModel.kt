package com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.GlucoseReading
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.repository.GlucoseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GlucoseViewModel @Inject constructor(
    private val repository: GlucoseRepository
) : ViewModel() {

    private val _readings = MutableStateFlow<List<GlucoseReading>>(emptyList())
    val readings = _readings.asStateFlow();
    private val fetchedTimeRanges = mutableListOf<LongRange>()


    fun loadDataForTimeRange(startTimeMillis: Long, endTimeMillis: Long) {
        val isAlreadyFetched = fetchedTimeRanges.any {
            it.contains(startTimeMillis) && it.contains(endTimeMillis)
        }
        if (isAlreadyFetched) return
        viewModelScope.launch {
            repository.fetchGlucoseHistory(startTimeMillis, endTimeMillis).onSuccess { newData ->
                Log.d("GLUCOSE_DATA", "Start: $startTimeMillis, End: $endTimeMillis Data: $newData")
                fetchedTimeRanges.add(startTimeMillis..endTimeMillis)
                val combined = (_readings.value + newData)
                    .distinctBy { it.timestampMillis }
                    .sortedBy { it.timestampMillis }

                _readings.value = combined
            }.onFailure { error ->
                Log.e("GLUCOSE_DATA", "Glucose history error ", error)
            }
        }
    }

}