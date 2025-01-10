package com.yarmouk.lena.ui.screens


import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.input.key.*
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.yarmouk.lena.Data.LenaConstants.greetingStrings
import com.yarmouk.lena.Data.LenaConstants.thinkingStrings
import com.yarmouk.lena.Models.MessageModel
import com.yarmouk.lena.R
import com.yarmouk.lena.Screens
import com.yarmouk.lena.ui.theme.DarkSuccess
import com.yarmouk.lena.ui.theme.LENATheme
import com.yarmouk.lena.ui.theme.LightSuccess
import com.yarmouk.lena.utils.PermissionManager
import com.yarmouk.lena.utils.rememberPermissionState
import com.yarmouk.lena.viewModels.AuthState
import com.yarmouk.lena.viewModels.AuthViewModel
import com.yarmouk.lena.viewModels.ChatViewModel
import com.yarmouk.lena.viewModels.SpeechRecognitionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun ChatMenu(navController: NavController, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val speechRecognitionViewModel: SpeechRecognitionViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel(
        factory = ChatViewModel.Factory(application, speechRecognitionViewModel)
    )
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var backPressedOnce by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val authState = authViewModel.authState.observeAsState()

    val activity = context as? Activity

    // Permission handling states
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showRationaleDialog by remember { mutableStateOf(false) }




    val multiplePermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val deniedPermissions = permissions.filterValues { !it }.keys.toList()

        if (deniedPermissions.isNotEmpty()) {
            val shouldShowRationaleForAny = deniedPermissions.any { permission ->
                activity?.let {
                    ActivityCompat.shouldShowRequestPermissionRationale(it, permission)
                } == true
            }

            if (shouldShowRationaleForAny) {
                showRationaleDialog = true
            } else {
                Toast.makeText(
                    context,
                    "Please enable all required permissions in Settings",
                    Toast.LENGTH_LONG
                ).show()
                PermissionManager.openAppSettings(context)
            }
        } else {
            PermissionManager.updatePermissionsRequested(context)
            Toast.makeText(context, "All permissions granted", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!PermissionManager.checkPermissions(context).all { it.value } &&
            !PermissionManager.hasRequestedPermissionsBefore(context)
        ) {
            showPermissionDialog = true
        }
    }

    if (showPermissionDialog) {
        PermissionDialog(
            onDismiss = {
                showPermissionDialog = false
                if (!PermissionManager.checkPermissions(context).all { it.value }) {
                    Toast.makeText(
                        context,
                        "Some features may be limited without permissions",
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            onConfirm = {
                showPermissionDialog = false
                val ungranted = PermissionManager.REQUIRED_PERMISSIONS.filter {
                    ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
                }
                if (ungranted.isNotEmpty()) {
                    multiplePermissionResultLauncher.launch(ungranted.toTypedArray())
                }
            }
        )
    }

    if (showRationaleDialog) {
        PermissionDialog(
            onDismiss = {
                showRationaleDialog = false
                Toast.makeText(
                    context,
                    "Some features may be limited without permissions",
                    Toast.LENGTH_LONG
                ).show()
            },
            onConfirm = {
                showRationaleDialog = false
                PermissionManager.openAppSettings(context)
            },
            showRationale = true
        )
    }

    BackHandler(enabled = authState.value is AuthState.Authenticated) {
        if (backPressedOnce) {
            activity?.finish()
        } else {
            backPressedOnce = true
            Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
            coroutineScope.launch {
                delay(2000)
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
                    .imePadding(),
                chatViewModel = chatViewModel,
                speechRecognitionViewModel = speechRecognitionViewModel
            )
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



@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun MessageInput(
    onMessageSend: (String) -> Unit,
    modifier: Modifier = Modifier,
    chatViewModel: ChatViewModel,
    speechRecognitionViewModel: SpeechRecognitionViewModel
) {
    var message by remember { mutableStateOf("") }
    var showPermissionDialog by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val activity = LocalContext.current as? Activity
    val scope = rememberCoroutineScope()

    // Speech recognition states
    val isListening by speechRecognitionViewModel.isListening.collectAsState()
    val spokenText by speechRecognitionViewModel.spokenText.observeAsState()
    val error by speechRecognitionViewModel.error.observeAsState()
    val autoContinue by speechRecognitionViewModel.autoContinue.collectAsState()


    // Permission handling
    val permissionState = rememberPermissionState { permissions ->
        when {
            permissions[Manifest.permission.RECORD_AUDIO] == true -> {
                scope.launch {
                    speechRecognitionViewModel.checkAndInitializeSpeechRecognizer()
                    speechRecognitionViewModel.handleMicrophoneClick()
                }
            }
            activity?.let { PermissionManager.shouldShowRationale(it, listOf(Manifest.permission.RECORD_AUDIO)) } == true -> {
                showPermissionDialog = true
            }
            else -> {
                Toast.makeText(context, "Microphone permission is required for voice input", Toast.LENGTH_SHORT).show()
                PermissionManager.openAppSettings(context)
            }
        }
    }

    // Handle spoken text updates
    LaunchedEffect(spokenText) {
        spokenText?.let {
            message = it
            onMessageSend(message.trim())
            message = ""
            focusManager.clearFocus()
        }
    }

    fun isThinkingString(prompt: String?): Boolean {
        val result = thinkingStrings.any { prompt == it }
        return result
    }

    // Handle responses and auto-continue
    LaunchedEffect(chatViewModel.messageList.size) {
        scope.launch {
            delay(1500) // Initial delay to allow processing

            while (true) {
                val lastMessage = chatViewModel.messageList.lastOrNull()

                if (lastMessage?.role == "model" && isThinkingString(lastMessage.prompt)) {
                    delay(500) // Additional delay to check again
                } else {
                    if (autoContinue && !isListening && !speechRecognitionViewModel.isTtsPlaying.value) {
                        speechRecognitionViewModel.handleMicrophoneClick()
                    }
                    break
                }
            }
        }
    }


    // Handle errors
    LaunchedEffect(error) {
        error?.let {
            Log.d("MessageInput", "Speech recognition error: $it")
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            speechRecognitionViewModel.clearError()
        }
    }

    // Handle errors
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            speechRecognitionViewModel.clearError()
        }
    }

    // Permission dialog
    if (showPermissionDialog) {
        PermissionDialog(
            onDismiss = { showPermissionDialog = false },
            onConfirm = {
                showPermissionDialog = false
                PermissionManager.openAppSettings(context)
            },
            showRationale = true
        )
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
                .imePadding().onKeyEvent { keyEvent ->
                    if (keyEvent.key == Key.Enter && keyEvent.type == KeyEventType.KeyUp) {
                        if(message.isNotBlank()){
                            onMessageSend(message.trim())
                            message = ""
                        }
                        true
                    } else {
                        false
                    }
                },
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
                    // Check microphone permission before handling click
                    when {
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            speechRecognitionViewModel.toggleAutoContinue()
                            speechRecognitionViewModel.handleMicrophoneClick()
                        }
                        activity?.let {
                            ActivityCompat.shouldShowRequestPermissionRationale(
                                it,
                                Manifest.permission.RECORD_AUDIO
                            )
                        } == true -> {
                            showPermissionDialog = true
                        }
                        else -> {
                            permissionState.permissionLauncher.launch(
                                arrayOf(Manifest.permission.RECORD_AUDIO)
                            )
                        }
                    }
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
                tint = if (isListening) {
                    if (isSystemInDarkTheme()) DarkSuccess else LightSuccess
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
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


@Preview()
@Composable
fun MainMenuPreview() {

}



@Preview
@Composable
fun GreetingsPreview(){
    LENATheme {
        Greetings()
    }
}