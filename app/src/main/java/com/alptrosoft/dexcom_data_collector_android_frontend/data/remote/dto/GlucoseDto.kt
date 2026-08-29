package com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.dto

data class GlucoseDto (
    val value: Int,
    val timestamp: String,
    val trend: String,
    val source: String,
    val status: String
)