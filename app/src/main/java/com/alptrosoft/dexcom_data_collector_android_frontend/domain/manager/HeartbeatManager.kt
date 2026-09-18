package com.alptrosoft.dexcom_data_collector_android_frontend.domain.manager

import com.alptrosoft.dexcom_data_collector_android_frontend.BuildConfig
import com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.ClientApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeartbeatManager @Inject constructor(
    private val api: ClientApi
) {
    // Uygulama ayakta olduğu sürece yaşayacak olan Coroutine Scope
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun startHeartbeat() {
        scope.launch {
            while (isActive) { // Scope aktif olduğu sürece döngüye devam et
                try {
                    api.sendHeartbeat(BuildConfig.CLIENT_NAME)
                } catch (e: Exception) {
                    // Bağlantı kopması veya sunucu hatası durumunda uygulama çökmez.
                    // İstersen buraya Log ekleyebilirsin: Log.e("Heartbeat", "Error: ${e.message}")
                }
                delay(30_000L) // 30 saniye bekle
            }
        }
    }
}