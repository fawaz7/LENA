package com.example.lena.viewModels

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val emailError: Boolean = false,
    val passwordError: Boolean = false,
    val isLogInFormValid: Boolean = false,
    val isSignUpFormValid: Boolean = false,
    val username: String = "",
    val rePassword: String = "",
    val isRePasswordVisible: Boolean = false,
    val userNameError: Boolean = false,
    val repeatPasswordError: Boolean = false,
    val successfulSignUp: Boolean = false
)

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState


    init {
        //_authState.value = AuthState.Loading
        checkAuthStatus()
    }

    fun checkAuthStatus() {
        if (auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated

        }
    }

    //====================================================================--> Login
    fun login(email: String, password: String) {

        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Username or password cannot be empty")
            return
        }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email.trim(), password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _authState.value = AuthState.Authenticated
            } else {
                _authState.value = AuthState.Error(task.exception?.message ?: "Unknown error")
            }
        }
    }

    fun onEmailChange(newEmail: String) {
        _uiState.update {
            it.copy(
                email = newEmail,
                emailError = false // Reset error on change
            )
        }
    }

    fun onEmailFocusChanged(focused: Boolean) {
        if (!focused) {
            _uiState.update {
                it.copy(
                    email = it.email.trim(),
                    emailError = !validateEmail(it.email.trim())
                )
            }
            validateForm()
        }
    }


    fun onPasswordChange(newPassword: String) {
        _uiState.update {
            it.copy(password = newPassword, passwordError = false)
        }
        validateForm()
    }

    fun togglePasswordVisibility() {
        _uiState.update {
            it.copy(isPasswordVisible = !it.isPasswordVisible)
        }
    }

    private fun validateForm() {
        _uiState.update {
            it.copy(
                isLogInFormValid = it.email.isNotBlank() &&
                        !it.emailError &&
                        it.password.isNotBlank()
            )
        }
    }

    private fun validateEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    //====================================================================--> Sign Up
    fun signUp() {
        if (uiState.value.isSignUpFormValid) {
            _authState.value = AuthState.Error("All fields must be filled and valid")
            return
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(_uiState.value.email, _uiState.value.password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _authState.value = AuthState.Authenticated
            } else {
                _authState.value = AuthState.Error(task.exception?.message ?: "Unknown error")
            }
        }
    }

    fun onUsernameChange(newUsername: String) {
        _uiState.update {
            it.copy(
                username = newUsername,
                userNameError = newUsername.isBlank()
            )
        }
        validateForm()
    }


    fun onRePasswordChange(newRePassword: String) {
        _uiState.update {
            it.copy(
                rePassword = newRePassword,
                repeatPasswordError = newRePassword.isNotEmpty() && newRePassword != _uiState.value.password
            )
        }
        validateForm()
    }

    fun toggleRePasswordVisibility() {
        _uiState.update {
            it.copy(isRePasswordVisible = !it.isRePasswordVisible)
        }
    }

    private fun validatePassword(password: String): Boolean {
        return password.length >= 8 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isLowerCase() } &&
                password.any { !it.isLetterOrDigit() }
    }

    private fun validateSignUpForm() {
        _uiState.update {
            it.copy(
                isSignUpFormValid = it.username.isNotBlank() &&
                        it.email.isNotBlank() &&
                        !it.emailError &&
                        it.password.isNotBlank() &&
                        !it.passwordError &&
                        it.rePassword.isNotBlank() &&
                        !it.repeatPasswordError &&
                        it.password == it.rePassword
            )
        }
    }

    fun onPasswordFocusChanged(focused: Boolean) {
        if (!focused) {
            _uiState.update {
                it.copy(passwordError = !validatePassword(it.password))
            }
            validateForm()
        } else {
            _uiState.update {
                it.copy(passwordError = false)
            }
        }
    }


    //====================================================================--> Sign Out
    fun signOut(){
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

}




sealed class AuthState{
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message : String) : AuthState()
}