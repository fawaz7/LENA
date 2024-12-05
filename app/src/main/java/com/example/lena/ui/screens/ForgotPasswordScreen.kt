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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Email
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
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
fun ForgotPasswordScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier

){
    val isImeVisible by rememberImeState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val resetEmailFocusRequester = FocusRequester()

    val uiState by authViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val authState = authViewModel.authState.observeAsState()

    val textSize by animateFloatAsState(targetValue = if (isImeVisible) 32f else 28f, animationSpec = tween(durationMillis = 300),
        label = "TextSize Animation")
    val subTextSize by animateFloatAsState(targetValue = if (isImeVisible) 20f else 16f, animationSpec = tween(durationMillis = 300),
        label = "subTextSize Animation")
    val firstSpacerHeight by animateDpAsState(targetValue = if (isImeVisible) 8.dp else 24.dp, animationSpec = tween(durationMillis = 300),
        label = "firstSpacerHeight Animation")

    LaunchedEffect(Unit) {
        authViewModel.resetToastFlag()
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
                    AuthEvent.None -> {}
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
                modifier = modifier.height(64.dp),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            val visibleState = remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                visibleState.value = true
            }
            AnimatedVisibility(
                visible = visibleState.value,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = fadeOut()
            ){
                Crossfade(targetState = isImeVisible, label = "Welcome Screen Label Animation") { isKeyboardVisible ->
                Column(modifier.align(Alignment.CenterHorizontally).padding(horizontal = 32.dp)) {
                    Text(
                        text = "Forgot Password?",
                        fontWeight = if (!isKeyboardVisible) FontWeight.Bold else FontWeight.Light,
                        fontSize = textSize.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Text(
                        text = "Enter your email address and we'll fix that for you",
                        fontWeight = if (isKeyboardVisible) FontWeight.Bold else FontWeight.Light,
                        fontSize = subTextSize.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                    )
                }
            }
        }

            Spacer(modifier = modifier.height(firstSpacerHeight).fillMaxWidth())
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                OutlinedTextField(
                    value = uiState.forgotPasswordEmail,
                    onValueChange = { authViewModel.onForgotPasswordEmailChange(it) },
                    label = { Text(text = "Email") },
                    modifier = Modifier
                        .focusRequester(resetEmailFocusRequester)
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused) {
                                authViewModel.onForgotPasswordEmailFocusChanged(focusState.isFocused)
                            }
                        },
                    trailingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = "Email Icon") },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done, keyboardType = KeyboardType.Email),
                    keyboardActions = KeyboardActions(onDone = {
                        authViewModel.onForgotPasswordEmailFocusChanged(false)
                        if (uiState.forgotPasswordEmail.isBlank()) {
                            authViewModel.setForgotEmailError("Email is required")
                        } else if (!authViewModel.validateEmail(uiState.forgotPasswordEmail)) {
                            authViewModel.setForgotEmailError("Invalid email address")
                        } else {
                            authViewModel.resetPassword(uiState.forgotPasswordEmail)
                            keyboardController?.hide()
                        }
                    }),
                    isError = uiState.forgotPasswordEmailError && uiState.forgotPasswordEmail.isNotBlank(),
                    singleLine = true,
                    supportingText = {
                        if (uiState.forgotPasswordEmailError && uiState.forgotPasswordEmail.isNotBlank()) {
                            Text(
                                text = uiState.forgotPasswordErrorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                //=======================================================================================---> End of Text Field
                Button(
                    onClick = {
                        authViewModel.onForgotPasswordEmailFocusChanged(false)
                        if (uiState.forgotPasswordEmail.isBlank()) {
                            authViewModel.setForgotEmailError("Email is required")
                        } else if (!authViewModel.validateEmail(uiState.forgotPasswordEmail)) {
                            authViewModel.setForgotEmailError("Invalid email address")
                        } else {
                            authViewModel.resetPassword(uiState.forgotPasswordEmail)
                            keyboardController?.hide()
                        }
                    },
                    enabled = uiState.forgotPasswordEmail.isNotBlank() && !uiState.forgotPasswordEmailError,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = if (isSystemInDarkTheme()) Gray900 else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        disabledContentColor = if (isSystemInDarkTheme()) Gray800 else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    ),
                    modifier = modifier.width(124.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (authState.value == AuthState.Loading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(text = "Reset")
                    }
                }

                authState.value?.let { state ->
                    when (state) {
                        is AuthState.Unauthenticated -> {
                            if (uiState.forgotPasswordSuccess) {
                                Toast.makeText(context, "Password reset email sent successfully", Toast.LENGTH_LONG).show()
                                navController.navigate("LoginScreen") {
                                    popUpTo("ForgotPasswordScreen") { inclusive = true }
                                }
                                authViewModel.resetUiState()
                            }
                        }
                        else -> {}
                    }
                }
            }

        }

    }

}

@Preview(showSystemUi = true)
@Composable
fun ForgotPassPreview(){
    LENATheme(darkTheme = true) {
        ForgotPasswordScreen(navController = rememberNavController(), AuthViewModel())

    }
}