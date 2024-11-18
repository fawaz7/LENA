package com.example.lena.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
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

    Column(
        modifier = Modifier.fillMaxWidth().pointerInput(Unit){detectTapGestures(onTap = {focusManager.clearFocus()})}
        , horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = modifier
            .height(90.dp)
            .fillMaxWidth())
        if (!isImeVisible) {
            Image(
                painter = painterResource(id = R.drawable.full_logo_black),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(270.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
            )
            Spacer(modifier.height(50.dp))
        } else {
            Image(
                painter = painterResource(id = R.drawable.medusa_black),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(80.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
            )
            Spacer(modifier.height(12.dp))
        }

        Text(
            text = "Welcome Back",
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            modifier = Modifier

        )
        Text(
            text = "Login to your account",
            modifier = Modifier
        )
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