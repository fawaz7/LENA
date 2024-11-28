package com.example.lena.ui.screens

import android.R.attr.textSize
import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.lena.R
import com.example.lena.ui.rememberImeState
import com.example.lena.ui.theme.Gray800
import com.example.lena.ui.theme.Gray900
import com.example.lena.ui.theme.LENATheme
import com.example.lena.viewModels.AuthState
import com.example.lena.viewModels.AuthViewModel
@Composable
fun MyAccountScreen(navController: NavController,){

    val context = LocalContext.current
    val activity = context as? Activity
    val focusManager = LocalFocusManager.current
    val isImeVisible by rememberImeState()
    val GreetingVisibleState = remember { mutableStateOf(false) }
    val selectedOption = remember { mutableStateOf<String?>(null) }

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
                Spacer(modifier = Modifier.height(64.dp).fillMaxWidth())
    //=================================================================================--> Options
                Box(
                    Modifier.fillMaxSize(),
                ){
                        Column(
                            Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Change Login Email",
                                fontWeight = if (selectedOption.value == "email") FontWeight.Bold else FontWeight.Light,
                                fontSize = 20.sp,
                                modifier = Modifier.clickable {
                                    selectedOption.value = "email"
                                }
                            )
                            Text(
                                text = "Change Password",
                                fontWeight = if (selectedOption.value == "password") FontWeight.Bold else FontWeight.Light,
                                fontSize = 20.sp,
                                modifier = Modifier.clickable {
                                    selectedOption.value = "password"
                                }
                            )
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
                        )
                        Text(
                            text = "Delete My Account",
                            fontWeight = FontWeight.Light,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.error
                        )
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