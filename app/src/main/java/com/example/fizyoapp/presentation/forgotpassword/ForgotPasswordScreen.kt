package com.example.fizyoapp.presentation.forgotpassword

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.fizyoapp.presentation.navigation.AppScreens
import com.example.fizyoapp.presentation.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value
    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is ForgotPasswordViewModel.UiEvent.NavigateBack -> {
                    navController.popBackStack()
                }
                is ForgotPasswordViewModel.UiEvent.ShowSuccessDialog -> {
                    showSuccessDialog = true
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                navController.navigate(AppScreens.LoginScreen.route) {
                    popUpTo(AppScreens.ForgotPasswordScreen.route) { inclusive = true }
                }
            },
            title = {
                Text(
                    "Sıfırlama Bağlantısı Gönderildi",
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            },
            text = {
                Column {
                    Text(
                        "E-posta adresinize bir şifre sıfırlama bağlantısı gönderdik.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Lütfen e-postanızı kontrol edin ve şifrenizi sıfırlamak için gönderilen bağlantıya tıklayın.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        navController.navigate(AppScreens.LoginScreen.route) {
                            popUpTo(AppScreens.ForgotPasswordScreen.route) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor
                    )
                ) {
                    Text("Tamam")
                }
            },
            containerColor = surfaceColor
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Şifremi Unuttum", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onEvent(ForgotPasswordEvent.NavigateBack) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Şifremi Sıfırla",
                        style = MaterialTheme.typography.headlineMedium,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = "Hesabınıza kayıtlı e-posta adresinizi girin. Size şifre sıfırlama bağlantısı göndereceğiz.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = { viewModel.onEvent(ForgotPasswordEvent.EmailChanged(it)) },
                        label = { Text("E-posta") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "E-posta",
                                tint = primaryColor.copy(alpha = 0.7f)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            focusedLabelColor = primaryColor,
                            cursorColor = primaryColor
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    if (state.errorMessage != null) {
                        Text(
                            text = state.errorMessage,
                            color = errorColor,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    Button(
                        onClick = { viewModel.onEvent(ForgotPasswordEvent.SendResetEmail) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !state.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text(
                                "Sıfırlama Bağlantısı Gönder",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    TextButton(
                        onClick = { viewModel.onEvent(ForgotPasswordEvent.NavigateBack) },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text(
                            "Giriş Sayfasına Dön",
                            color = primaryColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}