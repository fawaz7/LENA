package com.example.lena.ui.screens

import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.lena.R
import com.example.lena.Screens
import com.example.lena.ui.rememberImeState
import com.example.lena.ui.theme.Gray800
import com.example.lena.ui.theme.Gray900
import com.example.lena.ui.theme.LENATheme
import com.example.lena.viewModels.AuthState
import com.example.lena.viewModels.AuthViewModel
import com.example.lena.viewModels.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel ,
    modifier: Modifier = Modifier) {

   // val uiState by authViewModel.uiState.collectAsState()
    val isImeVisible by rememberImeState()
    val focusManager = LocalFocusManager.current
    val usernameFocusRequester = FocusRequester()
    val passwordFocusRequester = FocusRequester()
    val context = LocalContext.current
    val authState = authViewModel.authState.observeAsState()

    val logoSize by animateDpAsState(targetValue = if (isImeVisible) 80.dp else 270.dp, animationSpec = tween(durationMillis = 300),
        label = "LogoSize Animation"
    )
    val textSize by animateFloatAsState(targetValue = if (isImeVisible) 34f else 28f, animationSpec = tween(durationMillis = 300),
        label = "TextSize Animation"
    )
    val subTextSize by animateFloatAsState(targetValue = if (isImeVisible) 20f else 16f, animationSpec = tween(durationMillis = 300),
        label = "subTextSize Animation"
    )
    val spacerHeight by animateDpAsState(targetValue = if (isImeVisible) 0.dp else 40.dp, animationSpec = tween(durationMillis = 300),
        label = "SpacerHeight Animation"
    )
    val subSpacerHeight by animateDpAsState(targetValue = if (isImeVisible) 4.dp else 20.dp, animationSpec = tween(durationMillis = 300),
        label = "subSpacerHeight Animation"
    )

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> {
                navController.navigate(Screens.MainMenu.name)
            }
            is AuthState.Error -> {
                Toast.makeText(context, (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT).show()
            }
            else -> Unit

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
                modifier = modifier.height(64.dp)
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
                                modifier = Modifier.size(logoSize).align(Alignment.Center)
                            )
                        }
                        true -> {
                            Image(
                                painter = painterResource(id = if (isSystemInDarkTheme()) R.drawable.medusa_white else R.drawable.medusa_black),
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
            }

            Spacer(modifier.height(20.dp))
            //=================================--> Username
            var email by remember { mutableStateOf("") }
            OutlinedTextField(
                value = email,

                onValueChange = {
                    email = it
                },
                label = { Text(text = "Username") },
                modifier = Modifier
                    .focusRequester(usernameFocusRequester)
                    /*
                    .onFocusChanged { focusState ->
                    if (!focusState.isFocused) {
                        authViewModel.onUsernameChange(uiState.username.trim())
                    }
                }*/,
                trailingIcon = { Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Username Icon"
                )
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        passwordFocusRequester.requestFocus()

                    }
                ),

                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            //====================================--> Password
            var password by remember { mutableStateOf("") }
            OutlinedTextField(

                value = password,
                onValueChange = { password = it },
                label = { Text(text = "Password") },
                modifier = Modifier.focusRequester(passwordFocusRequester),
//                trailingIcon = {
//                    IconButton(onClick = { authViewModel.togglePasswordVisibility() }) {
//                        Icon(
//                            imageVector = if (uiState.isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
//                            contentDescription = if (uiState.isPasswordVisible) "Hide Password" else "Show Password"
//                        )
//                    }
//                },
               // visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Password,
                    autoCorrectEnabled = false
                ),
                keyboardActions =
                KeyboardActions(
                    onDone = {
                        authViewModel.login(email,password)
                    }
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
//            uiState.error?.let { error ->
//                Text(text = error,
//                    color = MaterialTheme.colorScheme.error,
//                    style = MaterialTheme.typography.bodySmall,
//                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp))
//            }

            Button(
                onClick = {
                    authViewModel.login(email,password)
                },
                enabled = true,/*authViewModel.validateInputs(),*/
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = Gray900,
                    disabledContentColor = Gray800,
                ),
//                modifier = modifier.padding(if (uiState.isValid) 8.dp else {0.dp})

            ) {
                Text(text = "Login")
            }

            Text(
                text = "Forgot Password",
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.clickable(onClick = { /*TODO*/ })
            )
            Row {
                Text(
                    text = "Don't have an account?",
                    color = Color.Gray
                )
                Text(
                    text = " Sign Up",
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable(onClick = { navController.navigate(Screens.SignUpScreen.name) })
                )
            }
        }
    }
}

@Preview(showSystemUi = true )
@Composable
fun LoginScreenPreview(){
    LENATheme(darkTheme = true) {
        LoginScreen(navController = rememberNavController(), AuthViewModel())
    }
}