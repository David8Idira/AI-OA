package com.aioa.app.config

object ApiConfig {
    // Base URL for the API - update this to your actual API server
    const val BASE_URL = "https://api.aioa.example.com/v1/"
    
    // Timeouts in milliseconds
    const val CONNECT_TIMEOUT = 30_000L
    const val READ_TIMEOUT = 30_000L
    const val WRITE_TIMEOUT = 30_000L
    
    // Retry configuration
    const val MAX_RETRY_COUNT = 3
    const val RETRY_DELAY_MS = 1_000L
    
    // Headers
    const val HEADER_CONTENT_TYPE = "Content-Type"
    const val HEADER_AUTHORIZATION = "Authorization"
    const val CONTENT_TYPE_JSON = "application/json"
}