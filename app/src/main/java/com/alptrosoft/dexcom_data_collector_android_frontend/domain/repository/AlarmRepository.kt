package com.alptrosoft.dexcom_data_collector_android_frontend.domain.repository

import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.Alarm

interface AlarmRepository {
    suspend fun getActiveAlarms(): Result<List<Alarm>>
    suspend fun acknowledgeAlarm(id: String): Result<Unit>
}