package com.example.lena

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lena.ui.screens.ForgotPasswordScreen
import com.example.lena.ui.screens.LoginScreen
import com.example.lena.ui.screens.MainMenu
import com.example.lena.ui.screens.MyAccountScreen
import com.example.lena.ui.screens.SignUpScreen
import com.example.lena.viewModels.AuthViewModel

enum class Screens{
    LoginScreen,
    MainMenu,
    SignUpScreen,
    ForgotPasswordScreen,
    MyAccountScreen,
}

@Composable
fun LenaAppNavigation(viewModel: AuthViewModel){

    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = Screens.LoginScreen.name) {
        composable(Screens.LoginScreen.name) {
            LoginScreen(navController, authViewModel = viewModel, modifier = Modifier)
        }
        composable(Screens.MainMenu.name) {
            MainMenu(navController, authViewModel = viewModel)
        }
        composable(Screens.SignUpScreen.name){
            SignUpScreen(navController =  navController, authViewModel = viewModel)
        }
        composable(Screens.ForgotPasswordScreen.name){
            ForgotPasswordScreen(navController = navController, authViewModel = viewModel)
        }
        composable(Screens.MyAccountScreen.name){
            MyAccountScreen(navController = navController, authViewModel = viewModel)
        }
    }
}

