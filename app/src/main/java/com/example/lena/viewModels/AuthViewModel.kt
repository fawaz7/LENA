package com.example.lena.viewModels

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.log

data class AuthUiState(
    val firstName: String = "",
    val lastName: String = "",
    val firstNameError: Boolean = false,
    val lastNameError: Boolean = false,
    val loginEmail: String = "",
    val signUpEmail: String = "",
    val loginEmailError: Boolean = false,
    val signUpEmailError: Boolean = false,
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val passwordError: Boolean = false,
    val isLogInFormValid: Boolean = false,
    val isSignUpFormValid: Boolean = false,
    val rePassword: String = "",
    val isRePasswordVisible: Boolean = false,
    val repeatPasswordError: Boolean = false,
    val successfulSignUp: Boolean = false,
    val signUpPasswordErrorMessage: String = "",
    val firstNameErrorMessage: String = "",
    val lastNameErrorMessage: String = ""
)

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val fStore: FirebaseFirestore = FirebaseFirestore.getInstance()


    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState


    init {
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
            _authState.value = AuthState.Error("Email or password cannot be empty")
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
                loginEmail = newEmail,
                loginEmailError = false // Reset error on change
            )
        }
    }

    fun onLoginEmailFocusChanged(focused: Boolean) {
        if (!focused) {
            _uiState.update {
                it.copy(
                    loginEmail = it.loginEmail.trim(),
                    loginEmailError = !validateEmail(it.loginEmail.trim())
                )
            }
            validateSignInForm()
        }
    }


    fun onSignInPasswordChange(newPassword: String) {
        _uiState.update {
            it.copy(
                password = newPassword,
                passwordError = false,
                repeatPasswordError = it.rePassword.isNotEmpty() && it.rePassword != newPassword
            )
        }
        validateSignInForm()
    }

    fun togglePasswordVisibility() {
        _uiState.update {
            it.copy(isPasswordVisible = !it.isPasswordVisible)
        }
    }

    private fun validateSignInForm() {
        _uiState.update {
            it.copy(
                isLogInFormValid = it.loginEmail.isNotBlank() &&
                        !it.loginEmailError &&
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
            Log.e("SignUp", "fname: ${uiState.value.firstName}, lname: ${uiState.value.lastName}, email: ${uiState.value.signUpEmail}, password: ${uiState.value.password}, rePassword: ${uiState.value.rePassword}")
            return
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(_uiState.value.signUpEmail, _uiState.value.password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userID = auth.currentUser?.uid ?: ""
                val documentReference: DocumentReference = fStore.collection("Users").document(userID)
                val user = mapOf(
                    "firstName" to _uiState.value.firstName,
                    "lastName" to _uiState.value.lastName,
                    "email" to _uiState.value.signUpEmail
                )
                documentReference.set(user).addOnSuccessListener {
                    _authState.value = AuthState.Authenticated
                }.addOnFailureListener { e ->
                    _authState.value = AuthState.Error(e.message ?: "Unknown error")
                }
            } else {
                _authState.value = AuthState.Error(task.exception?.message ?: "Unknown error")
            }
        }
    }

    fun onSignUpPasswordChange(newPassword: String) {
        _uiState.update {
            it.copy(
                password = newPassword,
                passwordError = false,
                repeatPasswordError = it.rePassword.isNotEmpty() && it.rePassword != newPassword
            )
        }
        validateSignUpForm()
    }


    fun onRePasswordChange(newRePassword: String) {
        _uiState.update {
            it.copy(
                rePassword = newRePassword,
                repeatPasswordError = newRePassword.isNotEmpty() && newRePassword != _uiState.value.password
            )
        }
        validateSignUpForm()
    }

    fun toggleRePasswordVisibility() {
        _uiState.update {
            it.copy(isRePasswordVisible = !it.isRePasswordVisible)
        }
    }

    private fun validatePassword(password: String): Pair<Boolean, String> {
        return when {
            password.length < 8 -> Pair(false, "Character count must be more than 8 characters long")
            !password.any { it.isLowerCase() } -> Pair(false, "Password must have a lowercase letter")
            !password.any { it.isUpperCase() } -> Pair(false, "Password must have an uppercase letter")
            !password.any { !it.isLetterOrDigit() } -> Pair(false, "Password must have a special character")
            else -> Pair(true, "")
        }
    }

    private fun validateSignUpForm() {
        _uiState.update {
            it.copy(
                isSignUpFormValid =
                        it.firstName.isNotBlank() &&
                        !it.firstNameError &&
                        it.lastName.isNotBlank() &&
                        !it.lastNameError &&
                        it.signUpEmail.isNotBlank() &&
                        !it.signUpEmailError &&
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
            val (isValid, errorMessage) = validatePassword(_uiState.value.password)
            _uiState.update {
                it.copy(passwordError = !isValid && it.password.isNotBlank(), signUpPasswordErrorMessage = errorMessage)
            }
            validateSignUpForm()
        } else {
            _uiState.update {
                it.copy(passwordError = false, signUpPasswordErrorMessage = "")
            }
        }
    }

    private fun validateName(name: String): Pair<Boolean, String> {
        val trimmedName = name.trim()
        val nameParts = trimmedName.split("\\s+".toRegex()).map { it.capitalize() }
        val capitalized = nameParts.joinToString(" ")

        return when {
            trimmedName.isBlank() -> Pair(false, "Name cannot be blank")
            trimmedName.length < 2 || trimmedName.length > 15 -> Pair(false, "Name must be between 2 and 15 characters long")
            !trimmedName.all { it.isLetter() || it.isWhitespace() } -> Pair(false, "Name can only contain alphabetic characters")
            trimmedName.any { it.isWhitespace() } && (nameParts.any { it.length < 2 }) -> Pair(false, "Each part of the name must be at least 2 characters long")
            else -> Pair(true, "")
        }
    }

    fun onFirstNameChange(newFirstName: String) {
        val (isValid, errorMessage) = validateName(newFirstName)
        _uiState.update {
            it.copy(
                firstName = newFirstName.trim(),
                firstNameError = !isValid,
                firstNameErrorMessage = errorMessage
            )
        }
        validateSignUpForm()
    }

    fun onLastNameChange(newLastName: String) {
        val (isValid, errorMessage) = validateName(newLastName)
        _uiState.update {
            it.copy(
                lastName = newLastName.trim(),
                lastNameError = !isValid,
                lastNameErrorMessage = errorMessage
            )
        }
        validateSignUpForm()
    }

    fun onSignUpEmailChange(newEmail: String) {
        _uiState.update {
            it.copy(
                signUpEmail = newEmail,
                signUpEmailError = false // Reset error on change
            )
        }
    }

    fun onSignUpEmailFocusChanged(focused: Boolean) {
        if (!focused) {
            _uiState.update {
                it.copy(
                    signUpEmail = it.signUpEmail.trim(),
                    signUpEmailError = !validateEmail(it.signUpEmail.trim())
                )
            }
            validateSignUpForm()
        }
    }




    //====================================================================--> Sign Out
    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
        resetUiState()
    }

    fun resetUiState() {
        _uiState.value = AuthUiState()
    }

}




sealed class AuthState{
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message : String) : AuthState()
}