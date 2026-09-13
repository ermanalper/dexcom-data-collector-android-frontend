package com.alptrosoft.dexcom_data_collector_android_frontend.data.mapper

import com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.dto.MealDto
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.Meal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

fun MealDto.toDomainModel(): Meal {
    return Meal(
        desc = this.desc,
        timestamp = this.timestamp,
        glucoseValue = this.glucoseValue,
        timestampMillis = try {
            Instant.parse(this.timestamp).toEpochMilli()
        } catch (e: Exception) {
            try {
                val safeTimestamp = this.timestamp.replace(" ", "T")
                LocalDateTime.parse(safeTimestamp).atZone(ZoneOffset.UTC).toInstant().toEpochMilli()
            } catch (e2: Exception) {
                0L
            }
        }
    )
}