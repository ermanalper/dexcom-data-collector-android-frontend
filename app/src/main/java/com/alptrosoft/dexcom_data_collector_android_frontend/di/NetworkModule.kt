package com.alptrosoft.dexcom_data_collector_android_frontend.di
import com.alptrosoft.dexcom_data_collector_android_frontend.data.remote.GlucoseApi
import com.alptrosoft.dexcom_data_collector_android_frontend.BuildConfig
import com.alptrosoft.dexcom_data_collector_android_frontend.data.repository.GlucoseRepositoryImpl
import com.alptrosoft.dexcom_data_collector_android_frontend.domain.repository.GlucoseRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {


    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder().create()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideGlucoseApi(retrofit: Retrofit): GlucoseApi {
        return retrofit.create(GlucoseApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGlucoseRepository(
        api: GlucoseApi,
        okHttpClient: OkHttpClient,
        gson: Gson
    ): GlucoseRepository {
        return GlucoseRepositoryImpl(api, okHttpClient, gson)
    }
}

