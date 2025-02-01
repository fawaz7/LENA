package com.yarmouk.lena

/**
 * Navigation.kt
 *
 * This Kotlin file defines the navigation architecture for the LENA application using Jetpack Compose's Navigation component.
 * It utilizes the `NavHost` and `composable` functions to set up and manage the navigation graph of the app.
 *
 * Key Components:
 * - `Screens` Enum: Enumerates all the potential screens in the app, including LoginScreen, ChatMenu, SignUpScreen, ForgotPasswordScreen, and MyAccountScreen.
 * - `LenaAppNavigation` Composable: The primary function that configures the navigation controller and defines the navigation routes.
 *   - Uses `rememberNavController` to create a navigation controller.
 *   - Initializes `VoiceViewModel` using `WitAiClient` with the provided token.
 *   - Sets up the navigation graph using `NavHost`, starting from the LoginScreen.
 *   - Each screen is associated with a `composable` function that specifies the screen's content and behavior.
 *   - Dependency Injection: Injects `AuthViewModel` and `VoiceViewModel` into the respective screens.
 *
 * Dependency Injection:
 * - `AuthViewModel`: Used to manage authentication states and actions across different screens.
 * - `VoiceViewModel`: Utilized in the `MyAccountScreen` to manage voice-related functionalities, initialized with `WitAiClient`.
 *
 * Navigation Flow:
 * - The app starts at `LoginScreen`.
 * - Users can navigate to other screens such as ChatMenu, SignUpScreen, ForgotPasswordScreen, and MyAccountScreen.
 * - Each navigation action is handled by the `NavController` to transition between screens.
 *
 * The navigation structure ensures a seamless user experience by managing screen transitions and maintaining state across different parts of the app.
 */

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

