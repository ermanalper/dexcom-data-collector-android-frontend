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
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun startHeartbeat() {
        scope.launch {
            while (isActive) {
                try {
                    api.sendHeartbeat(BuildConfig.CLIENT_NAME)
                } catch (e: Exception) {

                }
            }
        }
    }
}