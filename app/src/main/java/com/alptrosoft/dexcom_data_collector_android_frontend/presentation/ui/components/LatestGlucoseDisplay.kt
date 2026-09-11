package com.alptrosoft.dexcom_data_collector_android_frontend.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.GlucoseReading
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.getTrendArrow
import androidx.compose.ui.graphics.Color

@Composable
fun LatestGlucoseDisplay(
    latestReading: GlucoseReading?,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (latestReading?.status?.uppercase()) {
        "NORMAL" -> Color.Green.copy() // alpha = 0.4f gibi parametreler kullanılabilir
        "WARNING" -> Color.Yellow.copy()
        "CRITICAL" -> Color.Red.copy()
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Latest Value",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val valueText = latestReading?.value?.toInt()?.toString() ?: "---"
            val trend = latestReading?.trend
            Text(
                text = "$valueText ${getTrendArrow(trend)}",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}