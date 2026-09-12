package com.alptrosoft.dexcom_data_collector_android_frontend.data.repository

import com.alptrosoft.dexcom_data_collector_android_frontend.data.mapper.toDomainModel
import com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.InsulinApi
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.InsulinDose
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
}