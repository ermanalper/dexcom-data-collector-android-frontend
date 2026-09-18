package com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ClientResponseDto(
    @SerializedName("active_clients_count") val activeClientsCount: Int,
    @SerializedName("clients") val clients: List<String>
)