package com.alptrosoft.dexcom_data_collector_android_frontend.domain.repository

interface ClientRepository {
    suspend fun getActiveClients(): Result<List<String>>
    suspend fun testClient(clientName: String): Result<Unit>
}