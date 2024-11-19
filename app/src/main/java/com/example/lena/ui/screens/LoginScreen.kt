package com.example.lena.ui.screens


import android.R.attr.tint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter.Companion.tint
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.lena.R
import com.example.lena.Screens
import com.example.lena.ViewModels.LoginViewModel
import com.example.lena.ui.rememberImeState
import com.example.lena.ui.theme.LENATheme

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = viewModel(),
    modifier: Modifier = Modifier) {

    var username by remember { mutableStateOf(viewModel.username) }
    var password by remember { mutableStateOf(viewModel.password) }
    var valid by remember { mutableStateOf(viewModel.valid) }
    val isImeVisible by rememberImeState()
    val usernameFocusRequester = FocusRequester()
    val passwordFocusRequester = FocusRequester()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val logoSize by animateDpAsState(
        targetValue = if (isImeVisible) 80.dp else 270.dp // Adjust logo size based on keyboard visibility
    )

    Column(
        modifier = Modifier.fillMaxWidth().pointerInput(Unit){detectTapGestures(onTap = {focusManager.clearFocus()})}
        , horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {


        Spacer(modifier = modifier.height(68.dp).fillMaxWidth())

        Crossfade(targetState = isImeVisible) { isKeyboardVisible ->
            Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                when (isKeyboardVisible) {
                    false -> {
                        Image(
                            painter = painterResource(id = R.drawable.full_logo_black),
                            contentDescription = "Default Logo",
                            modifier = Modifier.size(logoSize).align(Alignment.Center)
                        )
                    }
                    true -> {
                        Image(
                            painter = painterResource(id = R.drawable.medusa_black),
                            contentDescription = "Keyboard Active Logo",
                            modifier = Modifier.size(logoSize).align(Alignment.Center)
                        )
                    }
                }
            }
        }

        Spacer(modifier.height(28.dp))


        Crossfade(targetState = isImeVisible, label = "") {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Welcome Back!",
                    fontWeight = FontWeight.Bold,
                    fontSize = if (it) 34.sp else 28.sp, // Font size increases when keyboard is visible
                    modifier = Modifier
                )
                Text(
                    text = "Login to your account",
                    fontSize = if (it) 20.sp else 16.sp, // Font size increases when keyboard is visible
                    modifier = Modifier
                )
            }
        }
        Spacer(modifier.height(20.dp))
        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                viewModel.username = it
                            },
            label = { Text(text = "Username") },
            modifier = Modifier.focusRequester(usernameFocusRequester),
            trailingIcon = {Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Username Icon"
            )
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { passwordFocusRequester.requestFocus() }
            )
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it
                            viewModel.password = it},
            label = { Text(text = "Password") },
            modifier = Modifier.focusRequester(passwordFocusRequester),
            trailingIcon = {Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Password Icon"
            )},
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    viewModel.login {
                        navController.navigate(Screens.MainMenu.name)
                    }
                }
            )
        )
        viewModel.error?.let { error ->
            Text(text = error, color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp, bottom = 4.dp))
            valid = false
        }

        Button(
            onClick = {
                viewModel.login {
                    navController.navigate(Screens.MainMenu.name)
                }
            },
            enabled = viewModel.validateInputs(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black // Change this to your desired color
            ),
            modifier = modifier.padding(if (valid) 8.dp else {0.dp})

        ) {
            Text(text = "Login")
        }

        Text(
            text = "Forgot Password?", modifier = Modifier.clickable(onClick = { /*TODO*/ }))
        Row{
            Text(text = "Don't have an account?", color = Color.Gray)
            Text(text = " Sign Up", modifier = Modifier.clickable(onClick = { navController.navigate(
                Screens.SignUpScreen.name) }))
        }

    }
}


@Preview (showSystemUi = true)
@Composable
fun LoginScreenPreview(){
    LENATheme {
        LoginScreen(navController = rememberNavController())
    }
}