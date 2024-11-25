package com.example.lena.viewModels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Data class representing the state of the Sign-Up form
data class SignUpState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val rePassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isRePasswordVisible: Boolean = false,
    val isFormValid: Boolean = false,
    val emailError: Boolean = false,
    val passwordError: Boolean = false,
    val userNameError: Boolean = false,
    val repeatPasswordError: Boolean = false,
    val successfulSignUp: Boolean = false
)

class SignUpViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SignUpState())
    val uiState: StateFlow<SignUpState> = _uiState.asStateFlow()


    fun onUsernameChange(newUsername: String) {
        _uiState.value = _uiState.value.copy(
            username = newUsername,
            userNameError = newUsername.isBlank()
        )
        validateForm()
    }

    fun onEmailChange(newEmail: String) {
        val isEmailValid = validateEmail(newEmail)
        _uiState.value = _uiState.value.copy(
            email = newEmail.trim(),
            emailError = newEmail.isBlank() || !isEmailValid
        )
        validateForm()
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.value = _uiState.value.copy(
            password = newPassword,
            passwordError = newPassword.isNotEmpty() && !validatePassword(newPassword)
        )
        validateForm()
    }


    fun onRePasswordChange(newRePassword: String) {
        _uiState.value = _uiState.value.copy(
            rePassword = newRePassword,
            repeatPasswordError = newRePassword.isNotEmpty() && newRePassword != _uiState.value.password
        )
        validateForm()
    }


    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isPasswordVisible = !_uiState.value.isPasswordVisible
        )
    }

    fun toggleRePasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isRePasswordVisible = !_uiState.value.isRePasswordVisible
        )
    }


    fun signUp(onSuccess: () -> Unit) {

        if (validateForms()) {
            _uiState.value = _uiState.value.copy(successfulSignUp = true)
            onSuccess()
        }
    }


    fun validateForm() {
        _uiState.value = _uiState.value.copy(
            isFormValid = _uiState.value.username.isNotBlank() &&
                    _uiState.value.email.isNotBlank() &&
                    !uiState.value.emailError &&
                    !uiState.value.passwordError &&
                    !uiState.value.repeatPasswordError
        )
    }


    private fun validateForms(): Boolean {
        return _uiState.value.username.isNotBlank() &&
                _uiState.value.email.isNotBlank() &&
                validateEmail(_uiState.value.email) &&
                validatePassword(_uiState.value.password) &&
                _uiState.value.password == _uiState.value.rePassword
    }

    private fun validateEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun validatePassword(password: String): Boolean {
        return password.length >= 8 && password.any { it.isUpperCase() } && password.any { it.isDigit() }
    }
}