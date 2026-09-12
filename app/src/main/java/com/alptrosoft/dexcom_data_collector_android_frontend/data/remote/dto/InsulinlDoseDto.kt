package com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.dto

import com.google.gson.annotations.SerializedName

data class InsulinDoseDto(
    @SerializedName("insulin_type")
    val insulinType: String,

    @SerializedName("timestamp")
    val timestamp: String,

    @SerializedName("dose")
    val dose: Float,

    @SerializedName("glucose_val")
    val glucoseVal: Float?
)