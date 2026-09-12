package com.alptrosoft.dexcom_data_collector_android_frontend.data.remote

import com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.dto.InsulinDoseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface InsulinApi {
    @GET("/api/v1/insulin/history/by-time")
    suspend fun getInsulinHistory(
        @Query("start_time") startTime: String,
        @Query("end_time") endTime: String? = null
    ): List<InsulinDoseDto>
}