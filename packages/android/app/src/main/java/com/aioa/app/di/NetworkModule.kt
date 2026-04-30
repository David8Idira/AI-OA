package com.aioa.app.di

import com.aioa.app.config.ApiConfig
import com.aioa.app.data.api.AioaApiService
import com.aioa.app.util.NetworkUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .connectTimeout(ApiConfig.CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(ApiConfig.READ_TIMEOUT, TimeUnit.MILLISECONDS)
            .writeTimeout(ApiConfig.WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .header("Content-Type", ApiConfig.CONTENT_TYPE_JSON)
                    .header("Accept", ApiConfig.CONTENT_TYPE_JSON)
                    .method(original.method, original.body)
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAioaApiService(retrofit: Retrofit): AioaApiService {
        return retrofit.create(AioaApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideNetworkUtils(networkUtils: NetworkUtils): NetworkUtils {
        return networkUtils
    }
}