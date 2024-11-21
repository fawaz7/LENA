package com.example.lena.ui.screens


import android.text.Layout
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.lena.Data.LenaConstants.greetingStrings
import com.example.lena.Models.MessageModel
import com.example.lena.R
import com.example.lena.ui.theme.LENATheme
import com.example.lena.viewModels.ChatViewModel


@Composable
fun MainMenu(navController: NavController){
    val chatViewModel: ChatViewModel = viewModel()
    Scaffold(
        topBar = { MainMenuTopBar() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
    ){
            MessageList(
                messageList = chatViewModel.messageList,
                modifier = Modifier.weight(1f))

            MessageInput(onMessageSend = {
                chatViewModel.sendMessage(it)
            })
        }


    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuTopBar(modifier: Modifier = Modifier){
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.Chat_menu),
                    color = Color.White,
                    modifier = Modifier.weight(0.8f)
                )
                Image(
                    painter = painterResource(id = R.drawable.medusa_white),
                    contentDescription = stringResource(R.string.Logo),
                    modifier = Modifier
                        .size(36.dp)
                        .weight(0.15f)
                )

            }


                },
        colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Black),
        modifier = modifier
    )

}


@Composable
fun MessageList(modifier : Modifier = Modifier, messageList: List<MessageModel>){
    if (messageList.isEmpty()){
        Column(
            modifier = Modifier
                .fillMaxSize(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.medusa_black),
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

    else {
        LazyColumn(
            modifier = modifier,
            reverseLayout = true
        ){
            items(messageList.reversed()) {
                MessageRow(messageModel = it)
            }
        }
    }
}

@Composable
fun MessageRow(messageModel: MessageModel){
    val isLena = messageModel.role == "Lena"
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ){
        Box(modifier = Modifier.fillMaxWidth()) {

            Box(modifier = Modifier
                .align(
                    if (isLena) Alignment.BottomStart else Alignment.BottomEnd
                )
                .padding(
                    start = if (isLena) 8.dp else 70.dp,
                    end = if (isLena) 70.dp else 8.dp,
                    top = 8.dp,
                    bottom = 8.dp
                )
                .border(
                    width = 2.dp,
                    color = if (isLena) Color.Blue else Color.Gray,
                    shape = RoundedCornerShape(8.dp) )
                .padding(16.dp)
            ) {
                SelectionContainer {
                    Text(
                        text = messageModel.prompt,
                        fontWeight = FontWeight.W500
                    )
                }
            }
        }
    }
}



@Composable
fun MessageInput(onMessageSend: (String) -> Unit){
    var message by remember { mutableStateOf("") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically){
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text(stringResource(R.string.textField_label)) },
            modifier = Modifier.weight(0.8f),
            singleLine = true)
        IconButton( onClick = {
            if (message.isNotBlank() || message.isNotEmpty()){
            onMessageSend(message)
            message = ""}
        } ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send message",

            )
        }
    }
}



@Preview(showSystemUi = true)
@Composable
fun MainMenuPreview() {
    LENATheme {
        val navController = rememberNavController()
        MainMenu(navController)
    }}