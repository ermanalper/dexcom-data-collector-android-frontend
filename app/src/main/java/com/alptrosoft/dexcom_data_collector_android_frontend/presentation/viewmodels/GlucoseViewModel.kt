package com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.repository.GlucoseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class GlucoseViewModel @Inject constructor(
    private val repository: GlucoseRepository
) : ViewModel() {
    init { }

    public fun getLatestGlucose() {
        viewModelScope.launch {
            repository.fetchLatestGlucose().onSuccess { data ->
                Log.d("GLUCOSE_DATA", "Latest: $data")
            }. onFailure { error ->
                Log.e("GLUCOSE_DATA", "Latest glucose error ", error)
            }
        }
    }
    public fun getGlucoseHistory(n: Int) {
        viewModelScope.launch {
            repository.fetchGlucoseHistory(n).onSuccess { history ->
                Log.d("GLUCOSE_DATA", "Last $n data: $history")
            }.onFailure { error ->
                Log.e("GLUCOSE_DATA", "Glucose history error ", error)
            }
        }
    }
}