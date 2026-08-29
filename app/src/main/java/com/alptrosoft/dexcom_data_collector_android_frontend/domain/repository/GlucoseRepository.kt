package com.alptrosoft.dexcom_data_collector_android_frontend.domain.repository

import com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.dto.GlucoseDto

interface GlucoseRepository {
    suspend fun fetchLatestGlucose(): Result<GlucoseDto>
    suspend fun fetchGlucoseHistory(limit: Int = 10): Result<List<GlucoseDto>>
}