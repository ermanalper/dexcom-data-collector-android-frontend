package com.alptrosoft.dexcom_data_collector_android_frontend.data.remote

import com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.dto.FirstDataDateResponse
import com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.dto.GlucoseDto
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response
interface GlucoseApi {
    @GET("api/v1/glucose/latest")
    suspend fun getLatestGlucose(): GlucoseDto

    @GET("api/v1/glucose/history")
    suspend fun getGlucoseHistoryDataCountBased(
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0
    ): List<GlucoseDto>

    @GET("api/v1/glucose/history/by-time")
    suspend fun getGlucoseHistory(
        @Query("start_time") startTime: String,
        @Query("end_time") endTime: String? = null
    ): List<GlucoseDto>

    @GET("/api/v1/glucose/first-data-date")
    suspend fun getFirstDataDate(): Response<FirstDataDateResponse>
}

