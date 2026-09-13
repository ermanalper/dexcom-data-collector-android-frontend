package com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.Meal
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.repository.MealRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MealViewModel @Inject constructor(
    private val repository: MealRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MealUiState())
    val state = _state.asStateFlow()

    private val fetchedTimeRanges = mutableListOf<LongRange>()
    private val maxRetentionMillis = 15L * 24L * 3600000L

    private var lastViewStart: Long = 0L
    private var lastViewEnd: Long = 0L

    fun onEvent(event: MealUiEvent) {
        when (event) {
            is MealUiEvent.FetchMeals -> checkAndFetchMissingData(event.startMillis, event.endMillis)
            is MealUiEvent.ToggleVisibility -> _state.update { it.copy(showMeals = event.show) }
            is MealUiEvent.PostMeal -> postMeal(event.desc, event.timestampMillis)
        }
    }

    private fun postMeal(desc: String, timestampMillis: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.postMeal(desc, timestampMillis).onSuccess {
                _state.update { it.copy(isLoading = false) }
                fetchedTimeRanges.clear()
                if (lastViewStart != 0L && lastViewEnd != 0L) {
                    checkAndFetchMissingData(lastViewStart, lastViewEnd)
                }
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }

    private fun checkAndFetchMissingData(viewStartMillis: Long, viewEndMillis: Long) {
        lastViewStart = viewStartMillis
        lastViewEnd = viewEndMillis

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
            repository.getMealHistory(fetchStart, fetchEnd).onSuccess { newData ->
                _state.update { currentState ->
                    val combined = (currentState.meals + newData)
                        .distinctBy { it.timestampMillis }
                        .sortedBy { it.timestampMillis }

                    currentState.copy(meals = applyRamPruning(combined, viewStartMillis, viewEndMillis))
                }
                cleanupFetchedRanges(viewStartMillis, viewEndMillis)
            }
        }
    }

    private fun pruneRam(viewStartMillis: Long, viewEndMillis: Long) {
        _state.update { it.copy(meals = applyRamPruning(it.meals, viewStartMillis, viewEndMillis)) }
    }

    private fun applyRamPruning(meals: List<Meal>, viewStart: Long, viewEnd: Long): List<Meal> {
        val keepStart = viewStart - maxRetentionMillis
        val keepEnd = viewEnd + maxRetentionMillis
        return meals.filter { it.timestampMillis in keepStart..keepEnd }
    }

    private fun cleanupFetchedRanges(viewStart: Long, viewEnd: Long) {
        val keepStart = viewStart - maxRetentionMillis
        val keepEnd = viewEnd + maxRetentionMillis
        fetchedTimeRanges.removeAll { it.last < keepStart || it.first > keepEnd }
    }
}