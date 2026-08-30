package com.alptrosoft.dexcom_data_collector_android_frontend.data.repository
import com.alptrosoft.dexcom_data_collector_android_frontend.data.mapper.toDomain
import javax.inject.Inject
import com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.GlucoseApi
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.GlucoseReading
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.repository.GlucoseRepository
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class GlucoseRepositoryImpl @Inject constructor(
    private val api: GlucoseApi
) : GlucoseRepository {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        .withZone(ZoneId.of("UTC"))

    override suspend fun fetchLatestGlucose(): Result<GlucoseReading> {
        return try {
            val response = api.getLatestGlucose()
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchGlucoseHistoryDataCountBased(limit: Int, offset: Int): Result<List<GlucoseReading>> {
        return try {
            val response = api.getGlucoseHistoryDataCountBased(limit, offset)
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchGlucoseHistory(startTimeMillis: Long, endTimeMillis: Long?): Result<List<GlucoseReading>> {
        return try {
            val startStr = Instant.ofEpochMilli(startTimeMillis).toString()
            val endStr = endTimeMillis?.let { Instant.ofEpochMilli(it).toString() }

            val response = api.getGlucoseHistory(startStr, endStr)
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            if (e is retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                android.util.Log.e("GLUCOSE_API_422", "Backend Validation Error: $errorBody")
            }
            Result.failure(e)
        }
    }
}