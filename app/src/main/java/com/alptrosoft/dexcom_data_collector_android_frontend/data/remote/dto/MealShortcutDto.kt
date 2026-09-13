package com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MealShortcutDto(
    @SerializedName("title") val title: String,
    @SerializedName("desc") val desc: String
)