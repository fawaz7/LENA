package com.example.lena

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.lena.ui.theme.LENATheme

@Composable
fun LoginScreen(navController: NavController,modifier: Modifier = Modifier) {

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var valid by remember { mutableStateOf(true) }
    val usernameFocusRequester = FocusRequester()
    val passwordFocusRequester = FocusRequester()
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = modifier.height(90.dp).fillMaxWidth())
        Image(
            painter = painterResource(id = R.drawable.full_logo_black),
            contentDescription = "App Logo",
            modifier = Modifier.size(270.dp).fillMaxWidth().align(Alignment.CenterHorizontally),
        )
        Spacer(modifier.height(50.dp))
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
            onValueChange = { username = it },
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
            onValueChange = { password = it },
            label = { Text(text = "Password") },
            modifier = Modifier.focusRequester(passwordFocusRequester),
            trailingIcon = {Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Password Icon"
            )},
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done // "Done" to submit the form
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (username == "fawaz" && password == "fawaz") {
                        keyboardController?.hide()
                        navController.navigate(Screens.MainMenu.name)
                        valid = true
                    } else {
                        keyboardController?.hide()
                        valid = false
                    }
                }
            )
        )
        if (!valid){
            Text(
                text = stringResource(R.string.invalid_credentials),
                color = Color.Red,
                modifier = Modifier
            )
        }


        Spacer(modifier.height(25.dp))

        Button(
            onClick = {
                if(username.equals("fawaz") && password.equals("fawaz")){
                    navController.navigate(Screens.MainMenu.name)
                    valid = true
                }
                else{
                    valid = false
                }
            },
            enabled = if(username.isNotBlank() && password.isNotBlank()){true} else {false}

            ) {
            Text(text = "Login")
        }
        Text(
            text = "Forgot Password?",
            modifier = Modifier.clickable(
                onClick = { /*TODO*/ }
            )
        )
        Row{
            Text(
                text = "Don't have an account?",
                color = Color.Gray
            )
            Text(
                text = " Sign Up",
                modifier = Modifier.clickable(
                    onClick = { /*TODO*/ }))
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