package com.alptrosoft.dexcom_data_collector_android_frontend.data.remote

import com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.dto.ClientResponseDto
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface ClientApi {
    @GET("/api/v1/events/clients")
    suspend fun getActiveClients(): ClientResponseDto

    @POST("/api/v1/clients/test-client")
    suspend fun testClient(@Query("client_name") clientName: String)

    @PATCH("/api/v1/events/heartbeat")
    suspend fun sendHeartbeat(@Query("client_name") clientName: String)
}