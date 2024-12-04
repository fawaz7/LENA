package com.example.lena.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.lena.R
import com.example.lena.Screens
import com.example.lena.ui.rememberImeState
import com.example.lena.ui.theme.Gray800
import com.example.lena.ui.theme.Gray900
import com.example.lena.ui.theme.LENATheme
import com.example.lena.viewModels.AuthEvent
import com.example.lena.viewModels.AuthState
import com.example.lena.viewModels.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel ,
    modifier: Modifier = Modifier) {

    val uiState by authViewModel.uiState.collectAsState()
    val isImeVisible by rememberImeState()
    val focusManager = LocalFocusManager.current
    val usernameFocusRequester = FocusRequester()
    val passwordFocusRequester = FocusRequester()
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    val logoSize by animateDpAsState(targetValue = if (isImeVisible) 80.dp else 240.dp, animationSpec = tween(durationMillis = 300),
        label = "LogoSize Animation"
    )
    val textSize by animateFloatAsState(targetValue = if (isImeVisible) 34f else 28f, animationSpec = tween(durationMillis = 300),
        label = "TextSize Animation"
    )
    val subTextSize by animateFloatAsState(targetValue = if (isImeVisible) 20f else 16f, animationSpec = tween(durationMillis = 300),
        label = "subTextSize Animation"
    )
    val spacerHeight by animateDpAsState(targetValue = if (isImeVisible) 0.dp else 20.dp, animationSpec = tween(durationMillis = 300),
        label = "SpacerHeight Animation"
    )
    val subSpacerHeight by animateDpAsState(targetValue = if (isImeVisible) 4.dp else 20.dp, animationSpec = tween(durationMillis = 300),
        label = "subSpacerHeight Animation"
    )

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> {
                navController.navigate(Screens.MainMenu.name) {
                    popUpTo(Screens.LoginScreen.name) { inclusive = true }
                }
            }
            else -> Unit
        }
    }

    LaunchedEffect(Unit) {
        authViewModel.resetUiState()
    }

    LaunchedEffect(Unit) {
        launch {
            authViewModel.authEvent.collect { event ->
                Log.d("ToastDebug", "Collected event: $event")
                when (event) {
                    is AuthEvent.Info -> {
                        Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                        authViewModel.resetToastFlag()
                    }
                    is AuthEvent.Error -> {
                        Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                        authViewModel.resetToastFlag()
                    }
                }
            }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                colors = TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                    scrolledContainerColor = MaterialTheme.colorScheme.onBackground,
                ),
                modifier = modifier.height(52.dp)
            )
        }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
        ) {

            Crossfade(targetState = isImeVisible, label = "Image change animation") { isKeyboardVisible ->
                Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    when (isKeyboardVisible) {
                        false -> {
                            Image(
                                painter = painterResource(id = if (isSystemInDarkTheme()) R.drawable.full_logo_white else R.drawable.full_logo_black),
                                contentDescription = "Default Logo",
                                modifier = Modifier
                                    .size(logoSize)
                                    .align(Alignment.Center)
                            )
                        }
                        true -> {
                            Image(
                                painter = painterResource(id = if (isSystemInDarkTheme()) R.drawable.medusa_white else R.drawable.medusa_black),
                                contentDescription = "Keyboard Active Logo",
                                modifier = Modifier
                                    .size(logoSize)
                                    .align(Alignment.Center)
                            )
                        }
                    }
                }
            }

            Spacer(modifier.height(28.dp))
            val visibleState = remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                visibleState.value = true
            }
            AnimatedVisibility(
                visible = visibleState.value,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = fadeOut()
            ){
                Crossfade(targetState = isImeVisible, label = "Welcome Screen Label") {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier.height(spacerHeight))
                    Text(
                        text = "Welcome Back!",
                        fontWeight = if (!it) FontWeight.Bold else FontWeight.Light,
                        fontSize = textSize.sp,
                        color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier,
                    )
                    Text(
                        text = "Login to your account",
                        fontWeight = if (it) FontWeight.Bold else FontWeight.Light,
                        fontSize = subTextSize.sp,
                        color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                    )
                    Spacer(modifier.height(subSpacerHeight))
                }
            }}

            Spacer(modifier.height(20.dp))
            //=================================--> Email
            OutlinedTextField(
                value = uiState.loginEmail,
                onValueChange = { authViewModel.onEmailChange(it) },
                label = { Text(text = "Email") },
                modifier = Modifier
                    .focusRequester(usernameFocusRequester)
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            authViewModel.onLoginEmailFocusChanged(focusState.isFocused)
                        }
                    },
                trailingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = "Email Icon") },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next, keyboardType = KeyboardType.Email),
                keyboardActions = KeyboardActions(onNext = { passwordFocusRequester.requestFocus() }),
                singleLine = true,
                isError = uiState.loginEmailError && uiState.loginEmail.isNotBlank(),
                supportingText = {
                    if (uiState.loginEmailError && uiState.loginEmail.isNotBlank()) {
                        Text(text = "Invalid email format", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            //====================================--> Password
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { authViewModel.onSignInPasswordChange(it) },
                label = { Text(text = "Password") },
                modifier = Modifier.focusRequester(passwordFocusRequester),
                trailingIcon = {
                    IconButton(onClick = { authViewModel.togglePasswordVisibility() }) {
                        Icon(
                            imageVector = if (uiState.isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (uiState.isPasswordVisible) "Hide Password" else "Show Password"
                        )
                    }
                },
                visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Password,
                    autoCorrectEnabled = false
                ),
                keyboardActions = KeyboardActions(onDone = { authViewModel.login(uiState.loginEmail, uiState.password) }),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            //====================================--> Invalid Credentials Text

            /*TODO Create a supprting text for invalid credentials*/

            //====================================--> Login Button
            Spacer(Modifier.height(16.dp).fillMaxWidth())
            Button(
                onClick = { authViewModel.login(uiState.loginEmail, uiState.password) },
                enabled = uiState.isLogInFormValid && authState.value != AuthState.Loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = if (isSystemInDarkTheme()) Gray900 else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledContentColor = if (isSystemInDarkTheme()) Gray800 else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                ),
            ) {
                if (authState.value == AuthState.Loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(text = "Login")
                }
            }

            Box(Modifier.fillMaxSize()) {
                Column(Modifier.fillMaxWidth().align(Alignment.BottomCenter), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Forgot Password",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.clickable(onClick = { navController.navigate(Screens.ForgotPasswordScreen.name) })
                    )
                    Row {
                        Text(
                            text = "Don't have an account?",
                            color = Color.Gray,
                            fontSize = 12.sp,
                        )
                        Text(
                            text = " Sign Up",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp,
                            modifier = Modifier.clickable(onClick = {
                                navController.navigate(Screens.SignUpScreen.name)
                                authViewModel.resetUiState()
                            })
                        )
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    val navController = rememberNavController()
    LENATheme(darkTheme = true) {
        // Provide mock dependencies to AuthViewModel
        val authViewModel = AuthViewModel()
        LoginScreen(navController, authViewModel, modifier = Modifier)
    }
}