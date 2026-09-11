package com.alptrosoft.dexcom_data_collector_android_frontend.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.GlucoseReading
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.repository.GlucoseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
@HiltViewModel
class GlucoseViewModel @Inject constructor(
    private val repository: GlucoseRepository
) : ViewModel() {

    private val _state = MutableStateFlow(GlucoseUiState())
    val state = _state.asStateFlow()

    private val fetchedTimeRanges = mutableListOf<LongRange>()

    private val maxRetentionMillis = 15L * 24L * 3600000L
    //private val maxRetentionMillis = 3600000L for debugging, 1-hr retention
    init {
        viewModelScope.launch {
            fetchOldestDataLimit() // get the first data ever recorded
            fetchLatestGlucose()
            repository.observeLiveGlucose().collect { liveReading ->
                onEvent(GlucoseUiEvent.RealtimeDataReceived(liveReading))
            }
        }
    }

    fun onEvent(event: GlucoseUiEvent) {
        when (event) {
            is GlucoseUiEvent.OnVisibleRangeChanged -> checkAndFetchMissingData(event.startMillis, event.endMillis)
            is GlucoseUiEvent.OnChartDragged -> handleDrag(event.newEndTimeMillis)
            is GlucoseUiEvent.SnapToLatest -> snapToLatest()
            is GlucoseUiEvent.ChangeScale -> _state.update { it.copy(scaleHours = event.newScaleHours) }
            is GlucoseUiEvent.RealtimeDataReceived -> handleNewRealtimeData(event.newReading)
            is GlucoseUiEvent.RefreshRequested -> handleRefresh()
            is GlucoseUiEvent.GoToDate -> goToDate(event.dateMillis)
        }
    }

    private fun handleRefresh() {
        // 1. Ekranı anında en sağa (güncele) yasla
        snapToLatest()

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val stateVal = _state.value

            // 2. Tepedeki yazıyı güncellemek için en son veriyi çek
            fetchLatestGlucose()

            // 3. Aradaki boşluğu (gap) zekice hesapla ve yama yap
            // Eğer RAM boşsa son 12 saati baz al, doluysa en son noktanın saatini bul
            val lastKnownTimestamp = stateVal.readings.maxOfOrNull { it.timestampMillis }
                ?: (now - 12 * 3600000L)

            // API'den sadece eksik olan o aralığı (örneğin son 3 saati) iste
            if (lastKnownTimestamp < now) {
                repository.fetchGlucoseHistory(lastKnownTimestamp, now).onSuccess { missingData ->
                    if (missingData.isNotEmpty()) {
                        _state.update { currentState ->
                            val combined = (currentState.readings + missingData)
                                .distinctBy { it.timestampMillis }
                                .sortedBy { it.timestampMillis }
                            currentState.copy(readings = combined)
                        }
                    }
                }
            }
        }
    }

    private suspend fun fetchOldestDataLimit() {
        repository.fetchFirstDataDate().onSuccess { timestamp ->
            _state.update { it.copy(oldestDataLimitMillis = timestamp) }
        }
    }

    private fun checkAndFetchMissingData(viewStartMillis: Long, viewEndMillis: Long) {
        val limit = _state.value.oldestDataLimitMillis ?: 0L

        val neededStart = maxOf(viewStartMillis, limit)
        val neededEnd = minOf(viewEndMillis, System.currentTimeMillis())

        val isCovered = fetchedTimeRanges.any { it.contains(neededStart) && it.contains(neededEnd) }

        if (isCovered) {
            // Yeni veriye gerek yok, sadece uzaklaşan eski verileri RAM'den sil
            pruneRam(viewStartMillis, viewEndMillis)
            return
        }

        // 2. FETCH: Çekilmediyse 7 günlük bloklar (Chunk) halinde API'ye git
        val chunkMillis = 7L * 24L * 3600000L
        val fetchStart = maxOf(neededStart - chunkMillis, limit)
        val fetchEnd = minOf(neededEnd + chunkMillis, System.currentTimeMillis())

        // Çekilen aralığı "Ağ Hafızasına" kazı ki tekrar tekrar istek atmasın
        fetchedTimeRanges.add(fetchStart..fetchEnd)

        viewModelScope.launch {
            repository.fetchGlucoseHistory(fetchStart, fetchEnd).onSuccess { newData ->
                _state.update { currentState ->
                    val combined = (currentState.readings + newData)
                        .distinctBy { it.timestampMillis }
                        .sortedBy { it.timestampMillis }

                    // Veri RAM'e eklendiği an acımasızca budama yapıyoruz
                    currentState.copy(readings = applyRamPruning(combined, viewStartMillis, viewEndMillis))
                }

                // ÇOK KRİTİK: RAM'den uçurduğumuz verilerin Ağ Hafızasını da temizlemeliyiz.
                // Aksi halde oraya geri kaydırılırsa API'ye gitmez ve ekran boş kalır.
                cleanupFetchedRanges(viewStartMillis, viewEndMillis)
            }
        }
    }

    private fun pruneRam(viewStartMillis: Long, viewEndMillis: Long) {
        _state.update { currentState ->
            currentState.copy(readings = applyRamPruning(currentState.readings, viewStartMillis, viewEndMillis))
        }
    }

    private fun applyRamPruning(readings: List<GlucoseReading>, viewStart: Long, viewEnd: Long): List<GlucoseReading> {
        // Ekranda bakılan yerin 15 gün öncesi ve 15 gün sonrası HARİÇ her şeyi RAM'den sil
        val keepStart = viewStart - maxRetentionMillis
        val keepEnd = viewEnd + maxRetentionMillis
        return readings.filter { it.timestampMillis in keepStart..keepEnd }
    }

    private fun cleanupFetchedRanges(viewStart: Long, viewEnd: Long) {
        val keepStart = viewStart - maxRetentionMillis
        val keepEnd = viewEnd + maxRetentionMillis

        // Ağ hafızasındaki blokları (LongRange) kontrol et.
        // Eğer bir blok tamamen RAM'de tutulan aralığın dışına çıkmışsa, onu Ağ hafızasından da sil.
        fetchedTimeRanges.removeAll { it.last < keepStart || it.first > keepEnd }
    }
    private fun handleDrag(newEndTimeMillis: Long) {
        val currentState = _state.value
        val now = System.currentTimeMillis()
        val snapThreshold = 2 * 60 * 1000L

        val limit = currentState.oldestDataLimitMillis ?: 0L
        val visibleDuration = currentState.scaleHours * 3600000L
        val minAllowedEndTime = limit + visibleDuration

        var boundedEndTime = newEndTimeMillis
        if (boundedEndTime < minAllowedEndTime && limit > 0L) {
            boundedEndTime = minAllowedEndTime
        }

        if (boundedEndTime >= now - snapThreshold) {
            snapToLatest()
        } else {
            _state.update { it.copy(viewEndTimeMillis = boundedEndTime, isAutoScroll = false) }
        }
    }
    private fun goToDate(dateMillis: Long) {
        val currentState = _state.value
        val zone = ZoneId.systemDefault()

        // DatePicker millis'i UTC gün başlangıcı olarak gelir.
        // Önce gerçek takvim tarihini alıyoruz.
        val selectedDate = Instant
            .ofEpochMilli(dateMillis)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()

        // Grafiğin göstereceği günün yerel 00:00'ı
        val dayStartMillis = selectedDate
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

        val visibleDurationMillis =
            currentState.scaleHours * 3600000L

        val now = System.currentTimeMillis()
        val oldestLimit = currentState.oldestDataLimitMillis ?: dayStartMillis

        // Normal durumda seçilen günün başlangıcı + görünür süre
        var targetEndTime = dayStartMillis + visibleDurationMillis

        // Geleceğe gitmesini engelle
        if (targetEndTime > now) {
            targetEndTime = now
        }

        // En eski veri sınırının altına düşmesini engelle
        val minAllowedEndTime = oldestLimit + visibleDurationMillis

        if (targetEndTime < minAllowedEndTime) {
            targetEndTime = minAllowedEndTime
        }

        _state.update {
            it.copy(
                viewEndTimeMillis = targetEndTime,
                isAutoScroll = false
            )
        }
    }

    private fun snapToLatest() {
        _state.update { it.copy(viewEndTimeMillis = System.currentTimeMillis(), isAutoScroll = true) }
    }

    private fun handleNewRealtimeData(reading: GlucoseReading) {
        _state.update { currentState ->
            val updatedReadings = (currentState.readings + reading)
                .distinctBy { it.timestampMillis }
                .sortedBy { it.timestampMillis }

            val nextEndTime = if (currentState.isAutoScroll) reading.timestampMillis else currentState.viewEndTimeMillis

            currentState.copy(
                readings = updatedReadings,
                latestReading = reading,
                viewEndTimeMillis = nextEndTime
            )
        }
    }

    private suspend fun fetchLatestGlucose() {
        repository.fetchLatestGlucose().onSuccess { newestReading ->
            onEvent(GlucoseUiEvent.RealtimeDataReceived(newestReading))
        }
    }
}