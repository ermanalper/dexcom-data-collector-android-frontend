package com.alptrosoft.dexcom_data_collector_android_frontend.domain.repository

import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.Meal

interface MealRepository {
    suspend fun getMealHistory(startMillis: Long, endMillis: Long): Result<List<Meal>>
    suspend fun postMeal(desc: String, timestampMillis: Long): Result<Unit>
}