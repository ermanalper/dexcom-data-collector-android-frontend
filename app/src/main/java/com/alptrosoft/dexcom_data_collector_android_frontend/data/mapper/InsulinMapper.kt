package com.alptrosoft.dexcom_data_collector_android_frontend.data.mapper

import com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.dto.InsulinDoseDto
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.InsulinDose
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

fun InsulinDoseDto.toDomainModel(): InsulinDose {
    return InsulinDose(
        insulinType = this.insulinType,
        timestamp = this.timestamp,
        dose = this.dose,
        glucoseVal = this.glucoseVal,
        timestampMillis = try {
            Instant.parse(this.timestamp).toEpochMilli()
        } catch (e: Exception) {
            try {
                // Eğer Z (UTC) ibaresi olmadan geliyorsa
                LocalDateTime.parse(this.timestamp).atZone(ZoneOffset.UTC).toInstant().toEpochMilli()
            } catch (e2: Exception) {
                0L
            }
        }
    )
}