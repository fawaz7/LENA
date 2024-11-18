package com.example.lena


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Send
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.lena.ViewModels.ChatViewModel
import com.example.lena.ui.theme.LENATheme



@Composable
fun MainMenu(navController: NavController ,modifier: Modifier = Modifier){
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
            Row(verticalAlignment = Alignment.CenterVertically,) {
                Text(
                    text = stringResource(R.string.Chat_menu),
                    color = Color.White,
                    modifier = Modifier.weight(0.8f)
                )
                Image(
                    painter = painterResource(id = R.drawable.medusa_white),
                    contentDescription = stringResource(R.string.Logo),
                    modifier = Modifier
                        .size(36.dp).weight(0.15f)
                )

            }


                },
        colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Black),
        modifier = modifier
    )

}

@Composable
fun MessageInput(onMessageSend: (String) -> Unit,){
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
            modifier = Modifier.weight(0.8f))
        IconButton( onClick = {
            onMessageSend(message)
            message = ""
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