package com.example.lena.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.lena.R
import com.example.lena.ui.rememberImeState
import com.example.lena.ui.theme.Gray800
import com.example.lena.ui.theme.Gray900
import com.example.lena.ui.theme.LENATheme


enum class SelectedOption {
    ChangeEmail, ChangePassword,
}

@Composable
fun MyAccountScreen(navController: NavController,){

    val context = LocalContext.current
    val activity = context as? Activity
    val focusManager = LocalFocusManager.current
    val isImeVisible by rememberImeState()
    val GreetingVisibleState = remember { mutableStateOf(false) }
    val selectedOption = remember { mutableStateOf<SelectedOption?>(null) }

    val changeEmailDialog = remember { mutableStateOf(false) }
    val signOutDialog = remember { mutableStateOf(false) }
    val deleteAccountDialog = remember { mutableStateOf(false) }
    val confirmDeleteDialog = remember { mutableStateOf(false) }



    LaunchedEffect(Unit) {
        GreetingVisibleState.value = true
    }


//    LaunchedEffect(authState.value) {
//        when(authState.value) {
//            is AuthState.Unauthenticated -> {
//                navController.navigate(Screens.LoginScreen.name) {
//                    popUpTo(Screens.MainMenu.name) { inclusive = true }
//                }
//            }
//            else -> Unit
//        }
//    }

    Scaffold(
        topBar = { MyAccountTopBar(modifier = Modifier, navController = navController/*viewModel = authViewModel*/) }
    ) { innerPadding ->


            Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ){
            Spacer(modifier = Modifier
                .height(64.dp)
                .fillMaxWidth())
        //===================================================================---> Greetings
        AnimatedVisibility(
            visible = GreetingVisibleState.value,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = fadeOut()
        ){
            Column(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 32.dp))
            {
                Text(
                    text = "Hi Fawaz", /*TODO*/
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Text(
                    text = "What would you like to do?",
                    fontWeight = FontWeight.Light,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                )
            }
        }
                Spacer(modifier = Modifier
                    .height(64.dp)
                    .fillMaxWidth())
    //=================================================================================--> Options
                Box(
                    Modifier.fillMaxSize(),
                ){
                        Column(
                            Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            var isItChangeEmail = false
                            var isItChangePassword = false
                            when (selectedOption.value) {
                                null -> {
                                    Text(
                                        text = "Change Login Email",
                                        fontWeight =FontWeight.Light,
                                        fontSize = 20.sp,
                                        modifier = Modifier.clickable {
                                            isItChangeEmail = !isItChangeEmail
                                            selectedOption.value = SelectedOption.ChangeEmail
                                        }
                                    )
                                    Text(
                                        text = "Change Password",
                                        fontWeight = if (selectedOption.value == SelectedOption.ChangePassword) FontWeight.Bold else FontWeight.Light,
                                        fontSize = 20.sp,
                                        modifier = Modifier.clickable {
                                            isItChangePassword = !isItChangePassword
                                            selectedOption.value = SelectedOption.ChangePassword
                                        }
                                    )
                                }
                                SelectedOption.ChangeEmail -> {
                                    var email = ""
                                    Text(
                                        text = "Change Login Email",
                                        fontWeight =FontWeight.Bold,
                                        fontSize = 20.sp,
                                        modifier = Modifier.clickable {
                                            isItChangeEmail = !isItChangeEmail
                                            selectedOption.value = null
                                        }
                                    )
                                    OutlinedTextField(
                                        value = email,
                                        onValueChange = {email = it},
                                        label = { Text(text = "New Email Address") },
                                        modifier = Modifier,
                                        singleLine = true,
                                    )
                                    Button(
                                        onClick = { changeEmailDialog.value = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary,
                                            disabledContainerColor = if (isSystemInDarkTheme()) Gray900 else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                            disabledContentColor = if (isSystemInDarkTheme()) Gray800 else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                        ),
                                        modifier = Modifier.width(124.dp),
                                        shape = RoundedCornerShape(12.dp)
                                        )
                                    {
                                        Text(
                                            text = "Submit",
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Light,
                                            textAlign = TextAlign.Center,
                                        )
                                    }
                                    Text(
                                        text = "Change Password",
                                        fontWeight = if (selectedOption.value == SelectedOption.ChangePassword) FontWeight.Bold else FontWeight.Light,
                                        fontSize = 20.sp,
                                        modifier = Modifier.clickable {
                                            isItChangePassword = !isItChangePassword
                                            selectedOption.value = SelectedOption.ChangePassword
                                        }
                                    )
                                    if(changeEmailDialog.value){
                                        ConfirmationDialog(
                                            title = "Change Email",
                                            message = "Are you sure you want to change your email?",
                                            onConfirm = {
                                                changeEmailDialog.value = false
                                                selectedOption.value = null
                                            },
                                            onDismiss = {
                                                changeEmailDialog.value = false
                                                selectedOption.value = null
                                            },
                                            confirmationText = "Yes, Change Email",
                                            dismissText = "Cancel"

                                        )
                                    }
                                }
                                SelectedOption.ChangePassword -> {
                                    var password = ""
                                    var newPassword = ""
                                    var confirmPassword = ""

                                    Text(
                                        text = "Change Login Email",
                                        fontWeight =FontWeight.Light,
                                        fontSize = 20.sp,
                                        modifier = Modifier.clickable {
                                            isItChangeEmail = !isItChangeEmail
                                            selectedOption.value = SelectedOption.ChangeEmail
                                        }
                                    )
                                    Text(
                                        text = "Change Password",
                                        fontWeight = if (selectedOption.value == SelectedOption.ChangePassword) FontWeight.Bold else FontWeight.Light,
                                        fontSize = 20.sp,
                                        modifier = Modifier.clickable {
                                            isItChangePassword = !isItChangePassword
                                            selectedOption.value = null
                                        }
                                    )
                                    OutlinedTextField(
                                        value = password,
                                        onValueChange = {password = it},
                                        label = { Text(text = "Old password") },
                                        modifier = Modifier,
                                        singleLine = true,
                                    )
                                    OutlinedTextField(
                                        value = newPassword,
                                        onValueChange = {newPassword = it},
                                        label = { Text(text = "New password") },
                                        modifier = Modifier,
                                        singleLine = true,
                                    )
                                    OutlinedTextField(
                                        value = confirmPassword,
                                        onValueChange = {confirmPassword = it},
                                        label = { Text(text = "Confirm new password") },
                                        modifier = Modifier,
                                        singleLine = true,
                                    )
                                    Button(
                                        onClick = {},
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary,
                                            disabledContainerColor = if (isSystemInDarkTheme()) Gray900 else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                            disabledContentColor = if (isSystemInDarkTheme()) Gray800 else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                        ),
                                        modifier = Modifier.width(124.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    {
                                        Text(
                                            text = "Submit",
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Light,
                                            textAlign = TextAlign.Center,
                                        )
                                    }
                                }
                                else -> Unit
                            }
                        }




                    Column(
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)

                    ) {
                        Text(
                            text = "Sign Out",
                            fontWeight = FontWeight.Light,
                            fontSize = 20.sp,
                            modifier = Modifier.clickable {
                                signOutDialog.value = true
                            }
                        )
                        if (signOutDialog.value){
                            ConfirmationDialog(
                                title = "Sign Out",
                                message = "Are you sure you want to sign out?",
                                onConfirm = {},
                                onDismiss = { signOutDialog.value = false },
                                confirmationText = "Sign Out",
                                dismissText = "Cancel"
                            )
                        }
                        Text(
                            text = "Delete My Account",
                            fontWeight = FontWeight.Light,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.clickable {
                                deleteAccountDialog.value = true
                            }
                        )
                        if (deleteAccountDialog.value){
                            ConfirmationDialog(
                                title = "Delete Account",
                                message = "Are you sure you want to delete your account?",
                                onConfirm = {
                                    deleteAccountDialog.value = false
                                    confirmDeleteDialog.value = true
                                },
                                onDismiss = { deleteAccountDialog.value = false },
                                confirmationText = "Yes, Delete my Account",
                                dismissText = "Cancel",
                                Confirmcolor = MaterialTheme.colorScheme.error
                            )
                        }
                        if (confirmDeleteDialog.value){
                            InputConfirmationDialog(
                                title = "Confirm Account Deletion",
                                message = "Please type your password to confirm account deletion",
                                onDismiss = { confirmDeleteDialog.value = false },
                                confirmationText = "Confirm",
                                onConfirm = {},
                                dismissText = "Cancel",
                                inputLabel = "Password"
                            )
                        }
                    }

                }

            }
        //===================================================================--->

    }
}








@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAccountTopBar(modifier: Modifier = Modifier, navController: NavController/*viewModel: AuthViewModel*/) {
    TopAppBar(
        title = {
                Text(
                    text = stringResource(R.string.my_account_string),
                    color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier
                )

        },
        navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Back"
                )
            }
        },
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Preview(showSystemUi = true)
@Composable
fun MyAccountPreview(){
    LENATheme(darkTheme = true) {
        MyAccountScreen(navController = rememberNavController())

    }
}