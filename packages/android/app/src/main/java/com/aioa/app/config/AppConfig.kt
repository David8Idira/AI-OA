package com.aioa.app.config

import com.aioa.app.BuildConfig

object AppConfig {
    // App info from BuildConfig
    val VERSION_NAME: String = BuildConfig.VERSION_NAME
    val VERSION_CODE: Int = BuildConfig.VERSION_CODE
    val IS_DEBUG: Boolean = BuildConfig.DEBUG
    
    // App identifiers
    const val APP_NAME = "AI-OA"
    const val APP_PACKAGE = "com.aioa.app"
    
    // Feature flags
    const val ENABLE_LOGGING = BuildConfig.DEBUG
    const val ENABLE_ANALYTICS = !BuildConfig.DEBUG
    
    // Storage keys
    object StorageKeys {
        const val ACCESS_TOKEN = "access_token"
        const val REFRESH_TOKEN = "refresh_token"
        const val USER_ID = "user_id"
        const val USER_NAME = "user_name"
        const val IS_LOGGED_IN = "is_logged_in"
    }
}