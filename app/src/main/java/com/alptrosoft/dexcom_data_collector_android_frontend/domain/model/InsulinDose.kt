package com.alptrosoft.dexcom_data_collector_android_frontend.domain.model

data class InsulinDose(
    val insulinType: String,
    val timestamp: String,
    val dose: Float,
    val glucoseVal: Float?,
    val timestampMillis: Long
)