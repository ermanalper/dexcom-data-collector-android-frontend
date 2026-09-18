package com.alptrosoft.dexcom_data_collector_android_frontend.data.repository

import com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.AlarmApi
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.Alarm
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.repository.AlarmRepository
import jakarta.inject.Inject
import java.time.Instant

class AlarmRepositoryImpl @Inject constructor(
    private val api: AlarmApi
) : AlarmRepository {

    override suspend fun getActiveAlarms(): Result<List<Alarm>> {
        return try {
            val response = api.getActiveAlarms()
            val mapped = response.map { dto ->
                val millis = Instant.parse(dto.timestamp).toEpochMilli()
                Alarm(dto.id, dto.level, dto.message, dto.timestamp, millis)
            }
            Result.success(mapped)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun acknowledgeAlarm(id: String): Result<Unit> {
        return try {
            api.acknowledgeAlarm(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun acknowledgeAllAlarms(): Result<Unit> {
        return try {
            api.acknowledgeAllAlarms()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}