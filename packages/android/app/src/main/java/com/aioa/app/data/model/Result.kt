package com.aioa.app.data.model

/**
 * A generic wrapper class for API responses with result type.
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val code: Int = -1, val message: String, val throwable: Throwable? = null) : Result<Nothing>()
    data object Loading : Result<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw throwable ?: IllegalStateException(message)
        is Loading -> throw IllegalStateException("Result is still loading")
    }

    fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> this
    }

    companion object {
        fun <T> success(data: T): Result<T> = Success(data)
        fun error(message: String, code: Int = -1, throwable: Throwable? = null): Result<Nothing> =
            Error(code, message, throwable)
        fun loading(): Result<Nothing> = Loading
    }
}

/**
 * Extension to convert suspend functions to Result
 */
suspend inline fun <T> runCatching(crossinline block: suspend () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e: Throwable) {
        Result.error(e.message ?: "Unknown error", throwable = e)
    }
}