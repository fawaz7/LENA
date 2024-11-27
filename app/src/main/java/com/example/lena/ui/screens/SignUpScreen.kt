package com.example.lena.ui.screens

import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.ArrowBackIosNew
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
import com.example.lena.viewModels.AuthState
import com.example.lena.viewModels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    navController: NavController,
    viewModel: AuthViewModel ,
    modifier: Modifier = Modifier
) {
    var isRePasswordFocused by remember { mutableStateOf(false) }
    val isImeVisible by rememberImeState()
    val focusManager = LocalFocusManager.current
    val firstNameFocusRequester = FocusRequester()
    val lastNameFocusRequester = FocusRequester()
    val emailFocusRequester = FocusRequester()
    val passwordFocusRequester = FocusRequester()
    val repeatPasswordFocusRequester = FocusRequester()
    val keyboardController = LocalSoftwareKeyboardController.current
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val authState = viewModel.authState.observeAsState()


    val textSize by animateFloatAsState(targetValue = if (isImeVisible) 32f else 28f, animationSpec = tween(durationMillis = 300),
        label = "TextSize Animation")
    val subTextSize by animateFloatAsState(targetValue = if (isImeVisible) 20f else 16f, animationSpec = tween(durationMillis = 300),
        label = "subTextSize Animation")
    val spacerHeight by animateDpAsState(targetValue = if (isImeVisible) 0.dp else 40.dp, animationSpec = tween(durationMillis = 300),
        label = "SpacerHeight Animation")
    val subSpacerHeight by animateDpAsState(targetValue = if (isImeVisible) 4.dp else 20.dp, animationSpec = tween(durationMillis = 300),
        label = "subSpacerHeight Animation")
    val firstSpacerHeight by animateDpAsState(targetValue = if (isImeVisible) 20.dp else 80.dp, animationSpec = tween(durationMillis = 300),
        label = "firstSpacerHeight Animation")

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

    LaunchedEffect(Unit) {
        viewModel.resetUiState()
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
                .fillMaxWidth()
                .padding(innerPadding)
                .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = modifier.height(firstSpacerHeight).fillMaxWidth())

            Crossfade(targetState = isImeVisible, label = "Welcome Screen Label Animation") { isKeyboardVisible ->
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
            Spacer(modifier = modifier.height(firstSpacerHeight).fillMaxWidth())
            Box(modifier = modifier) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
//=============================================================================-----> Full Name field
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.firstName,
                            onValueChange = { viewModel.onFirstNameChange(it) },
                            label = { Text(text = "First Name", fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(firstNameFocusRequester)
                                .onFocusChanged { focusState ->
                                    if (!focusState.isFocused) {
                                        viewModel.onFirstNameFocusChanged(focusState.isFocused)
                                    }
                                },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { lastNameFocusRequester.requestFocus() }
                            ),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            isError = uiState.firstNameError,
                            supportingText = {
                                if (uiState.firstNameError) {
                                    Text(
                                        text = uiState.firstNameErrorMessage,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            },
                        )
                        OutlinedTextField(
                            value = uiState.lastName,
                            onValueChange = { viewModel.onLastNameChange(it) },
                            label = { Text(text = "Last Name", fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(lastNameFocusRequester)
                                .onFocusChanged { focusState ->
                                    if (!focusState.isFocused) {
                                        viewModel.onLastNameFocusChanged(focusState.isFocused)
                                    }
                                },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { emailFocusRequester.requestFocus() }
                            ),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            isError = uiState.lastNameError,
                            supportingText = {
                                if (uiState.lastNameError) {
                                    Text(
                                        text = uiState.lastNameErrorMessage,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            },

                        )
                    }
//=============================================================================-----> Username field
//=============================================================================-----> Email field
                    OutlinedTextField(
                        value = uiState.signUpEmail,
                        onValueChange = { viewModel.onSignUpEmailChange(it) },
                        label = { Text(text = "Email") },
                        modifier = Modifier
                            .focusRequester(emailFocusRequester)
                            .onFocusChanged { focusState ->
                                if (!focusState.isFocused) {
                                    viewModel.onSignUpEmailFocusChanged(focusState.isFocused)
                                }
                            },
                        trailingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = "Email Icon") },
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next, keyboardType = KeyboardType.Email),
                        keyboardActions = KeyboardActions(onNext = { passwordFocusRequester.requestFocus() }),
                        isError = uiState.signUpEmailError && uiState.signUpEmail.isNotBlank(),
                        singleLine = true,
                        supportingText = {
                            if (uiState.signUpEmailError && uiState.signUpEmail.isNotBlank()) {
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
//=============================================================================-----> Password field
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = { viewModel.onSignUpPasswordChange(it) },
                        label = { Text(text = "Password") },
                        modifier = Modifier
                            .focusRequester(passwordFocusRequester)
                            .onFocusChanged { focusState ->
                                viewModel.onPasswordFocusChanged(focusState.isFocused)
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
                        keyboardActions = KeyboardActions(onNext = { repeatPasswordFocusRequester.requestFocus() }),
                        isError = uiState.passwordError,
                        supportingText = {
                            if (uiState.passwordError) {
                                Text(
                                    text = uiState.signUpPasswordErrorMessage,
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
//=============================================================================-----> Re-enter Password field
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
                            imeAction = if (uiState.isSignUpFormValid) ImeAction.Send else ImeAction.Done,
                            keyboardType = KeyboardType.Password,
                            autoCorrectEnabled = false
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            if (uiState.isSignUpFormValid) {
                                viewModel.signUp()
                                keyboardController?.hide()
                            } else {
                                keyboardController?.hide()
                            }
                        }),
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
//=============================================================================-----> Finish the Sign Up
            Button(
                onClick = {
                    viewModel.signUp()
                    keyboardController?.hide()
                },
                enabled = uiState.isSignUpFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = if (isSystemInDarkTheme()) Gray900 else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledContentColor = if (isSystemInDarkTheme()) Gray800 else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                ),
                modifier = modifier.padding(8.dp)
            ) {
                if (authState.value == AuthState.Loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(text = "Sign Up")
                }
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
                        onClick = {
                            navController.navigate(Screens.LoginScreen.name)
                            viewModel.resetUiState()},
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
        SignUpScreen(navController = rememberNavController(), viewModel = viewModel())
    }
}