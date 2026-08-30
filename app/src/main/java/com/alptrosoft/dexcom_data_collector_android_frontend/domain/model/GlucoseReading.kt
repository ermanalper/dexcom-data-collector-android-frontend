package com.alptrosoft.dexcom_data_collector_android_frontend.domain.model

data class GlucoseReading (
    val value: Int,
    val timestampMillis: Long,
    val trend: String,
    val status: String
)
