package com.example.lena.ViewModels

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.lena.Screens


class LoginViewModel : ViewModel() {
    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoginMode by mutableStateOf(true)
    var valid by mutableStateOf(true)
    var error by mutableStateOf<String?>(null)

    fun login(onSuccess: () -> Unit){
        error = null

        // Simulate login logic (replace with real authentication logic)
        if (username == "fawaz" && password == "fawaz") {
            valid = true
            onSuccess()
        } else {
            valid = false
            error = "Invalid credentials"
        }
    }

    fun validateInputs(): Boolean {
        return username.isNotBlank() && password.isNotBlank()
    }
}

