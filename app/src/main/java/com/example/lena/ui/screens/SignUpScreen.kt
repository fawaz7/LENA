package com.example.lena.ui.screens

import android.R.attr.top
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.lena.Screens
import com.example.lena.ViewModels.SignUpViewModel
import com.example.lena.ui.theme.LENATheme

@Composable
fun SignUpScreen(
    navController: NavController,
    viewModel: SignUpViewModel = viewModel(),
    modifier: Modifier = Modifier) {

    var username by remember { mutableStateOf(viewModel.username) }
    var email by remember { mutableStateOf(viewModel.email) }
    var password by remember { mutableStateOf(viewModel.password) }
    var re_password by remember { mutableStateOf( viewModel.re_password ) }
    var successfulSignUp by remember { mutableStateOf(viewModel.valid) }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }
    var userNameError by remember { mutableStateOf(false) }
    var repeatPasswordError by remember { mutableStateOf(false) }


    val focusManager = LocalFocusManager.current
    val usernameFocusRequester = FocusRequester()
    val emailFocusRequester = FocusRequester()
    val passwordFocusRequester = FocusRequester()
    val repeatPasswordFocusRequester = FocusRequester()

    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = Modifier.fillMaxWidth().pointerInput(Unit){detectTapGestures(onTap = {focusManager.clearFocus()})}, horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = modifier.height(90.dp).fillMaxWidth())

        Spacer(modifier.height(50.dp))
        Text(
            text = "Hey There!",
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            modifier = Modifier

        )
        Text(
            text = "Let's create an account!",
            modifier = Modifier
        )
        Spacer(modifier.height(20.dp))

        Box(modifier = modifier
        ){
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
        OutlinedTextField(//========================================--> Username
            value = username,
            onValueChange =
            {
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
                onNext = { emailFocusRequester.requestFocus() }
            ),
            isError = userNameError,
            supportingText = {if (userNameError) {
                Text(
                    text = "Username cannot be empty",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start))
            }}
        )
        //========================================--> Email
        OutlinedTextField(
    value = email,
    onValueChange = {
        email = it
        viewModel.email = it
        emailError = if (!viewModel.validateEmail(email)) {
            "Invalid email address"
        } else {
            ""
        }
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
    isError = emailError.isNotEmpty(),
    supportingText = {
        if (emailError.isNotEmpty()) {
            Text(
                text = emailError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
)
        //========================================--> Password
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    viewModel.password = it
                },
                label = { Text(text = "Password") },
                modifier = Modifier
                    .focusRequester(passwordFocusRequester)
                    .onFocusChanged { focusState ->
                        // Validate the password when the field loses focus
                        if (!focusState.hasFocus) {
                            passwordError = !viewModel.validatePassword(password)
                        }
                    },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password Icon"
                    )
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { repeatPasswordFocusRequester.requestFocus() }
                ),
                isError = passwordError,
                supportingText = {
                    if (passwordError) {
                        Text(
                            text = "Password must be at least 8 characters",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    }
                }
            )
        //========================================--> Re-enter password
            OutlinedTextField(
                value = re_password,
                onValueChange = {
                    re_password = it
                    viewModel.re_password = it
                    repeatPasswordError = re_password != password && re_password.isNotEmpty()
                },
                label = { Text(text = "Repeat password") },
                modifier = Modifier.focusRequester(repeatPasswordFocusRequester),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password Icon"
                    )
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        viewModel.signUp {
                            successfulSignUp = true
                        }
                        keyboardController?.hide()
                    }
                ),
                isError = repeatPasswordError,
                supportingText = {
                    if (repeatPasswordError) {
                        Text(
                            text = "Passwords do not match",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    }
                }
            )
        //========================================
        }
        }

        if(successfulSignUp){

            Text(
                text = "Sign up successful!",
                color = Color.Green,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
        } else {
            viewModel.error?.let { error ->
                Text(
                    text = error, color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp).align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.bodySmall)
                successfulSignUp = false
            }
        }
        Button(
            onClick = {
                viewModel.signUp {
                    successfulSignUp = true
                }
                keyboardController?.hide()
            },
            enabled = viewModel.validateForms(), //TO-DO: add validation
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black // Change this to your desired color
            ),
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