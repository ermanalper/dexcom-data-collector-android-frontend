package com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AlarmDto(
    @SerializedName("id") val id: String,
    @SerializedName("level") val level: Int,
    @SerializedName("message") val message: String,
    @SerializedName("timestamp") val timestamp: String
)