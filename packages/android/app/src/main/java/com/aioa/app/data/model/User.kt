package com.aioa.app.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String?,
    @SerializedName("display_name")
    val displayName: String?,
    @SerializedName("avatar_url")
    val avatarUrl: String?,
    @SerializedName("role")
    val role: String?,
    @SerializedName("created_at")
    val createdAt: Long?
)

data class LoginRequest(
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String
)

data class LoginResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String?,
    @SerializedName("expires_in")
    val expiresIn: Long,
    @SerializedName("user")
    val user: User
)