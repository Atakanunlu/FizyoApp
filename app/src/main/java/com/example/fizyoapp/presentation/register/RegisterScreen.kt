package com.example.fizyoapp.presentation.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.fizyoapp.domain.model.auth.UserRole
import com.example.fizyoapp.presentation.navigation.AppScreens
import com.example.fizyoapp.presentation.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showVerificationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is RegisterViewModel.UiEvent.NavigateToLogin -> {
                    navController.navigate(AppScreens.LoginScreen.route) {
                        popUpTo(AppScreens.RegisterScreen.route) { inclusive = true }
                    }
                }
                is RegisterViewModel.UiEvent.ShowEmailVerificationDialog -> {
                    showVerificationDialog = true
                }
                is RegisterViewModel.UiEvent.ShowSuccessDialog -> {
                    showSuccessDialog = true
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                viewModel.onEvent(RegisterEvent.ResetState)
                navController.navigate(AppScreens.LoginScreen.route) {
                    popUpTo(AppScreens.RegisterScreen.route) { inclusive = true }
                }
            },
            title = {
                Text(
                    "Başarılı!",
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            },
            text = {
                Text(
                    "Kayıt işlemi başarıyla tamamlandı. Şimdi giriş yapabilirsiniz.",
                    color = textColor
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.onEvent(RegisterEvent.ResetState)
                        navController.navigate(AppScreens.LoginScreen.route) {
                            popUpTo(AppScreens.RegisterScreen.route) { inclusive = true }
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

    if (showVerificationDialog) {
        AlertDialog(
            onDismissRequest = {
                showVerificationDialog = false
                viewModel.onEvent(RegisterEvent.ResetState)
                navController.navigate(AppScreens.LoginScreen.route) {
                    popUpTo(AppScreens.RegisterScreen.route) { inclusive = true }
                }
            },
            title = {
                Text(
                    "E-posta Doğrulama",
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            },
            text = {
                Column {
                    Text(
                        "Kayıt işlemi başarıyla tamamlandı! E-posta adresinize bir doğrulama bağlantısı gönderdik.",
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Lütfen hesabınızı aktifleştirmek için e-postanızı kontrol edin ve doğrulama bağlantısına tıklayın.",
                        color = textColor
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showVerificationDialog = false
                        viewModel.onEvent(RegisterEvent.ResetState)
                        navController.navigate(AppScreens.LoginScreen.route) {
                            popUpTo(AppScreens.RegisterScreen.route) { inclusive = true }
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
                title = { Text("Kayıt Ol", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onEvent(RegisterEvent.NavigateToLogin) }) {
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
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
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
                        text = "Hesap Oluştur",
                        style = MaterialTheme.typography.headlineMedium,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    Text(
                        text = "Rol Seçin",
                        fontWeight = FontWeight.Medium,
                        color = textColor,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = state.selectedRole == UserRole.USER,
                            onClick = { viewModel.onEvent(RegisterEvent.RoleChanged(UserRole.USER)) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = primaryColor
                            )
                        )
                        Text(
                            text = "Kullanıcı",
                            fontWeight = FontWeight.Medium,
                            color = textColor,
                            modifier = Modifier.padding(start = 8.dp, end = 16.dp)
                        )

                        RadioButton(
                            selected = state.selectedRole == UserRole.PHYSIOTHERAPIST,
                            onClick = { viewModel.onEvent(RegisterEvent.RoleChanged(UserRole.PHYSIOTHERAPIST)) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = primaryColor
                            )
                        )
                        Text(
                            text = "Fizyoterapist",
                            fontWeight = FontWeight.Medium,
                            color = textColor,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    OutlinedTextField(
                        value = state.email,
                        onValueChange = { viewModel.onEvent(RegisterEvent.EmailChanged(it)) },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
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

                    OutlinedTextField(
                        value = state.password,
                        onValueChange = { viewModel.onEvent(RegisterEvent.PasswordChanged(it)) },
                        label = { Text("Şifre") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Şifre",
                                tint = primaryColor.copy(alpha = 0.7f)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation(),
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

                    OutlinedTextField(
                        value = state.confirmPassword,
                        onValueChange = { viewModel.onEvent(RegisterEvent.ConfirmPasswordChanged(it)) },
                        label = { Text("Şifre Doğrulama") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Şifre Doğrulama",
                                tint = primaryColor.copy(alpha = 0.7f)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation(),
                        isError = state.passwordError,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            focusedLabelColor = primaryColor,
                            cursorColor = primaryColor,
                            errorBorderColor = errorColor,
                            errorLabelColor = errorColor,
                            errorCursorColor = errorColor
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    if (state.passwordError) {
                        Text(
                            text = "Şifreler eşleşmiyor!",
                            color = errorColor,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            textAlign = TextAlign.Start
                        )
                    }

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
                        onClick = { viewModel.onEvent(RegisterEvent.SignUp) },
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
                                "Kayıt Ol",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    TextButton(
                        onClick = { viewModel.onEvent(RegisterEvent.NavigateToLogin) },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            "Zaten bir hesabınız var mı? Giriş Yapın",
                            color = primaryColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}