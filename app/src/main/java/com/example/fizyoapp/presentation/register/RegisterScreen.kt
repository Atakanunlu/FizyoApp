package com.example.fizyoapp.presentation.register

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.fizyoapp.domain.model.auth.UserRole
import com.example.fizyoapp.presentation.navigation.AppScreens
import kotlinx.coroutines.flow.collectLatest

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
            title = { Text("Başarılı!") },
            text = { Text("Kayıt işlemi başarıyla tamamlandı. Şimdi giriş yapabilirsiniz.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.onEvent(RegisterEvent.ResetState)
                        navController.navigate(AppScreens.LoginScreen.route) {
                            popUpTo(AppScreens.RegisterScreen.route) { inclusive = true }
                        }
                    }
                ) {
                    Text("Tamam")
                }
            }
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
            title = { Text("E-posta Doğrulama") },
            text = {
                Column {
                    Text("Kayıt işlemi başarıyla tamamlandı! E-posta adresinize bir doğrulama bağlantısı gönderdik.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Lütfen hesabınızı aktifleştirmek için e-postanızı kontrol edin ve doğrulama bağlantısına tıklayın.")
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
                    }
                ) {
                    Text("Tamam")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Kayıt Ol",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(text = "Rol Seçin", modifier = Modifier.padding(bottom = 8.dp))
        Row(
            modifier = Modifier.padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = state.selectedRole == UserRole.USER,
                onClick = { viewModel.onEvent(RegisterEvent.RoleChanged(UserRole.USER)) }
            )
            Text(
                text = "Kullanıcı",
                modifier = Modifier.padding(start = 8.dp, end = 16.dp)
            )
            RadioButton(
                selected = state.selectedRole == UserRole.PHYSIOTHERAPIST,
                onClick = { viewModel.onEvent(RegisterEvent.RoleChanged(UserRole.PHYSIOTHERAPIST)) }
            )
            Text(
                text = "Fizyoterapist",
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        OutlinedTextField(
            value = state.email,
            onValueChange = { viewModel.onEvent(RegisterEvent.EmailChanged(it)) },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = state.password,
            onValueChange = { viewModel.onEvent(RegisterEvent.PasswordChanged(it)) },
            label = { Text("Şifre") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = state.confirmPassword,
            onValueChange = { viewModel.onEvent(RegisterEvent.ConfirmPasswordChanged(it)) },
            label = { Text("Şifre Doğrulama") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            isError = state.passwordError,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        if (state.passwordError) {
            Text(
                text = "Şifreler eşleşmiyor!",
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 8.dp)
            )
        }

        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage,
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = { viewModel.onEvent(RegisterEvent.SignUp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(bottom = 8.dp),
            enabled = !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text("Kayıt Ol")
            }
        }
        TextButton(
            onClick = { viewModel.onEvent(RegisterEvent.NavigateToLogin) },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Zaten bir hesabınız var mı? Giriş Yapın")
        }
    }
}