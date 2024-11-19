package com.example.lena.ui.screens


import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.lena.R
import com.example.lena.Screens
import com.example.lena.ui.rememberImeState
import com.example.lena.ui.theme.LENATheme
import com.example.lena.viewModels.LoginViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = viewModel(),
    modifier: Modifier = Modifier) {

    val uiState by viewModel.uiState.collectAsState()
    val isImeVisible by rememberImeState()
    val focusManager = LocalFocusManager.current
    val usernameFocusRequester = FocusRequester()
    val passwordFocusRequester = FocusRequester()

    val logoSize by animateDpAsState(targetValue = if (isImeVisible) 80.dp else 270.dp, animationSpec = tween(durationMillis = 300))
    val textSize by animateFloatAsState(targetValue = if (isImeVisible) 34f else 28f, animationSpec = tween(durationMillis = 300))
    val subTextSize by animateFloatAsState(targetValue = if (isImeVisible) 20f else 16f, animationSpec = tween(durationMillis = 300))
    val spacerHeight by animateDpAsState(targetValue = if (isImeVisible) 0.dp else 40.dp, animationSpec = tween(durationMillis = 300))
    val subSpacerHeight by animateDpAsState(targetValue = if (isImeVisible) 4.dp else 20.dp, animationSpec = tween(durationMillis = 300))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit){detectTapGestures(onTap = {focusManager.clearFocus()})}
    ) {


        Spacer(modifier = modifier.height(68.dp).fillMaxWidth())

        Crossfade(targetState = isImeVisible, label = "Image change animation") { isKeyboardVisible ->
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


        Crossfade(targetState = isImeVisible, label = "Welcome Screen Label") {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier.height(spacerHeight))
                Text(
                    text = "Welcome Back!",
                    fontWeight = if (!it) FontWeight.Bold else FontWeight.Light,
                    fontSize = textSize.sp,
                    modifier = Modifier
                )
                Text(
                    text = "Login to your account",
                    fontWeight = if (it) FontWeight.Bold else FontWeight.Light,
                    fontSize = subTextSize.sp,
                    modifier = Modifier
                )
                Spacer(modifier.height(subSpacerHeight))
            }
        }

        Spacer(modifier.height(20.dp))
        OutlinedTextField(
            value = uiState.username,
            onValueChange = {
                viewModel.onUsernameChange(it)
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
            value = uiState.password,
            onValueChange = {viewModel.onPasswordChange(it)},
            label = { Text(text = "Password") },
            modifier = Modifier.focusRequester(passwordFocusRequester),
            trailingIcon = {
                IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                    Icon(
                        imageVector = if (uiState.isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (uiState.isPasswordVisible) "Hide Password" else "Show Password"
                    )
                }
            },
            visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
        uiState.error?.let { error ->
            Text(text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp))
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
            modifier = modifier.padding(if (uiState.isValid) 8.dp else {0.dp})

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