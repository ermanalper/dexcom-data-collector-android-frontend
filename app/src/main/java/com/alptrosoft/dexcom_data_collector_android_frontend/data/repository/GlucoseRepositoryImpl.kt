package com.alptrosoft.dexcom_data_collector_android_frontend.data.repository
import javax.inject.Inject
import com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.GlucoseApi
import com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.dto.GlucoseDto
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.repository.GlucoseRepository

class GlucoseRepositoryImpl @Inject constructor(
    private val api: GlucoseApi
) : GlucoseRepository {

    override suspend fun fetchLatestGlucose(): Result<GlucoseDto> {
        return try {
            val response = api.getLatestGlucose()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchGlucoseHistory(limit: Int): Result<List<GlucoseDto>> {
        return try {
            val response = api.getGlucoseHistory(limit)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}