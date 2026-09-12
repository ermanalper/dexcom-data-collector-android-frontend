package com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.dto

import com.google.gson.annotations.SerializedName

data class InsulinTypeDto(
    @SerializedName("id") val id: Int,
    @SerializedName("type") val type: String
)

data class PostInsulinDoseDto(
    @SerializedName("insulin_id") val insulinId: Int,
    @SerializedName("dose") val dose: Float,
    @SerializedName("timestamp") val timestamp: String
)