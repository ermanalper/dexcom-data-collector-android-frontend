package com.alptrosoft.dexcom_data_collector_android_frontend.data.repository

import com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.ClientApi
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.repository.ClientRepository
import javax.inject.Inject

class ClientRepositoryImpl @Inject constructor(
    private val api: ClientApi
) : ClientRepository {

    override suspend fun getActiveClients(): Result<List<String>> {
        return try {
            val response = api.getActiveClients()
            Result.success(response.clients)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun testClient(clientName: String): Result<Unit> {
        return try {
            api.testClient(clientName)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}