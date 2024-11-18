package com.example.lena

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lena.ui.screens.LoginScreen
import com.example.lena.ui.screens.MainMenu
import com.example.lena.ui.screens.SignUpScreen

enum class Screens{
    LoginScreen,
    MainMenu,
    SignUpScreen
}

@Composable
fun LenaAppNavigation(){
    val navController: NavHostController = rememberNavController()
    NavHost(navController = navController, startDestination = Screens.LoginScreen.name) {
        composable(Screens.LoginScreen.name) {
            LoginScreen(navController)
        }
        composable(Screens.MainMenu.name) {
            MainMenu(navController)
        }
        composable(Screens.SignUpScreen.name){
            SignUpScreen(navController)
        }
    }
}

