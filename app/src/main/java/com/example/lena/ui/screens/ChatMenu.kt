package com.example.lena.ui.screens


import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
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
import androidx.navigation.compose.rememberNavController
import com.example.lena.Data.LenaConstants.greetingStrings
import com.example.lena.Models.MessageModel
import com.example.lena.R
import com.example.lena.Screens
import com.example.lena.ui.theme.LENATheme
import com.example.lena.viewModels.AuthState
import com.example.lena.viewModels.AuthViewModel
import com.example.lena.viewModels.ChatViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun MainMenu(navController: NavController, authViewModel: AuthViewModel) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val chatViewModel: ChatViewModel = viewModel()
    var backPressedOnce by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthState.Unauthenticated -> navController.navigate(Screens.LoginScreen.name)
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
        topBar = { MainMenuTopBar(viewModel = authViewModel) }
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
                    .padding(bottom = 64.dp) // Reserve space for the input field
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

            // Position the message input at the bottom
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuTopBar(modifier: Modifier = Modifier, viewModel: AuthViewModel) {
    var optionsMenu by remember { mutableStateOf(false) }
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.Chat_menu),
                    color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                    modifier = Modifier.weight(0.8f)
                )
                Image(
                    painter = painterResource(id = if (isSystemInDarkTheme()) R.drawable.medusa_white else R.drawable.medusa_black),
                    contentDescription = stringResource(R.string.Logo),
                    modifier = Modifier.size(36.dp).clickable(
                        onClick = {
                            optionsMenu = !optionsMenu
                        }
                    ).weight(0.2f)
                )
                DropdownMenu(
                    expanded = optionsMenu,
                    onDismissRequest = { optionsMenu = false },
                    offset = DpOffset(500.dp, 0.dp),
                    modifier = Modifier.background(
                        color = MaterialTheme.colorScheme.secondary,
                    )
                ){
                    DropdownMenuItem(
                        onClick = { viewModel.signOut() },
                        text = { Text(text = "Logout") },

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
    if (messageList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Greetings()
        }
    } else {
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
fun MessageInput(onMessageSend: (String) -> Unit, modifier: Modifier = Modifier) {
    var message by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 8.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text(stringResource(R.string.textField_label)) },
            modifier = Modifier.weight(0.8f).imePadding(),
            maxLines = 4,
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (message.isNotBlank()) {
                        onMessageSend(message.trimEnd(' '))
                        message = ""
                    }
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        IconButton(onClick = {
            if (message.isNotBlank()) {
                keyboardController?.hide()
                onMessageSend(message.trimEnd(' '))
                message = ""
                focusManager.clearFocus()
            }
        }) {
            Icon(
                imageVector = if (message == "") Icons.Default.Mic else Icons.Default.Send,
                contentDescription = "Send message"
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
            )
        }
    }
}




@Preview(showSystemUi = true)
@Composable
fun MainMenuPreview() {
    LENATheme {
        val navController = rememberNavController()
        MainMenu(navController, AuthViewModel())
    }}

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