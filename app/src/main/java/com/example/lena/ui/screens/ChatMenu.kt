package com.example.lena.ui.screens


import android.Manifest
import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lena.Data.LenaConstants.greetingStrings
import com.example.lena.Models.MessageModel
import com.example.lena.R
import com.example.lena.Screens
import com.example.lena.ui.theme.DarkSuccess
import com.example.lena.ui.theme.LENATheme
import com.example.lena.ui.theme.LightSuccess
import com.example.lena.viewModels.AuthState
import com.example.lena.viewModels.AuthViewModel
import com.example.lena.viewModels.ChatViewModel
import com.example.lena.viewModels.SpeechRecognitionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch





@Composable
fun ChatMenu(navController: NavController, authViewModel: AuthViewModel) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val chatViewModel: ChatViewModel = viewModel()
    var backPressedOnce by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    //=============================================================--> Permission handling
    var showPermissionPopup by remember { mutableStateOf(true) }
    //================================================================


    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthState.Unauthenticated -> {
                navController.navigate(Screens.LoginScreen.name) {
                    popUpTo(Screens.ChatMenu.name) { inclusive = true }
                }
            }
            else -> Unit
        }
    }

    BackHandler(enabled = authState.value is AuthState.Authenticated) {
        if (backPressedOnce) {
            // Exit the app
            activity?.finish()
        } else {
            backPressedOnce = true
            Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
            coroutineScope.launch {
                delay(2000)  // 2 seconds to reset the flag
                backPressedOnce = false
            }
        }
    }



    Scaffold(
        topBar = { MainMenuTopBar(viewModel = authViewModel, navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            if (showPermissionPopup) {
                PermissionPopup(onDismiss = { showPermissionPopup = false })
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 64.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            })
                        }
                ) {
                    MessageList(
                        messageList = chatViewModel.messageList,
                        modifier = Modifier.weight(1f)
                    )
                }

                MessageInput(
                    onMessageSend = { chatViewModel.sendMessage(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .imePadding()
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuTopBar(modifier: Modifier = Modifier, viewModel: AuthViewModel, navController: NavController) {
    var optionsMenu by remember { mutableStateOf(false) }
    var confirmSignOutDialog = remember { mutableStateOf(false) }
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.Chat_menu),
                    color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.weight(0.8f)
                )
                Icon(
                    painter = painterResource(id = R.drawable.menu),
                    contentDescription = stringResource(R.string.Logo),
                    tint = if (isSystemInDarkTheme()) Color.White else Color.Black,
                    modifier = Modifier
                        .size(36.dp)
                        .clickable(
                            onClick = { optionsMenu = !optionsMenu },
                            indication = null,  // This disables the ripple effect
                            interactionSource = remember { MutableInteractionSource() }
                        )
                        .weight(0.2f)
                )
                DropdownMenu(
                    expanded = optionsMenu,
                    onDismissRequest = { optionsMenu = false },
                    offset = DpOffset(500.dp, 0.dp),
                    modifier = Modifier.background(
                        color = MaterialTheme.colorScheme.outline,
                    )
                ){
                    DropdownMenuItem(
                        onClick = {
                            optionsMenu = false
                            navController.navigate(Screens.MyAccountScreen.name
                            ) },
                        text = { Text(text = "My Account", fontStyle = MaterialTheme.typography.bodyMedium.fontStyle) },

                        )
                    DropdownMenuItem(
                        onClick = {
                            optionsMenu = false
                            confirmSignOutDialog.value = true
                        },
                        text = { Text(text = "Logout", fontStyle = MaterialTheme.typography.bodyMedium.fontStyle)},
                    )
                }
                if(confirmSignOutDialog.value){
                    ConfirmationDialog(
                        title = "Logout",
                        message = "Are you sure you want to logout?",
                        onConfirm = {
                            confirmSignOutDialog.value = false
                            viewModel.signOut(navController)
                        },
                        onDismiss = {
                            confirmSignOutDialog.value = false
                        },
                        confirmationText = "Yes, Logout",
                        dismissText = "Cancel"
                    )
                }
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


@Composable
fun MessageList(modifier: Modifier = Modifier, messageList: List<MessageModel>) {
    val visibleState = remember { mutableStateOf(false) }
    if (messageList.isEmpty()) {

        LaunchedEffect(Unit) {
            visibleState.value = true
        }
        AnimatedVisibility(
            visible = visibleState.value,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = fadeOut()
        )
        {Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Greetings()
        }}
    } else {
        visibleState.value = false
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            reverseLayout = true
        ) {
            items(messageList.reversed()) {
                MessageRow(messageModel = it)
            }
        }
    }
}
@Composable

fun MessageRow(messageModel: MessageModel) {
    val isModel = messageModel.role == "model"
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .align(if (isModel) Alignment.BottomStart else Alignment.BottomEnd)
                    .padding(
                        start = if (isModel) 8.dp else 70.dp,
                        end = if (isModel) 70.dp else 8.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    )
                    .clip(RoundedCornerShape(48f))
                    .background(
                        if (isModel) {
                            if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
                        } else {
                            if (isSystemInDarkTheme()) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground
                        }
                    )
                    .padding(8.dp)
            ) {
                SelectionContainer {
                    Text(
                        text = messageModel.prompt,
                        color = Color.White,
                        fontWeight = FontWeight.W300,
                    )
                }
            }
        }
    }
}



@Composable
fun MessageInput(
    onMessageSend: (String) -> Unit,
    modifier: Modifier = Modifier,
    speechRecognitionViewModel: SpeechRecognitionViewModel = viewModel()
) {
    var message by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    val isListening by speechRecognitionViewModel.isListening.collectAsState()
    val spokenText by speechRecognitionViewModel.spokenText.observeAsState()
    val needsPermission by speechRecognitionViewModel.needsPermission.collectAsState()
    val error by speechRecognitionViewModel.error.observeAsState()

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            speechRecognitionViewModel.checkAndInitializeSpeechRecognizer()
            speechRecognitionViewModel.handleMicrophoneClick()
        } else {
            // Handle permission denied case
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Handle permission request
    LaunchedEffect(needsPermission) {
        if (needsPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            // Reset the flag inside the ViewModel after handling
            speechRecognitionViewModel.onPermissionHandled()
        }
    }

    // Handle spoken text updates
    LaunchedEffect(spokenText) {
        spokenText?.let {
            message = it
            // Optionally, send the message automatically
            // onMessageSend(message.trim())
            // message = ""
        }
    }

    // Handle errors
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            speechRecognitionViewModel.clearError()
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text(stringResource(R.string.textField_label)) },
            modifier = Modifier
                .weight(1f)
                .imePadding(),
            maxLines = 4,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = if (message.isNotBlank()) ImeAction.Send else ImeAction.Default
            ),
            keyboardActions = KeyboardActions(
                onSend = {
                    if (message.isNotBlank()) {
                        onMessageSend(message.trim())
                        message = ""
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                focusedLabelColor = MaterialTheme.colorScheme.onSurface
            )
        )
        IconButton(
            onClick = {
                if (message.isNotBlank()) {
                    keyboardController?.hide()
                    onMessageSend(message.trim())
                    message = ""
                    focusManager.clearFocus()
                } else {
                    speechRecognitionViewModel.handleMicrophoneClick()
                }
            }
        ) {
            Icon(
                imageVector = when {
                    message.isNotBlank() -> Icons.AutoMirrored.Filled.Send
                    isListening -> Icons.Default.MicNone
                    else -> Icons.Default.MicNone
                },
                contentDescription = when {
                    message.isNotBlank() -> stringResource(R.string.send_message)
                    isListening -> stringResource(R.string.stop_listening)
                    else -> stringResource(R.string.start_listening)
                },
                tint = if (isListening) if (isSystemInDarkTheme()) {DarkSuccess} else {LightSuccess} else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun Greetings(){
    Box(modifier = Modifier
        .fillMaxWidth()
        .imePadding(),
        contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = if (isSystemInDarkTheme()) R.drawable.medusa_white else R.drawable.medusa_black),
                contentDescription = stringResource(R.string.Logo),
                modifier = Modifier
                    .size(100.dp)

            )
            Text(
                text = greetingStrings.random(),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp)
            )
        }
    }
}

@Composable
fun PermissionPopup(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity

    val permissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.SET_ALARM,
        Manifest.permission.WRITE_CALENDAR
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsResult ->
        // Handle permission results here
        if (permissionsResult.all { it.value }) {
            Toast.makeText(context, "All permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Some permissions were denied", Toast.LENGTH_SHORT).show()
        }
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            Button(onClick = {
                permissionLauncher.launch(permissions.toTypedArray())
            }) {
                Text("OK")
            }
        },
        title = { Text("Permission Request") },
        text = { Text("Please accept the following permissions.") },
        dismissButton = null
    )
}

@Preview()
@Composable
fun MainMenuPreview() {

}

@Preview
@Composable
fun MessageInputPreview(){
    LENATheme {
        MessageInput({})
    }
}

@Preview
@Composable
fun GreetingsPreview(){
    LENATheme {
        Greetings()
    }
}