package com.example.lena

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

enum class Screens{
    LoginScreen,
    MainMenu,
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
    }
}

