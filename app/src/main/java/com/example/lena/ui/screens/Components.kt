package com.example.lena.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.lena.ui.theme.Gray800
import com.example.lena.ui.theme.Gray900
import com.example.lena.viewModels.AuthState
import com.example.lena.viewModels.AuthViewModel




@Composable
internal fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmationText: String,
    dismissText: String,
    confirmColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val authState = AuthViewModel().authState.observeAsState()
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                if (authState.value == AuthState.Loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(text = confirmationText, color = confirmColor)
                }
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
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    confirmationText: String,
    dismissText: String,
    inputLabel: String,
    confirmColor: Color = MaterialTheme.colorScheme.onSurface,
    type: String = "text",
    passwordError: Boolean = false,
    keyboardOnDone: (String) -> Unit,
) {
    var input by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val authState = AuthViewModel().authState.observeAsState()
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
                    visualTransformation = if (type == "password" && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,                    isError = passwordError,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done,
                        keyboardType = if (type == "password") KeyboardType.Password else KeyboardType.Text,
                        autoCorrectEnabled = false
                    ),
                    supportingText = {
                        if (passwordError) {
                            Text(text = "Invalid Password", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                    },
                    keyboardActions = KeyboardActions(onDone = {keyboardOnDone}),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    trailingIcon = {
                        when (type){
                            "password" -> {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = if (passwordVisible) "Hide Password" else "Show Password"
                                    )
                                }
                            }
                            else -> {}
                        }
                    },
                )
            }

        },
        confirmButton = {
            TextButton(onClick = { onConfirm(input) }) {
                if (authState.value == AuthState.Loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(text = confirmationText, color = confirmColor)
                }
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
internal fun SubmitButton(
    text: String,
    onClick: () -> Unit,
    isEnabled: Boolean = true,
    modifier: Modifier = Modifier,
    buttonWidth: Dp = 124.dp,
    cornerRadius: Dp = 12.dp,
    buttonColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
) {

    Button(
        onClick = onClick,
        enabled = isEnabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = if (isSystemInDarkTheme()) Gray900 else MaterialTheme.colorScheme.onSurface.copy(
                alpha = 0.12f
            ),
            disabledContentColor = if (isSystemInDarkTheme()) Gray800 else MaterialTheme.colorScheme.onSurface.copy(
                alpha = 0.38f
            ),
        ),
        modifier = modifier.width(buttonWidth),
        shape = RoundedCornerShape(cornerRadius)
    ) {
        val authState = AuthViewModel().authState.observeAsState()
        if (authState.value == AuthState.Loading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text = text,
                color = textColor,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
internal fun FadedHorizontalDivider(topPadding: Dp = 0.dp, bottomPadding: Dp = 0.dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPadding, bottom = bottomPadding)
            .height(2.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        MaterialTheme.colorScheme.onSurface,
                        Color.Transparent
                    )
                )
            )
    )
}

@Composable
fun PermissionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    showRationale: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (showRationale)
                    "Permissions Required"
                else
                    "Important Permissions"
            )
        },
        text = {
            Text(
                text = if (showRationale) {
                    "These permissions are needed for core functionality. " +
                            "Please grant them in Settings."
                } else {
                    "This app requires location, calendar, microphone, and " +
                            "Bluetooth permissions for full functionality."
                }
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(
                    text = if (showRationale)
                        "Open Settings"
                    else
                        "Grant Permissions"
                )
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
