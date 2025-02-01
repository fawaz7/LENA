package com.yarmouk.lena.ui.screens


import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.yarmouk.lena.Data.Voices
import com.yarmouk.lena.R
import com.yarmouk.lena.Screens
import com.yarmouk.lena.ui.rememberImeState
import com.yarmouk.lena.ui.theme.DarkSuccess
import com.yarmouk.lena.ui.theme.LightSuccess
import com.yarmouk.lena.viewModels.AuthEvent
import com.yarmouk.lena.viewModels.AuthState
import com.yarmouk.lena.viewModels.AuthViewModel
import com.yarmouk.lena.viewModels.VoiceViewModel
import kotlinx.coroutines.launch


enum class SelectedOption {
    ChangeEmail, ChangePassword, ChangeTtsVoice
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAccountScreen(navController: NavController, authViewModel: AuthViewModel, voiceViewModel: VoiceViewModel ) {

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val isImeVisible by rememberImeState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val oldPasswordFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val repeatPasswordFocusRequester = remember { FocusRequester() }
    var isRePasswordFocused by remember { mutableStateOf(false) }
    val greetingVisibleState = remember { mutableStateOf(false) }
    val selectedOption = remember { mutableStateOf<SelectedOption?>(null) }
    val changeEmailDialog = remember { mutableStateOf(false) }
    val signOutDialog = remember { mutableStateOf(false) }
    val deleteAccountDialog = remember { mutableStateOf(false) }
    val changePasswordDialog = remember { mutableStateOf(false) }
    val confirmDeleteDialog = remember { mutableStateOf(false) }
    val uiState by authViewModel.uiState.collectAsState()

    val availableVoices = voiceViewModel.availableVoices
    val selectedVoice = voiceViewModel.selectedVoice
    var tempSelectedVoice by remember { mutableStateOf(uiState.selectedVoice) }
    var isTtsDisabled by remember { mutableStateOf(uiState.isTtsDisabled) }
    var showDialog by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }

    fun playVoiceSample(context: Context, voiceDisplayName: String) {
        val resId = when (voiceDisplayName) {
            "Rubie" -> R.raw.wit_rubie // Replace with your actual resource IDs
            "Cooper" -> R.raw.wit_cooper
            "Vampire" -> R.raw.wit_vampire
            "Kenyan Accent" -> R.raw.wit_kenyan_accent
            "British Butler" -> R.raw.wit_british_butler
            "Cartoon Villain" -> R.raw.wit_cartoon_villain
            "Cockney Accent" -> R.raw.wit_cockney_accent
            else -> null // Default sample if no match found
        }

        resId?.let {
            mediaPlayer?.release() // Release any previous MediaPlayer instance
            mediaPlayer = MediaPlayer.create(context, it)
            mediaPlayer?.start()
        }
    }


    fun isDialogsOff(): Boolean{
        if (
            changeEmailDialog.value == false &&
            signOutDialog.value == false &&
            deleteAccountDialog.value == false &&
            changePasswordDialog.value == false &&
            confirmDeleteDialog.value == false
        ) {return true}
        else return false
    }

    LaunchedEffect(Unit) {
        authViewModel.resetToastFlag()
        authViewModel.resetUiState()
    }

    LaunchedEffect(Unit) {
        greetingVisibleState.value = true
    }

    LaunchedEffect(authViewModel.authState.value) {
        when (authViewModel.authState.value) {
            is AuthState.Authenticated -> {
                authViewModel.fetchUserInfo()
                authViewModel.fetchVerificationStatus()
            }
            is AuthState.Unauthenticated -> {
                navController.navigate(Screens.LoginScreen.name) {
                    popUpTo(Screens.ChatMenu.name) { inclusive = true }
                }
            }
            else -> Unit
        }
    }

    LaunchedEffect(Unit) {
        launch {
            authViewModel.authEvent.collect { event ->
                Log.d("ToastDebug", "Collected event: $event")
                when (event) {
                    is AuthEvent.Info -> {
                        Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                        authViewModel.resetToastFlag()
                    }
                    is AuthEvent.Error -> {
                        Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                        authViewModel.resetToastFlag()
                    }
                    AuthEvent.None -> {}
                }
            }
        }
    }

    val imeSpacer1 by animateDpAsState(targetValue = if (isImeVisible && isDialogsOff()) 0.dp else 64.dp, animationSpec = tween(durationMillis = 300),
        label = "ImeSpacer1 Animation")
    val imeSpacer2 by animateDpAsState(targetValue = if (isImeVisible && isDialogsOff()) 0.dp else 16.dp, animationSpec = tween(durationMillis = 300),
        label = "ImeSpacer1 Animation")




    Scaffold(
        topBar = { MyAccountTopBar(modifier = Modifier, navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier
                .height(imeSpacer1)
                .fillMaxWidth())

            // Greetings Section
            AnimatedVisibility(
                visible = greetingVisibleState.value,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 32.dp)
                ) {
                    Text(
                        text = "Hi ${uiState.authorizedUserFirstName}",
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
            FadedHorizontalDivider(topPadding = 16.dp, bottomPadding = 8.dp)
            Spacer(modifier = Modifier
                .height(imeSpacer2)
                .fillMaxWidth())

            //=================================================================================--> Change Email/Password/TTS Options Section
            Box(
                Modifier.fillMaxSize(),
            ) {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Change Login Email",
                        fontWeight = if (selectedOption.value == SelectedOption.ChangeEmail) FontWeight.W500 else FontWeight.Light,
                        fontSize = 20.sp,
                        modifier = Modifier.clickable {
                            selectedOption.value =
                                if (selectedOption.value == SelectedOption.ChangeEmail) null else SelectedOption.ChangeEmail
                        }
                    )
                    AnimatedVisibility(
                            visible = selectedOption.value == SelectedOption.ChangeEmail,
                            enter = slideInVertically(initialOffsetY = { -it/2 }, ) + fadeIn(animationSpec = tween(durationMillis = 150)),
                            exit = slideOutVertically(targetOffsetY = { -it/2 }, ) + fadeOut(animationSpec = tween(durationMillis = 150))
                        ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),

                        ) {
                            OutlinedTextField(
                                value = uiState.authorizedUserEmail,
                                onValueChange = {},
                                enabled = false,
                                singleLine = true,
                                isError = !uiState.isAuthorizedUserVerified,
                                label = { Text(text = if (uiState.isAuthorizedUserVerified) "Current Email Address" else "This email address is not verified") },
                                leadingIcon = {
                                    if (uiState.isAuthorizedUserVerified){
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Checked mark", tint = if (isSystemInDarkTheme()) DarkSuccess else LightSuccess)
                                    } else { Icon(imageVector = Icons.Filled.Info , contentDescription = "Checked mark", tint = MaterialTheme.colorScheme.error) }
                                },
                                supportingText = {
                                    if (uiState.isAuthorizedUserVerified){
                                    Text(text = "This email address is verified", color = if (isSystemInDarkTheme()) DarkSuccess else LightSuccess)
                                    } else {
                                            Text(
                                                text = "resend verification email.",
                                                color = MaterialTheme.colorScheme.error,
                                                style = MaterialTheme.typography.bodySmall.merge(TextStyle(textDecoration = TextDecoration.Underline)),
                                                modifier = Modifier
                                                    .clickable(
                                                    ) { authViewModel.sendVerificationEmail() },                                                )
                                    }
                                },
                            )
                            OutlinedTextField(
                                value = uiState.authorizedNewEmailAddress,
                                onValueChange = { authViewModel.onAuthorizedNewEmailChange(it) },
                                label = { Text(text = "New Email Address") },
                                modifier = Modifier.onFocusChanged { focusState ->
                                    if (!focusState.isFocused) {
                                        authViewModel.onAuthorizedNewEmailFocusChanged(focusState.isFocused)
                                    }
                                },
                                singleLine = true,
                                trailingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = "Email Icon") },
                                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done, keyboardType = KeyboardType.Email),
                                keyboardActions = KeyboardActions(onDone = {
                                    if (!uiState.authorizedNewEmailError && uiState.authorizedNewEmailAddress.isNotBlank()) {
                                        changeEmailDialog.value = true
                                    } else {
                                        authViewModel.onAuthorizedNewEmailFocusChanged(false) // Show error message
                                    }
                                }),
                                isError = uiState.authorizedNewEmailError && uiState.authorizedNewEmailAddress.isNotBlank(),
                                supportingText = {
                                    if (uiState.authorizedNewEmailError && uiState.authorizedNewEmailAddress.isNotBlank()) {
                                        Text(text = "Invalid Email Address", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),

                            )

                            SubmitButton(
                                text = "Submit",
                                onClick = {
                                    if (!uiState.authorizedNewEmailError && uiState.authorizedNewEmailAddress.isNotBlank()) {
                                        changeEmailDialog.value = true
                                    } else {
                                        authViewModel.onAuthorizedNewEmailFocusChanged(false) // Show error message
                                    }
                                },
                                isEnabled = !uiState.authorizedNewEmailError && uiState.authorizedNewEmailAddress.isNotBlank(),
                            )

                            if (changeEmailDialog.value){

                                InputConfirmationDialog(
                                    title = "Change Email?",
                                    message = "Please Type your Password again to confirm changing your current email to ${uiState.authorizedNewEmailAddress}\n\n" +
                                            "Please note that you will be signed out to confirm the changes.\n",
                                    onConfirm = { password ->
                                        authViewModel.changeEmail(uiState.authorizedNewEmailAddress, password)
                                        { success ->
                                            if (success){
                                                changeEmailDialog.value = false
                                            }
                                        }
                                    }, /*TODO*/
                                    onDismiss = { changeEmailDialog.value = false },
                                    confirmationText = "Yes, Change Email",
                                    dismissText = "Cancel",
                                    inputLabel = "Confirm Password",
                                    type = "password",
                                    passwordError = uiState.changeEmailPasswordConfirmationError,
                                    keyboardOnDone = { password ->
                                        authViewModel.changeEmail(uiState.authorizedNewEmailAddress, password)
                                        { success ->
                                            if (success){
                                                changeEmailDialog.value = false
                                            }
                                        }
                                    },

                                )
                            }
                        }
                    }

                    Text(
                        text = "Change Password",
                        fontWeight = if (selectedOption.value == SelectedOption.ChangePassword) FontWeight.W500 else FontWeight.Light,
                        fontSize = 20.sp,
                        modifier = Modifier.clickable {
                            selectedOption.value =
                                if (selectedOption.value == SelectedOption.ChangePassword) null else SelectedOption.ChangePassword
                        }
                    )
                    AnimatedVisibility(
                        visible = selectedOption.value == SelectedOption.ChangePassword,
                        enter = slideInVertically(initialOffsetY = { -it/2 }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { -it/2 }) + fadeOut()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            var currentPassword by remember { mutableStateOf("") }

                            OutlinedTextField(
                                value = currentPassword,
                                onValueChange = { currentPassword = it },
                                label = { Text(text = "Old password") },
                                modifier = Modifier.focusRequester(oldPasswordFocusRequester),
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(onClick = { authViewModel.togglePasswordVisibility() }) {
                                        Icon(
                                            imageVector = if (uiState.isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                            contentDescription = if (uiState.isPasswordVisible) "Hide Password" else "Show Password"
                                        )
                                    }
                                },
                                visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                isError = uiState.isCurrentPasswordWrong && currentPassword.isNotBlank(),
                                supportingText = {
                                    if (uiState.isCurrentPasswordWrong && currentPassword.isNotBlank()) {
                                        Text(
                                            text = uiState.currentPasswordError,
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                )
                            OutlinedTextField(
                                value = uiState.password,
                                onValueChange = { authViewModel.onPasswordChange(it, Screens.MyAccountScreen) },
                                label = { Text(text = "Password") },
                                modifier = Modifier
                                    .focusRequester(passwordFocusRequester)
                                    .onFocusChanged { focusState ->
                                        authViewModel.onPasswordFocusChanged(
                                            focusState.isFocused,
                                            Screens.MyAccountScreen
                                        )
                                    },
                                trailingIcon = {
                                    IconButton(onClick = { authViewModel.togglePasswordVisibility() }) {
                                        Icon(
                                            imageVector = if (uiState.isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                            contentDescription = if (uiState.isPasswordVisible) "Hide Password" else "Show Password"
                                        )
                                    }
                                },
                                visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Next,
                                    keyboardType = KeyboardType.Password,
                                    autoCorrectEnabled = false
                                ),
                                keyboardActions = KeyboardActions(onNext = { repeatPasswordFocusRequester.requestFocus() }),
                                isError = uiState.passwordError,
                                supportingText = {
                                    if (uiState.passwordError) {
                                        Text(
                                            text = uiState.signUpPasswordErrorMessage,
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
//=============================================================================-----> Re-enter Password field
                            OutlinedTextField(
                                value = uiState.rePassword,
                                onValueChange = { authViewModel.onRePasswordChange(it, Screens.MyAccountScreen) },
                                label = { Text(text = "Repeat password") },
                                modifier = Modifier
                                    .focusRequester(repeatPasswordFocusRequester)
                                    .onFocusChanged { focusState ->
                                        isRePasswordFocused = focusState.isFocused
                                        if (!focusState.isFocused) {
                                            authViewModel.onRePasswordChange(
                                                uiState.rePassword,
                                                Screens.MyAccountScreen
                                            )
                                        }
                                    },
                                trailingIcon = {
                                    IconButton(onClick = { authViewModel.toggleRePasswordVisibility() }) {
                                        Icon(
                                            imageVector = if (uiState.isRePasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                            contentDescription = if (uiState.isRePasswordVisible) "Hide Password" else "Show Password"
                                        )
                                    }
                                },
                                visualTransformation = if (uiState.isRePasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = if (uiState.isSignUpFormValid) ImeAction.Send else ImeAction.Done,
                                    keyboardType = KeyboardType.Password,
                                    autoCorrectEnabled = false
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    if (uiState.isSignUpFormValid) {
                                        changePasswordDialog.value = true
                                        keyboardController?.hide()
                                    } else {
                                        keyboardController?.hide()
                                    }
                                }),
                                isError = uiState.repeatPasswordError,
                                supportingText = {
                                    if (uiState.repeatPasswordError) {
                                        Text(
                                            text = "Passwords do not match",
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            SubmitButton(
                                text = "Submit",
                                onClick = { changePasswordDialog.value = true },
                                isEnabled = uiState.isPasswordFieldsValid,
                            )

                            if (changePasswordDialog.value){
                                ConfirmationDialog(
                                    title = "Change Password",
                                    message = "Are you sure you want to change your password?\nPlease note that you'll be required to sign in again to apply changes.",
                                    onConfirm = {
                                        authViewModel.changePassword(
                                            oldPassword = currentPassword, // This is your old password state
                                            newPassword = uiState.password, // This is your new password state
                                        ) { success ->
                                            if (success) {
                                                changePasswordDialog.value = false
                                                // Password was changed successfully and user will be signed out
                                            } else {
                                                changePasswordDialog.value = false
                                            }
                                        }
                                    },
                                    onDismiss = { changePasswordDialog.value = false },
                                    confirmationText = "Yes, Change Password",
                                    dismissText = "Cancel",
                                )
                            }
                        }
                    }
                    //============================================--> TTS options
                    Text(
                        text = "Change assistant's voice",
                        fontWeight = if (selectedOption.value == SelectedOption.ChangeTtsVoice) FontWeight.W500 else FontWeight.Light,
                        fontSize = 20.sp,
                        modifier = Modifier.clickable {
                            selectedOption.value =
                                if (selectedOption.value == SelectedOption.ChangeTtsVoice) null else SelectedOption.ChangeTtsVoice
                        }
                    )
                    AnimatedVisibility(
                        visible = selectedOption.value == SelectedOption.ChangeTtsVoice,
                        enter = slideInVertically(initialOffsetY = { -it/2 }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { -it/2 }) + fadeOut()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Spacer(modifier = Modifier.height(4.dp))
                            // Show Dialog for Voice Selection
                            Row(
                                modifier = Modifier
                                    .wrapContentSize()
                                    .border(
                                        width = 0.5.dp,
                                        color = if (!isTtsDisabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .padding(8.dp)
                                    .clickable(enabled = !isTtsDisabled) {
                                        tempSelectedVoice = selectedVoice.value.toString() // Store the current selection temporarily
                                        showDialog = true // Open the dialog
                                    }.widthIn(min = 200.dp, max = 200.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = uiState.selectedVoice.let { voiceName ->
                                        Voices.allVoices.find { it.name == voiceName }?.displayName ?: "Select Voice"
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (!isTtsDisabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                )
                                Log.d("selectedVoice", selectedVoice.value.toString())

                            }

                            // Show Dialog for Voice Selection
                            if (showDialog) {
                                Dialog(onDismissRequest = {
                                    showDialog = false
                                    tempSelectedVoice = uiState.selectedVoice // Reset tempSelectedVoice if dialog is dismissed
                                }) {
                                    Surface(
                                        modifier = Modifier.size(300.dp),
                                        shape = MaterialTheme.shapes.medium,
                                        color = MaterialTheme.colorScheme.background,
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "Select Voice",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )
                                            HorizontalDivider()
                                            LazyColumn(
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                items(availableVoices) { voiceDisplayName ->
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                tempSelectedVoice = voiceDisplayName // Update the temporary selection
                                                                playVoiceSample(context, voiceDisplayName) // Play the selected voice sample
                                                            }
                                                            .padding(8.dp),
                                                        horizontalArrangement = Arrangement.Center
                                                    ) {
                                                        Text(
                                                            text = voiceDisplayName,
                                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                                fontWeight = if (tempSelectedVoice == voiceDisplayName) FontWeight.Bold else FontWeight.Light,
                                                            ),
                                                            textAlign = TextAlign.Center
                                                        )
                                                    }
                                                }
                                            }
                                            HorizontalDivider()
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = {
                                                    voiceViewModel.changeSelectedVoice(tempSelectedVoice) // Update the selected voice
                                                    authViewModel.updateVoiceModel(tempSelectedVoice) // Save the selected voice to Firebase
                                                    showDialog = false

                                                    // Here you can add the action for previewing the voice if needed
                                                },
                                                modifier = Modifier.align(Alignment.End)
                                            ) {
                                                Text("Confirm")
                                            }
                                        }
                                    }
                                }
                            }

                            // Checkbox to enable/disable TTS
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isTtsDisabled,
                                    onCheckedChange = { isDisabled ->
                                        isTtsDisabled = isDisabled
                                        authViewModel.updateTtsStatus(isDisabled)
                                    }
                                )
                                Text("Disable Text to Speech")
                            }
                        }
                    }



                    Box(
                        Modifier.fillMaxSize()
                    ) {
                        var currentPassword by remember { mutableStateOf("") }
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
                                    onConfirm = {
                                        signOutDialog.value = false
                                        authViewModel.signOut(navController)
                                    },
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



                                InputConfirmationDialog(
                                    type = "password",
                                    title = "Confirm Account Deletion",
                                    message = "Please type your password to confirm account deletion",
                                    onDismiss = {
                                        deleteAccountDialog.value = false
                                        currentPassword = ""  // Reset password when dismissed
                                    },
                                    confirmationText = "Confirm",
                                    onConfirm = { password ->
                                        currentPassword = password  // Set the current password
                                        authViewModel.reAuthenticateUser(password) { success ->
                                            if (success) {
                                                deleteAccountDialog.value = false
                                                confirmDeleteDialog.value = true
                                            }
                                        }
                                    },
                                    dismissText = "Cancel",
                                    inputLabel = "Password",
                                    keyboardOnDone = { password ->
                                        currentPassword = password  // Set the current password
                                        authViewModel.reAuthenticateUser(password) { success ->
                                            if (success) {
                                                deleteAccountDialog.value = false
                                                confirmDeleteDialog.value = true
                                            }
                                        }
                                    },
                                    confirmColor = MaterialTheme.colorScheme.error,
                                    passwordError = uiState.isCurrentPasswordWrong && currentPassword.isNotBlank()
                                )
                            }
                            if (confirmDeleteDialog.value){
                                ConfirmationDialog(
                                    title = "Delete Account",
                                    message = "Are you sure you want to delete your account?\nThis action can't be undone.",
                                    onConfirm = {
                                        authViewModel.deleteAccount(currentPassword) { success ->
                                            if (success) {
                                                confirmDeleteDialog.value = false
                                                navController.navigate(Screens.LoginScreen.name)
                                            }
                                        }
                                    },
                                    onDismiss = { confirmDeleteDialog.value = false },
                                    confirmationText = "Yes, Delete my Account",
                                    dismissText = "Cancel",
                                    confirmColor = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
            }
        }
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

