package com.alptrosoft.dexcom_data_collector_android_frontend.domain.repository

import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.InsulinDose

interface InsulinRepository {
    suspend fun getInsulinHistory(startMillis: Long, endMillis: Long): Result<List<InsulinDose>>
}