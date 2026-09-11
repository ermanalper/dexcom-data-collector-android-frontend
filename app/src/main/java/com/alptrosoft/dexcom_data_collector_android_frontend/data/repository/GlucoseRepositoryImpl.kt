package com.alptrosoft.dexcom_data_collector_android_frontend.data.repository

import android.util.Log
import com.alptrosoft.dexcom_data_collector_android_frontend.BuildConfig.BASE_URL
import com.alptrosoft.dexcom_data_collector_android_frontend.data.mapper.toDomain
import com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.GlucoseApi
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.error.GlucoseDataException
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.model.GlucoseReading
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.repository.GlucoseRepository
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import retrofit2.HttpException
import java.io.IOException
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GlucoseRepositoryImpl @Inject constructor(
    private val api: GlucoseApi,
    private val okHttpClient: OkHttpClient,
    private val gson: Gson
) : GlucoseRepository {

    private fun mapException(e: Exception): GlucoseDataException {
        return when (e) {
            is IOException -> GlucoseDataException.NetworkError("Cannot talk to the server", e)
            is HttpException -> {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("GLUCOSE_API", "HTTP Error: $errorBody")
                GlucoseDataException.ServerError(e.code(), "Server error: $errorBody")
            }
            is com.google.gson.JsonSyntaxException -> GlucoseDataException.ParsingError("Data response error: ", e)
            else -> GlucoseDataException.UnknownError("Unknown error: ${e.message}", e)
        }
    }

    override suspend fun fetchLatestGlucose(): Result<GlucoseReading> {
        return try {
            val response = api.getLatestGlucose()
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(mapException(e)) // Sızıntı kapatıldı
        }
    }

    override suspend fun fetchGlucoseHistoryDataCountBased(limit: Int, offset: Int): Result<List<GlucoseReading>> {
        return try {
            val response = api.getGlucoseHistoryDataCountBased(limit, offset)
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(mapException(e))
        }
    }

    override suspend fun fetchGlucoseHistory(startTimeMillis: Long, endTimeMillis: Long?): Result<List<GlucoseReading>> {
        return try {
            val startStr = Instant.ofEpochMilli(startTimeMillis).toString()
            val endStr = endTimeMillis?.let { Instant.ofEpochMilli(it).toString() }

            val response = api.getGlucoseHistory(startStr, endStr)
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(mapException(e))
        }
    }

    override suspend fun fetchFirstDataDate(): Result<Long> {
        return try {
            val response = api.getFirstDataDate()
            if (response.isSuccessful) {
                val timestampString = response.body()?.timestamp
                if (timestampString != null) {
                    val millis = Instant.parse(timestampString).toEpochMilli()
                    Result.success(millis)
                } else {
                    Result.failure(GlucoseDataException.ParsingError("Timestamp is null in response body"))
                }
            } else {
                Result.failure(GlucoseDataException.ServerError(response.code(), response.message()))
            }
        } catch (e: Exception) {
            Result.failure(mapException(e))
        }
    }

    override fun observeLiveGlucose(): Flow<GlucoseReading> = callbackFlow {
        // Hardcode URL yerine kurala uygun sabit kullanımı
        val request = Request.Builder()
            .url(BASE_URL + GlucoseApi.STREAM_ENDPOINT)
            .build()

        val sseClient = okHttpClient.newBuilder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .apply { interceptors().clear() }
            .build()

        val listener = object : EventSourceListener() {
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                if (type == "new_glucose") {
                    try {
                        val jsonMap = gson.fromJson(data, Map::class.java)

                        val timeString = jsonMap["timestamp"] as String
                        val isoString = timeString.replace(" ", "T")
                        val timestampMillis = Instant.parse(isoString).toEpochMilli()

                        val newReading = GlucoseReading(
                            value = (jsonMap["value"] as Double).toInt(),
                            timestampMillis = timestampMillis,
                            trend = jsonMap["trend"] as String,
                            status = jsonMap["status"] as String
                        )
                        Log.d("LIVE_GLUCOSE", "Timestamp: ${newReading.timestampMillis} Value: ${newReading.value}")
                        trySend(newReading)

                    } catch (e: Exception) {
                        Log.e("LIVE_GLUCOSE", "Error: ${e.message}")
                    }
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                Log.e("LIVE_GLUCOSE", "SSE Bağlantı koptu veya hata: ${t?.message}")
            }
        }

        val eventSource = EventSources.createFactory(sseClient).newEventSource(request, listener)
        awaitClose { eventSource.cancel() }
    }
}