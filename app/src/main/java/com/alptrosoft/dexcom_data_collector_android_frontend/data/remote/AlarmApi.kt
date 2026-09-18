package com.alptrosoft.dexcom_data_collector_android_frontend.data.remote

import com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.dto.AlarmDto
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Query

interface AlarmApi {
    // 1. Endpoint: GET /api/v1/alarm/active-alarms
    @GET("/api/v1/alarm/active-alarms")
    suspend fun getActiveAlarms(): List<AlarmDto>


    @PATCH("/api/v1/alarm/ack-alarm")
    suspend fun acknowledgeAlarm(@Query("alarm_id") alarmId: String)

    @PATCH("/api/v1/alarm/ack-all-alarms")
    suspend fun acknowledgeAllAlarms()
}