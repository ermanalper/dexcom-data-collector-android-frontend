package com.alptrosoft.dexcom_data_collector_android_frontend.data.repository

import com.alptrosoft.dexcom_data_collector_android_frontend.data.mapper.toDomainModel
import com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.InsulinApi
import com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.dto.PostInsulinDoseDto
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.InsulinDose
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.InsulinType
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.repository.InsulinRepository
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class InsulinRepositoryImpl @Inject constructor(
    private val api: InsulinApi
) : InsulinRepository {


    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        .withZone(ZoneId.of("UTC"))

    override suspend fun getInsulinHistory(
        startMillis: Long,
        endMillis: Long
    ): Result<List<InsulinDose>> {
        return try {
            val startStr = formatter.format(Instant.ofEpochMilli(startMillis))
            val endStr = formatter.format(Instant.ofEpochMilli(endMillis))

            val response = api.getInsulinHistory(startTime = startStr, endTime = endStr)
            Result.success(response.map { it.toDomainModel() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun getInsulinTypes(): Result<List<InsulinType>> {
        return try {
            val response = api.getInsulinTypes()
            Result.success(response.map { InsulinType(it.id, it.type) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun postInsulinDose(typeId: Int, dose: Float, timestampMillis: Long): Result<Unit> {
        return try {
            val timeString = Instant.ofEpochMilli(timestampMillis).toString()
            val request = PostInsulinDoseDto(insulinId = typeId, dose = dose, timestamp = timeString)
            api.postInsulinDose(request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}