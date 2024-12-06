package com.example.lena.viewModels

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.lena.Screens
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale


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
    val isPasswordFieldsValid: Boolean = false,
    val successfulSignUp: Boolean = false,
    val signUpPasswordErrorMessage: String = "",
    val firstNameErrorMessage: String = "",
    val lastNameErrorMessage: String = "",
    val forgotPasswordEmail: String = "",
    val forgotPasswordEmailError: Boolean = false,
    val forgotPasswordErrorMessage: String = "",
    val forgotPasswordSuccess: Boolean = false,
    val authorizedUserFirstName: String = "",
    val authorizedUserEmail: String = "",
    val authorizedNewEmailAddress: String = "",
    val authorizedNewEmailError: Boolean = false,
    val isAuthorizedUserVerified: Boolean = false,
    val changeEmailPasswordConfirmationError: Boolean = false,
    val errorMessage: String = "",
    val infoMessage: String = "",
)

sealed class AuthState{
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
}

sealed class AuthEvent {
    object None : AuthEvent()
    data class Error(val message : String) : AuthEvent()
    data class Info(val message : String) : AuthEvent()
}

enum class toastType {
    Info, Error
}

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val fStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var lastVerificationEmailSentTime: Long = 0
    private var toastShown = false

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _authEvent = MutableSharedFlow<AuthEvent>(replay = 1)
    val authEvent: SharedFlow<AuthEvent> = _authEvent

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    companion object {
        private const val VERIFICATION_EMAIL_COOLDOWN = 2 * 60 * 1000 // 2 minutes in milliseconds
    }

    init {
        checkAuthStatus()
        fetchUserInfo()
        fetchVerificationStatus()
    }

    fun checkAuthStatus() {
        if (auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
        }
    }

    //====================================================================================--> Login
    fun login(email: String, password: String) {
        val formattedEmail = email.trim().lowercase()
        var isValidationFailed = false

        validateEmail(formattedEmail) { isValid, _ ->
            if (!isValid) {
                isValidationFailed = true
            }
        }

        if (password.isBlank()) {
            isValidationFailed = true
        }

        if (isValidationFailed) return

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(formattedEmail, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user != null) {
                    if (user.isEmailVerified) {

                        val userId = user.uid
                        val userRef = fStore.collection("Users").document(userId)
                        userRef.get().addOnSuccessListener { document ->
                            if (document != null) {
                                val storedEmail = document.getString("email") ?: ""
                                if (storedEmail != user.email) {
                                    userRef.update("email", user.email!!)
                                    //Log.i("AuthViewModel", "Email updated successfully")
                                } else {
                                    //Log.i("AuthViewModel", "Email matches Firestore record")
                                }
                                _authState.value = AuthState.Authenticated
                            } else {
                                toastMessage(toastType.Error, "Something is wrong")
                            }
                        }.addOnFailureListener { e ->
                            toastMessage(toastType.Error, e.message ?: ("Something is wrong"))
                        }

                    } else {
                        toastMessage(toastType.Error, "Please verify your email address")
                        _authState.value = AuthState.Authenticated
                    }
                }
            } else {
                toastMessage(toastType.Error, task.exception?.message ?: ("Invalid request"))
                _authState.value = AuthState.Unauthenticated
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

    //************************************************************************************
    //====================================================================================--> Sign Up
    fun signUp() {
        if (!uiState.value.isSignUpFormValid) {
            toastMessage(toastType.Error, "Please check all field then try again")
            return
        }
        checkIfEmailExistsInFirestore(_uiState.value.signUpEmail) { exists ->
            if (exists) {
                toastMessage(toastType.Error, "Email already exists")
                return@checkIfEmailExistsInFirestore
            }

            _authState.value = AuthState.Loading
            auth.createUserWithEmailAndPassword(_uiState.value.signUpEmail.lowercase().trim(), _uiState.value.password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userID = auth.currentUser?.uid ?: ""
                    val documentReference: DocumentReference = fStore.collection("Users").document(userID)
                    val user = mapOf(
                        "firstName" to _uiState.value.firstName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                        "lastName" to _uiState.value.lastName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                        "email" to _uiState.value.signUpEmail.lowercase().trim()
                    )
                    documentReference.set(user).addOnSuccessListener {
                        auth.currentUser?.sendEmailVerification()?.addOnCompleteListener { verificationTask ->
                            if (verificationTask.isSuccessful) {
                                /*TODO Verification email time*/
                                toastMessage(toastType.Info, "Email Verification sent")
                                _authState.value = AuthState.Authenticated
                            } else {
                                _authEvent.tryEmit(AuthEvent.Error(verificationTask.exception?.message ?: "Unknown error"))
                            }
                        }
                    }.addOnFailureListener { e ->
                        toastMessage(toastType.Error, e.message ?: "Email verification failed")
                    }
                } else {
                   toastMessage(toastType.Error, task.exception?.message ?: "Unknown error while creating account")
                    _authState.value = AuthState.Unauthenticated
                }
            }
        }
    }
    //====================================================================--> Name handling
    fun onFirstNameChange(newFirstName: String) {
        _uiState.update {
            it.copy(
                firstName = newFirstName.trim(),
                firstNameError = false // Reset error on change
            )
        }
    }

    fun onFirstNameFocusChanged(focused: Boolean) {
        if (!focused) {
            val (isValid, errorMessage) = validateName(_uiState.value.firstName, focused)
            _uiState.update {
                it.copy(
                    firstName = it.firstName.trim(),
                    firstNameError = !isValid,
                    firstNameErrorMessage = errorMessage
                )
            }
            validateSignUpForm()
        }
    }

    fun onLastNameChange(newLastName: String) {
        _uiState.update {
            it.copy(
                lastName = newLastName,
                lastNameError = false // Reset error on change
            )
        }
    }

    fun onLastNameFocusChanged(focused: Boolean) {
        if (!focused) {
            val (isValid, errorMessage) = validateName(_uiState.value.lastName, focused)
            _uiState.update {
                it.copy(
                    lastName = it.lastName.trim(),
                    lastNameError = !isValid,
                    lastNameErrorMessage = errorMessage
                )
            }
            validateSignUpForm()
        }
    }

    private fun validateName(name: String, focused: Boolean): Pair<Boolean, String> {
        val trimmedName = name.trim()
        val nameParts = trimmedName.split("\\s+".toRegex()).map {
            it.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
            }
        }

        return when {
            trimmedName.isBlank() -> Pair(false, "Name cannot be blank")
            (trimmedName.length < 2 || trimmedName.length > 15) && !focused -> Pair(false, "Name must be between 2 and 15 characters long")
            !trimmedName.all { it.isLetter() || it.isWhitespace() || it == '-' } -> Pair(false, "Name can only contain alphabetic characters and '-'")
            trimmedName.startsWith('-') -> Pair(false, "Name cannot start with '-'")
            trimmedName.any { it.isWhitespace() } && (nameParts.any { it.length < 2 }) -> Pair(false, "Each part of the name must be at least 2 characters long")
            else -> Pair(true, "")
        }
    }
    //====================================================================--> Email Handling
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

    fun validateEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    //====================================================================--> Password Handling
    fun onPasswordChange(newPassword: String, page: Screens) {
        _uiState.update {
            it.copy(
                password = newPassword,
                passwordError = false,
                repeatPasswordError = it.rePassword.isNotEmpty() && it.rePassword != newPassword
            )
        }
        when (page) {
            Screens.SignUpScreen -> validateSignUpForm()
            Screens.MyAccountScreen -> validatePasswordFields()
            else -> {}
        }
    }

    fun onPasswordFocusChanged(focused: Boolean, page : Screens) {
        if (!focused) {
            val (isValid, errorMessage) = validatePassword(_uiState.value.password)
            _uiState.update {
                it.copy(passwordError = !isValid && it.password.isNotBlank(), signUpPasswordErrorMessage = errorMessage)
            }
            when (page) {
                Screens.SignUpScreen -> validateSignUpForm()
                Screens.MyAccountScreen -> validatePasswordFields()
                else -> {}
            }
        } else {
            _uiState.update {
                it.copy(passwordError = false, signUpPasswordErrorMessage = "")
            }
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

    fun onRePasswordChange(newRePassword: String, page: Screens) {
        _uiState.update {
            it.copy(
                rePassword = newRePassword,
                repeatPasswordError = newRePassword.isNotEmpty() && newRePassword != _uiState.value.password
            )
        }
        when (page) {
            Screens.SignUpScreen -> validateSignUpForm()
            Screens.MyAccountScreen -> validatePasswordFields()
            else -> {}
        }
    }

    fun toggleRePasswordVisibility() {
        _uiState.update {
            it.copy(isRePasswordVisible = !it.isRePasswordVisible)
        }
    }
    //====================================================================--> Validate Entire Sign Up Form
    private fun validateSignUpForm() {
        _uiState.update {
            it.copy(
                isSignUpFormValid =
                        it.firstName.isNotBlank() &&
                        !it.firstNameError &&
                        it.lastName.isNotBlank() &&
                        !it.lastNameError &&
                        it.signUpEmail.isNotBlank() &&
                        !it.signUpEmailError && !it.isPasswordFieldsValid
            )
        }
    }

    internal fun validatePasswordFields() {
         _uiState.update {
             it.copy(
                 isPasswordFieldsValid =
                        it.password.isNotBlank() &&
                         !it.passwordError &&
                         it.rePassword.isNotBlank() &&
                         !it.repeatPasswordError &&
                         it.password == it.rePassword
             )
         }
    }
    //************************************************************************************
    //====================================================================================--> Forgot Password

    fun resetPassword(email: String) {
        val email = email.trim().lowercase()
        validateAndCheckEmail(email) { isValid, errorMessage ->
            if (!isValid) {
                setForgotEmailError(errorMessage)
                return@validateAndCheckEmail
            }

            _authState.value = AuthState.Loading
            auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _uiState.update { it.copy(forgotPasswordSuccess = true) }
                    clearAuthState()
                } else {
                    //_authEvent.value = AuthEvent.Error(task.exception?.message ?: "Unknown error")
                    _uiState.update { it.copy(forgotPasswordSuccess = false) }
                    clearAuthState()
                }
            }
        }
    }

    fun validateAndCheckEmail(email: String, onResult: (Boolean, String) -> Unit) {
        when {
            email.isBlank() -> onResult(false, "Email is required")
            !validateEmail(email) -> onResult(false, "Invalid email address")
            else -> checkIfEmailExistsInFirestore(email) { exists ->
                if (exists) {
                    onResult(true, "")
                    Log.i("email", "this email exists")
                } else {
                    onResult(false, "This email does not exist")
                    Log.i("email", "this doesn't  exists")
                }
            }
        }
    }

    fun validateEmail(email: String, onResult: (Boolean, String) -> Unit) {
        when {
            email.isBlank() -> onResult(false, "Email is required")
            !validateEmail(email) -> onResult(false, "Invalid email address")
            else -> onResult(true, "Validation successful")
        }
    }

    fun onForgotPasswordEmailChange(newEmail: String) {
        _uiState.update {
            it.copy(
                forgotPasswordEmail = newEmail,
                forgotPasswordEmailError = false // Reset error on change
            )
        }
    }

    fun checkIfEmailExistsInFirestore(email: String, onResult: (Boolean) -> Unit) {
        val email = email.trim().lowercase()
        val usersCollection = fStore.collection("Users")
        usersCollection.whereEqualTo("email", email.trim()).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documents = task.result
                    onResult(documents != null && !documents.isEmpty)
                } else {
                    onResult(false)
                }
            }
    }

    fun onForgotPasswordEmailFocusChanged(focused: Boolean) {
        if (!focused) {
            _uiState.update {
                it.copy(
                    forgotPasswordEmail = it.forgotPasswordEmail.trim(),
                    forgotPasswordEmailError = !validateEmail(it.forgotPasswordEmail.trim())
                )
            }
        }
    }

    fun setForgotEmailError(errorMessage: String) {
        _uiState.update {
            it.copy(
                forgotPasswordEmailError = true,
                forgotPasswordErrorMessage = errorMessage
            )
        }
    }
    //************************************************************************************
    //====================================================================================--> MyAccount
    fun fetchUserInfo() {
        val user = auth.currentUser
        user?.let {
            val userId = it.uid
            val userRef = fStore.collection("Users").document(userId)
            userRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    val firstName = document.getString("firstName") ?: ""
                    _uiState.update { state -> state.copy(authorizedUserFirstName =  firstName) }
                    val email = document.getString("email") ?: ""
                    _uiState.update { state -> state.copy(authorizedUserEmail =  email) }
                }
            }.addOnFailureListener { exception ->
                Log.w("AuthViewModel", "Error getting documents: ", exception)
            }
        }
    }

    fun fetchVerificationStatus() {
        val user = auth.currentUser
        if (user != null) {
            _uiState.update { state -> state.copy(isAuthorizedUserVerified = user.isEmailVerified) }
        } else {
            _uiState.update { state -> state.copy(isAuthorizedUserVerified = false) }
        }
    }

    fun onAuthorizedNewEmailChange(newEmail: String) {
        _uiState.update {
            it.copy(
                authorizedNewEmailAddress = newEmail,
                authorizedNewEmailError = false // Reset error on change
            )
        }
    }

    fun onAuthorizedNewEmailFocusChanged(focused: Boolean) {
        if (!focused) {
            _uiState.update {
                it.copy(
                    authorizedNewEmailAddress = it.authorizedNewEmailAddress.trim(),
                    authorizedNewEmailError = !validateEmail(it.authorizedNewEmailAddress.trim())
                )
            }
        }
    }

    private fun reauthenticateUser(password: String, onComplete: (Boolean) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val credential = EmailAuthProvider.getCredential(currentUser.email!!, password)
            currentUser.reauthenticate(credential).addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
        } else {
            onComplete(false)
        }
    }

    fun changeEmail(newEmail: String, password: String, callback: (Boolean) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            _authState.value = AuthState.Loading

            reauthenticateUser(password) { success ->
                if (success) {
                    currentUser.verifyBeforeUpdateEmail(newEmail).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            /*TODO Verification email time*/
                            toastMessage(toastType.Info, "Email verification sent")
                            callback(true)
                            signOut()

                        } else {

                            toastMessage(toastType.Error, task.exception?.message ?: "Email change failed")
                        }
                    }
                } else {
                    _uiState.update { it.copy(changeEmailPasswordConfirmationError = !success) }
                    callback(false)
                }
            }
        } else {
            toastMessage(toastType.Error, "User not authenticated")
        }
    }
    //====================================================================================--> Change Password



    //====================================================================================-->




    //====================================================================================--> Miscellaneous
    fun signOut(navController: NavController? = null) {
        auth.signOut()
        resetUiState()
        navController?.navigate(Screens.LoginScreen.name)
        _authState.value = AuthState.Unauthenticated

    }

    fun resetUiState() {
        _uiState.value = AuthUiState()
    }

    fun clearAuthState() {
        _authState.value = AuthState.Unauthenticated
    }

    private fun toastMessage(eventType: toastType, message: String) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {

                if (!toastShown) {
                    when (eventType) {
                        toastType.Info ->  {
                            _authEvent.tryEmit(AuthEvent.Info(message))
                        }
                        toastType.Error -> {
                            _authEvent.tryEmit(AuthEvent.Error(message))
                        }
                        else -> Log.e("AuthViewModel", "Unknown event type: $eventType")
                    }
                    _authEvent.emit(AuthEvent.None)
                } else toastShown = true
            }
        }
    }

    fun resetToastFlag() {
        toastShown = false  // Reset flag when you want to show toast again
    }

    fun sendVerificationEmail() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastVerificationEmailSentTime < VERIFICATION_EMAIL_COOLDOWN) {
            toastMessage(toastType.Error, "Please wait before requesting another verification email.")
            return
        }

        val user = auth.currentUser
        user?.let {
            it.reload().addOnCompleteListener { reloadTask ->
                if (reloadTask.isSuccessful) {
                    if (it.isEmailVerified) {
                        toastMessage(toastType.Info, "Your email is already verified.")
                    } else {
                        it.sendEmailVerification().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                lastVerificationEmailSentTime = currentTime
                                toastMessage(toastType.Info, "Verification email sent successfully.")
                            } else {
                                toastMessage(toastType.Error, task.exception?.message ?: "Failed to send verification email.")
                            }
                        }
                    }
                } else {
                    toastMessage(toastType.Error, reloadTask.exception?.message ?: "Failed to check email verification status.")
                }
            }
        }
    }
}




