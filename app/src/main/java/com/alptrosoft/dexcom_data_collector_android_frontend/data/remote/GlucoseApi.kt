package com.alptrosoft.dexcom_data_collector_android_frontend.data.remote

import com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.dto.GlucoseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface GlucoseApi {
    @GET("api/v1/glucose/latest")
    suspend fun getLatestGlucose(): GlucoseDto

    @GET("api/v1/glucose/history")
    suspend fun getGlucoseHistory(
        @Query("limit") limit: Int = 10
    ): List<GlucoseDto>
}

