package com.yarmouk.lena

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import com.yarmouk.lena.ui.theme.LENATheme
import com.yarmouk.lena.viewModels.AuthViewModel

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authViewModel : AuthViewModel by viewModels()
        setContent {
            LENATheme {
                LenaAppNavigation(viewModel = authViewModel)
            }
        }
    }
}

