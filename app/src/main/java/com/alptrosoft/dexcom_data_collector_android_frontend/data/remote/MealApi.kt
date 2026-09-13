package com.alptrosoft.dexcom_data_collector_android_frontend.data.remote

import com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.dto.MealDto
import com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.dto.MealShortcutDto
import com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.dto.PostMealDto
import com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.dto.PostMealShortcutDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MealApi {
    @GET("/api/v1/meal/history/by-time")
    suspend fun getMealHistory(
        @Query("start_time") startTime: String,
        @Query("end_time") endTime: String? = null
    ): List<MealDto>

    @POST("/api/v1/meal/add-meal")
    suspend fun postMeal(@Body request: PostMealDto)

    @POST("/api/v1/meal/add-meal-shortcut")
    suspend fun postMealShortcut(@Body request: PostMealShortcutDto)

    @GET("/api/v1/meal/meal-shortcuts")
    suspend fun getMealShortcuts(): List<MealShortcutDto>
}