package com.aioa.app.data.api

import com.aioa.app.data.model.Result
import com.aioa.app.util.NetworkUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Retrofit API client wrapper with error handling and Flow-based responses
 */
@Singleton
class AioaApiClient @Inject constructor(
    private val apiService: AioaApiService,
    private val networkUtils: NetworkUtils
) {

    /**
     * Execute API call and wrap result in Flow
     */
    fun <T> executeFlow(call: suspend () -> Response<T>): Flow<Result<T>> = flow {
        emit(Result.Loading)
        try {
            if (!networkUtils.isNetworkAvailable()) {
                emit(Result.error("Network unavailable", code = -1))
                return@flow
            }
            val response = call()
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(Result.success(it))
                } ?: emit(Result.error("Empty response body", response.code()))
            } else {
                emit(Result.error(
                    message = response.message() ?: "Unknown error",
                    code = response.code()
                ))
            }
        } catch (e: Exception) {
            emit(Result.error(e.message ?: "Network error", throwable = e))
        }
    }

    /**
     * Suspend-based API call wrapper
     */
    suspend fun <T> execute(call: suspend () -> Response<T>): Result<T> {
        return try {
            if (!networkUtils.isNetworkAvailable()) {
                return Result.error("Network unavailable", code = -1)
            }
            val response = call()
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.error("Empty response body", response.code())
            } else {
                Result.error(
                    message = response.message() ?: "Unknown error",
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            Result.error(e.message ?: "Network error", throwable = e)
        }
    }
}