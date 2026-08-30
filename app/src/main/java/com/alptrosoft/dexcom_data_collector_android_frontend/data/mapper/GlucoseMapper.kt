package com.alptrosoft.dexcom_data_collector_android_frontend.data.mapper

import com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.dto.GlucoseDto
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.GlucoseReading
import java.time.Instant

fun GlucoseDto.toDomain(): GlucoseReading {
    return GlucoseReading(
        value = this.value,
        timestampMillis = Instant.parse(this.timestamp).toEpochMilli(),
        trend = this.trend,
        status = this.status
    )
}
fun List<GlucoseDto>.toDomain(): List<GlucoseReading> =
    map { it.toDomain() }