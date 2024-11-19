package com.example.lena.ViewModels

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.lifecycle.ViewModel

class SignUpViewModel : ViewModel(){

    var username by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var re_password by mutableStateOf("")
    var valid by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    fun validateEmail(emailInput: String): Boolean {
        if (emailInput.isEmpty()) return true
        return android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()
    }

    // Function to validate password length
    fun validatePassword(passwordInput: String): Boolean {
        if (passwordInput.isEmpty()) return true
        return passwordInput.length >= 8
    }

    // Function to validate repeat password
    fun validateRepeatPassword(passwordInput: String): Boolean {
        return passwordInput == password
    }

    fun validateForms(): Boolean{
        return username.isNotBlank() && email.isNotBlank() && password.isNotBlank() && re_password.isNotBlank()
    }





    val isValidEmail: Boolean
        get() = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    val isValidPassword: Boolean
        get() = password.length >= 8 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isDigit() }

    private fun notBlank(): Boolean {
        return username.isNotBlank() && email.isNotBlank() && password.isNotBlank() && re_password.isNotBlank()
    }

    fun signUp(onSuccess: () -> Unit){
        error = null

        if (notBlank()){
            if (!isValidEmail) {
                error = "Invalid email address"
                return
            }
            if (!isValidPassword) {
                error = "Password must be at least 8 characters, contain one uppercase letter and one number."
                return
            }
            if (password == re_password) {
                valid = true
                onSuccess()
            } else {
                valid = false
                error = "Passwords do not match"
            }
        } else {
            valid = false
            error = "Please fill in all fields"
        }
    }
}