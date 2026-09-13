package com.alptrosoft.dexcom_data_collector_android_frontend.domain.repository

import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.Meal
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.MealShortcut

interface MealRepository {
    suspend fun getMealHistory(startMillis: Long, endMillis: Long): Result<List<Meal>>
    suspend fun postMeal(desc: String, timestampMillis: Long): Result<Unit>
    suspend fun postMealShortcut(title: String, desc: String): Result<Unit>
    suspend fun getMealShortcuts(): Result<List<MealShortcut>>
}