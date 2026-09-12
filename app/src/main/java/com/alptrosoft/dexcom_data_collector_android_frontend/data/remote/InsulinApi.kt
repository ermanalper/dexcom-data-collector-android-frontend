package com.alptrosoft.dexcom_data_collector_android_frontend.data.remote

import com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.dto.InsulinDoseDto
import com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.dto.InsulinTypeDto
import com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.dto.PostInsulinDoseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface InsulinApi {
    @GET("/api/v1/insulin/history/by-time")
    suspend fun getInsulinHistory(
        @Query("start_time") startTime: String,
        @Query("end_time") endTime: String? = null
    ): List<InsulinDoseDto>

    @GET("/api/v1/insulin/insulin-types")
    suspend fun getInsulinTypes(): List<InsulinTypeDto>

    @POST("/api/v1/insulin/enter-insulin-dose")
    suspend fun postInsulinDose(@Body request: PostInsulinDoseDto)
}