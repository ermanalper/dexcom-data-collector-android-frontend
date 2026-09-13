package com.alptrosoft.dexcom_data_collector_android_frontend.domain.model

data class Meal(
    val desc: String,
    val timestamp: String,
    val glucoseValue: Float?,
    val timestampMillis: Long
)