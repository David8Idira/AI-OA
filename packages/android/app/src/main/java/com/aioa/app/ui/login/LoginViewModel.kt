package com.aioa.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aioa.app.data.api.AioaApiClient
import com.aioa.app.data.model.LoginRequest
import com.aioa.app.data.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val apiClient: AioaApiClient
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            
            // Note: Replace with actual API call when backend is ready
            // val result = apiClient.execute {
            //     apiService.login(LoginRequest(username, password))
            // }
            
            // Simulate network delay for demo
            kotlinx.coroutines.delay(1000)
            
            // For demo purposes, just check if fields are filled
            if (username.isNotBlank() && password.length >= 6) {
                _uiState.value = LoginUiState.Success
            } else {
                _uiState.value = LoginUiState.Error("用户名或密码错误")
            }
        }
    }
}