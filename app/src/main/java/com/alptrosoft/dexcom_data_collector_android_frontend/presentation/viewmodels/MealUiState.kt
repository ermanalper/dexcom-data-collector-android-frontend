package com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels

import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.Meal
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.MealShortcut

data class MealUiState(
    val meals: List<Meal> = emptyList(),
    val shortcuts: List<MealShortcut> = emptyList(),
    val showMeals: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class MealUiEvent {
    data class FetchMeals(val startMillis: Long, val endMillis: Long) : MealUiEvent()
    data class ToggleVisibility(val show: Boolean) : MealUiEvent()
    data class PostMeal(val desc: String, val timestampMillis: Long) : MealUiEvent()
    data class PostMealShortcut(val title: String, val desc: String) : MealUiEvent()
}

