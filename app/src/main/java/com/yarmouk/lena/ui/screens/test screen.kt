package com.yarmouk.lena.ui.screens

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
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.yarmouk.lena.ui.rememberImeState
import com.yarmouk.lena.ui.theme.Gray800
import com.yarmouk.lena.ui.theme.Gray900
import com.yarmouk.lena.ui.theme.LENATheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpTestScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var isRePasswordFocused by remember { mutableStateOf(false) }
    val isImeVisible by rememberImeState()
    val focusManager = LocalFocusManager.current
    val usernameFocusRequester = FocusRequester()
    val firstNameFocusRequester = FocusRequester()
    val lastNameFocusRequester = FocusRequester()
    val emailFocusRequester = FocusRequester()
    val passwordFocusRequester = FocusRequester()
    val repeatPasswordFocusRequester = FocusRequester()
    val keyboardController = LocalSoftwareKeyboardController.current
    //===================================
    //val uiState by viewModel.collectAsState()
    //===================================
    val context = LocalContext.current
    //val authState = viewModel.authState.observeAsState()
    var email by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rePassword by remember { mutableStateOf("") }

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
            //Spacer(modifier = modifier.height(firstSpacerHeight).fillMaxWidth())

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
            Spacer(modifier = modifier
                .height(firstSpacerHeight)
                .fillMaxWidth())
            Box(modifier = modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
//=============================================================================-----> Full Name field
                    Box(modifier = Modifier
                        .width(IntrinsicSize.Min)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = firstName,
                                onValueChange = { firstName = it },
                                label = { Text(text = "First Name", fontSize = 12.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(firstNameFocusRequester),
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

                                )
                            OutlinedTextField(
                                value = lastName,
                                onValueChange = { lastName = it },
                                label = { Text(text = "Last Name", fontSize = 12.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(lastNameFocusRequester),
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { usernameFocusRequester.requestFocus() }
                                ),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),

                                )
                        }
                    }
//=============================================================================-----> Username field
//=============================================================================-----> Email field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(text = "Email") },
                        modifier = Modifier
                            .focusRequester(emailFocusRequester),
                        trailingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = "Email Icon") },
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next, keyboardType = KeyboardType.Email),
                        keyboardActions = KeyboardActions(onNext = { passwordFocusRequester.requestFocus() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
//=============================================================================-----> Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(text = "Password") },
                        modifier = Modifier
                            .focusRequester(passwordFocusRequester),

                        //visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Password,
                            autoCorrectEnabled = false
                        ),
                        keyboardActions = KeyboardActions(onNext = { repeatPasswordFocusRequester.requestFocus() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
//=============================================================================-----> Re-enter Password field
                    OutlinedTextField(
                        value = rePassword,
                        onValueChange = { rePassword = it },
                        label = { Text(text = "Repeat password") },
                        modifier = Modifier
                            .focusRequester(repeatPasswordFocusRequester),

                        //visualTransformation = if (isRePasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction =  ImeAction.Done,
                            keyboardType = KeyboardType.Password,
                            autoCorrectEnabled = false
                        ),

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
                    keyboardController?.hide()
                },
                enabled = true,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = if (isSystemInDarkTheme()) Gray900 else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledContentColor = if (isSystemInDarkTheme()) Gray800 else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                ),
                modifier = modifier.padding(8.dp)
            ) {
                if (false) {
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
                        onClick = {},
                    )
                )
            }
        }
    }
}

@Preview
@Composable
fun TestPreview(){
    LENATheme {
        val nav = rememberNavController()
        SignUpTestScreen(nav)
    }
}