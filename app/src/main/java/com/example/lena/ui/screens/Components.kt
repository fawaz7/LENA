package com.example.lena.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
internal fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmationText: String,
    dismissText: String,
    Confirmcolor: Color = MaterialTheme.colorScheme.onSurface
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text(
                    text = confirmationText,
                    color = Confirmcolor
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(
                    text = dismissText,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}

@Composable
internal fun InputConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmationText: String,
    dismissText: String,
    inputLabel: String,
    confirmColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    var input = ""
    AlertDialog(

        onDismissRequest = { onDismiss() },
        title = { Text(text = title) },
        text = {
            Column(modifier = Modifier, verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = message)
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text(text = inputLabel) },
                    modifier = Modifier,
                    singleLine = true,
                )
            }

        },
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text(
                    text = confirmationText,
                    color = confirmColor
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(
                    text = dismissText,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}
