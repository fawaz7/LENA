package com.example.lena.ui.screens

import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import com.example.lena.Screens
import com.example.lena.ui.rememberImeState
import com.example.lena.ui.theme.Gray800
import com.example.lena.ui.theme.Gray900
import com.example.lena.ui.theme.LENATheme
import com.example.lena.viewModels.SignUpViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    navController: NavController,
    viewModel: SignUpViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var isRePasswordFocused by remember { mutableStateOf(false) }
    val isImeVisible by rememberImeState()
    val focusManager = LocalFocusManager.current
    val usernameFocusRequester = FocusRequester()
    val emailFocusRequester = FocusRequester()
    val passwordFocusRequester = FocusRequester()
    val repeatPasswordFocusRequester = FocusRequester()
    val keyboardController = LocalSoftwareKeyboardController.current
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val textSize by animateFloatAsState(targetValue = if (isImeVisible) 32f else 28f, animationSpec = tween(durationMillis = 300),
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
    val firstSpacerHeight by animateDpAsState(targetValue = if (isImeVisible) 20.dp else 80.dp, animationSpec = tween(durationMillis = 300),
        label = "firstSpacerHeight Animation"
    )

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
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = modifier.height(firstSpacerHeight).fillMaxWidth())

            Crossfade(targetState = isImeVisible, label = "Welcome Screen Label") { isKeyboardVisible ->
                Column(modifier.align(Alignment.CenterHorizontally)) {
                    Spacer(modifier.height(spacerHeight))
                    Text(
                        text = "Hey There!",
                        fontWeight = if (!isKeyboardVisible) FontWeight.Bold else FontWeight.Light,
                        fontSize = textSize.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Text(
                        text = "Let's create an account!",
                        fontWeight = if (isKeyboardVisible) FontWeight.Bold else FontWeight.Light,
                        fontSize = subTextSize.sp,
                        modifier = Modifier
                    )
                }
                Spacer(modifier.height(subSpacerHeight))
            }

            Box(modifier = modifier) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = uiState.username,
                        onValueChange = { viewModel.onUsernameChange(it) },
                        label = { Text(text = "Username") },
                        modifier = Modifier
                            .focusRequester(usernameFocusRequester)
                            ,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Username Icon"
                            )
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { emailFocusRequester.requestFocus() }
                        ),
                        isError = uiState.userNameError,
                        singleLine = true,
                        supportingText = {
                            if (uiState.userNameError) {
                                Text(
                                    text = "Username cannot be empty",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.align(Alignment.Start)
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.onEmailChange(it) },
                        label = { Text(text = "Email") },
                        modifier = Modifier
                            .focusRequester(emailFocusRequester),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email Icon"
                            )
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Email
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { passwordFocusRequester.requestFocus() }
                        ),
                        isError = uiState.emailError,
                        singleLine = true,
                        supportingText = {
                            if (uiState.emailError) {
                                Text(
                                    text = "Invalid Email Address",
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

                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = { viewModel.onPasswordChange(it) },
                        label = { Text(text = "Password") },
                        modifier = Modifier
                            .focusRequester(passwordFocusRequester)
                            .onFocusChanged { focusState ->
                                if (!focusState.isFocused) {
                                    viewModel.validatePassword(uiState.password)
                                }
                            },
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
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Password,
                            autoCorrectEnabled = false
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { repeatPasswordFocusRequester.requestFocus() }
                        ),
                        isError = uiState.passwordError,
                        supportingText = {
                            if (uiState.passwordError) {
                                Text(
                                    text = "Password must be at least 8 characters, contain one uppercase letter, and one number",
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

                    OutlinedTextField(
                        value = uiState.rePassword,
                        onValueChange = { viewModel.onRePasswordChange(it) },
                        label = { Text(text = "Repeat password") },
                        modifier = Modifier
                            .focusRequester(repeatPasswordFocusRequester)
                            .onFocusChanged { focusState ->
                                isRePasswordFocused = focusState.isFocused
                                if (!focusState.isFocused) {
                                    viewModel.onRePasswordChange(uiState.rePassword)
                                }
                            },
                        trailingIcon = {
                            IconButton(onClick = { viewModel.toggleRePasswordVisibility() }) {
                                Icon(
                                    imageVector = if (uiState.isRePasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (uiState.isRePasswordVisible) "Hide Password" else "Show Password"
                                )
                            }
                        },
                        visualTransformation = if (uiState.isRePasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Password,
                            autoCorrectEnabled = false
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                viewModel.signUp {}
                                keyboardController?.hide()
                            }
                        ),
                        isError = uiState.repeatPasswordError,
                        supportingText = {
                            if (uiState.repeatPasswordError) {
                                Text(
                                    text = "Passwords do not match",
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
                }
            }

            Button(
                onClick = {
                    viewModel.signUp {
                        Toast.makeText(context, "Sign up successful!", Toast.LENGTH_SHORT).show()
                    }
                    keyboardController?.hide()
                },
                enabled = uiState.isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = Gray900,
                    disabledContentColor = Gray800,
                ),
                modifier = modifier.padding(8.dp)

            ) {
                Text(text = "Sign Up")
            }

            Row {
                Text(
                    text = "Already have an account?",
                    color = Color.Gray
                )
                Text(
                    text = " Sign In",
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable(
                        onClick = { navController.navigate(Screens.LoginScreen.name) },
                    )
                )
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun SignUpScreenPreview() {
    LENATheme(darkTheme = true) {
        SignUpScreen(navController = rememberNavController())
    }
}