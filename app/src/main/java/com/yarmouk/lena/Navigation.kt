package com.yarmouk.lena

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yarmouk.lena.ui.screens.ForgotPasswordScreen
import com.yarmouk.lena.ui.screens.LoginScreen
import com.yarmouk.lena.ui.screens.ChatMenu
import com.yarmouk.lena.ui.screens.MyAccountScreen
import com.yarmouk.lena.ui.screens.SignUpScreen
import com.yarmouk.lena.utils.WitAiClient
import com.yarmouk.lena.viewModels.AuthViewModel
import com.yarmouk.lena.viewModels.VoiceViewModel

enum class Screens{
    LoginScreen,
    ChatMenu,
    SignUpScreen,
    ForgotPasswordScreen,
    MyAccountScreen,
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun LenaAppNavigation(viewModel: AuthViewModel){

    val navController: NavHostController = rememberNavController()
    val voiceViewModel = VoiceViewModel(WitAiClient(BuildConfig.WIT_AI_TOKEN))

    NavHost(navController = navController, startDestination = Screens.LoginScreen.name) {
        composable(Screens.LoginScreen.name) {
            LoginScreen(navController, authViewModel = viewModel, modifier = Modifier)
        }
        composable(Screens.ChatMenu.name) {
            ChatMenu(navController, authViewModel = viewModel)
        }
        composable(Screens.SignUpScreen.name){
            SignUpScreen(navController =  navController, authViewModel = viewModel)
        }
        composable(Screens.ForgotPasswordScreen.name){
            ForgotPasswordScreen(navController = navController, authViewModel = viewModel)
        }
        composable(Screens.MyAccountScreen.name){
            MyAccountScreen(navController = navController, authViewModel = viewModel, voiceViewModel = voiceViewModel)
        }
    }
}

