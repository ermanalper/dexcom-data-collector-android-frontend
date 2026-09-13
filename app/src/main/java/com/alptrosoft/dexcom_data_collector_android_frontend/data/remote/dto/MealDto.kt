package com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MealDto(
    @SerializedName("desc") val desc: String,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("glucose_value") val glucoseValue: Float?
)

data class PostMealDto(
    @SerializedName("desc") val desc: String,
    @SerializedName("timestamp") val timestamp: String
)
data class PostMealShortcutDto(
    @SerializedName("title") val title: String,
    @SerializedName("desc") val desc: String
)