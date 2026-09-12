package com.alptrosoft.dexcom_data_collector_android_frontend.domain.repository

import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.InsulinDose
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.InsulinType

interface InsulinRepository {
    suspend fun getInsulinHistory(startMillis: Long, endMillis: Long): Result<List<InsulinDose>>
    suspend fun getInsulinTypes(): Result<List<InsulinType>>
    suspend fun postInsulinDose(typeId: Int, dose: Float, timestampMillis: Long): Result<Unit>
}