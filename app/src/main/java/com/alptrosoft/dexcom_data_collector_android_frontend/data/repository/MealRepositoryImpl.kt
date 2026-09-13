package com.alptrosoft.dexcom_data_collector_android_frontend.data.repository

import com.alptrosoft.dexcom_data_collector_android_frontend.data.mapper.toDomainModel
import com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.MealApi
import com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.dto.PostMealDto
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.Meal
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.repository.MealRepository
import java.time.Instant
import javax.inject.Inject

class MealRepositoryImpl @Inject constructor(
    private val api: MealApi
) : MealRepository {

    override suspend fun getMealHistory(startMillis: Long, endMillis: Long): Result<List<Meal>> {
        return try {
            val startStr = Instant.ofEpochMilli(startMillis).toString()
            val endStr = Instant.ofEpochMilli(endMillis).toString()

            val response = api.getMealHistory(startTime = startStr, endTime = endStr)
            Result.success(response.map { it.toDomainModel() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun postMeal(desc: String, timestampMillis: Long): Result<Unit> {
        return try {
            val timeString = Instant.ofEpochMilli(timestampMillis).toString()
            val request = PostMealDto(desc = desc, timestamp = timeString)
            api.postMeal(request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}