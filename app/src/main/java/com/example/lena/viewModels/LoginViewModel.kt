package com.example.lena.viewModels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class LoginState(
    val username: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isValid: Boolean = true,
    val error: String? = null
)

class LoginViewModel() : ViewModel() {
    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState

    fun onUsernameChange(newUsername: String) {
        _uiState.update { it.copy(username = newUsername) }
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.update { it.copy(password = newPassword) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun login(onSuccess: () -> Unit) {
        if (_uiState.value.username == "fawaz" && _uiState.value.password == "fawaz") {
            _uiState.update { it.copy(isValid = true, error = null) }
            onSuccess()
        } else {
            _uiState.update { it.copy(isValid = false, error = "Invalid credentials") }
        }
    }

    fun validateInputs(): Boolean {
        return _uiState.value.username.isNotBlank() && _uiState.value.password.isNotBlank()
    }
}