package com.alptrosoft.dexcom_data_collector_android_frontend.domain.model

data class GlucoseReading (
    val value: Int,
    val timestampMillis: Long,
    val trend: String,
    val status: String
)
fun getTrendArrow(trend: String?): String {
    if (trend == null) {
        return "-"
    }
    return when (trend) {
        "SINGLE_UP" -> "↑"
        "DOUBLE_UP" -> "↑↑"
        "FORTY_FIVE_UP" -> "↗"
        "FLAT" -> "→"
        "FORTY_FIVE_DOWN" -> "↘"
        "SINGLE_DOWN" -> "↓"
        "DOUBLE_DOWN" -> "↓↓"
        else -> "?"
    }}