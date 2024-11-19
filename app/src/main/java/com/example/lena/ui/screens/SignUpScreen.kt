package com.example.lena.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import com.example.lena.Screens
import com.example.lena.viewModels.SignUpViewModel
import com.example.lena.ui.rememberImeState
import com.example.lena.ui.theme.LENATheme

@Composable
fun SignUpScreen(
    navController: NavController,
    viewModel: SignUpViewModel = viewModel(),
    modifier: Modifier = Modifier) {



    var isRePasswordFocused by remember { mutableStateOf(false) }
    val isImeVisible by rememberImeState()
    val focusManager = LocalFocusManager.current
    val usernameFocusRequester = FocusRequester()
    val emailFocusRequester = FocusRequester()
    val passwordFocusRequester = FocusRequester()
    val repeatPasswordFocusRequester = FocusRequester()
    val keyboardController = LocalSoftwareKeyboardController.current
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier
        .fillMaxWidth()
        .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }, horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = modifier
            .height(80.dp)
            .fillMaxWidth())

        Crossfade(targetState = isImeVisible, label = "Welcome Screen Label") {
            Column(modifier.align(Alignment.CenterHorizontally)) {
                Spacer(modifier.height(if (it) 0.dp else 40.dp))
                Text(
                    text = "Hey There!",
                    fontWeight = if (!it) FontWeight.Bold else FontWeight.Light,
                    fontSize = if (it) 32.sp else 28.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)

                )
                Text(
                    text = "Let's create an account!",
                    fontWeight = if (it) FontWeight.Bold else FontWeight.Light,
                    fontSize = if (it) 20.sp else 16.sp,
                    modifier = Modifier
                )
            }
            Spacer(modifier.height(if (it) 4.dp else 20.dp))
        }


        Box(modifier = modifier
        ){
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
        OutlinedTextField(//========================================--> Username
            value = uiState.username,
            onValueChange =
            { viewModel.onUsernameChange(it) },
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
                onNext = { emailFocusRequester.requestFocus() }
            ),
            isError = uiState.userNameError,
            singleLine = true,

            supportingText = {if (uiState.userNameError) {
                Text(
                    text = "Username cannot be empty",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start))
            }}
        )
        //========================================--> Email
        OutlinedTextField(
            value = uiState.email,
            onValueChange = {
                viewModel.onEmailChange(it)

                            },
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
                imeAction = ImeAction.Next
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
            }
)
        //========================================--> Password
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
                    imeAction = ImeAction.Next
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
                }
            )
        //========================================--> Re-enter password
            OutlinedTextField(
                value = uiState.rePassword,
                onValueChange = { viewModel.onRePasswordChange(it) },
                label = { Text(text = "Repeat password") },
                modifier = Modifier
                    .focusRequester(repeatPasswordFocusRequester)
                    .onFocusChanged { focusState ->
                        isRePasswordFocused = focusState.isFocused
                        // Check and show an error only if the field loses focus and does not match the password
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
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        viewModel.signUp { }
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
                }
            )
            //================================================-->Success Message
        }
        } //--> End of Column layout

        AnimatedVisibility(
            visible = uiState.successfulSignUp,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success Icon",
                    tint = Color(0xFF4CAF50), // Match with the text color
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sign up successful!",
                    color = Color(0xFF4CAF50),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }

        //================================================-->Button
        Button(
            onClick = {
                viewModel.signUp {}
                keyboardController?.hide()
            },
            enabled = uiState.isFormValid,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            modifier = modifier.padding(8.dp)

        ) {
            Text(text = "Sign Up")
        }

        Row{
            Text(
                text = "Already have an account?",
                color = Color.Gray
            )
            Text(
                text = " Sign In",
                modifier = Modifier.clickable(
                    onClick = { navController.navigate(Screens.LoginScreen.name) }))
        }

    }
}


@Preview (showSystemUi = true)
@Composable
fun SignUpScreenPreview(){
    LENATheme {
        SignUpScreen(navController = rememberNavController())
    }
}