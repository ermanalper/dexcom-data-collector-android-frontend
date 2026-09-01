package com.alptrosoft.dexcom_data_collector_android_frontend.domain.repository

import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.GlucoseReading

interface GlucoseRepository {
    suspend fun fetchLatestGlucose(): Result<GlucoseReading>
    suspend fun fetchGlucoseHistoryDataCountBased(limit: Int = 10, offset: Int=0): Result<List<GlucoseReading>>
    suspend fun fetchGlucoseHistory(startTimeMillis: Long, endTimeMillis: Long? = null): Result<List<GlucoseReading>>
    suspend fun fetchFirstDataDate(): Result<Long>
}