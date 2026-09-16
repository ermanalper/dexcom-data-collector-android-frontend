package com.alptrosoft.dexcom_data_collector_android_frontend.domain.model

data class Alarm(
    val id: String,
    val level: Int,
    val message: String,
    val timestamp: String,
    val timestampMillis: Long
)