package com.example.fizyoapp.presentation.login

import com.example.fizyoapp.domain.model.auth.UserRole
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.fizyoapp.presentation.navigation.AppScreens
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value


    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            try {
                when (event) {
                    is LoginViewModel.UiEvent.NavigateBasedOnRole -> {
                        when (event.role) {
                            UserRole.PHYSIOTHERAPIST -> {
                                navController.navigate(AppScreens.PhysiotherapistMainScreen.route) {
                                    popUpTo(AppScreens.LoginScreen.route) { inclusive = true }
                                }
                            }
                            UserRole.USER -> {
                                navController.navigate(AppScreens.UserMainScreen.route) {
                                    popUpTo(AppScreens.LoginScreen.route) { inclusive = true }
                                }
                            }
                            else -> {
                                navController.navigate(AppScreens.LoginScreen.route)
                            }
                        }
                    }
                    is LoginViewModel.UiEvent.NavigateToRegister -> {
                        navController.navigate(AppScreens.RegisterScreen.route)
                    }
                    is LoginViewModel.UiEvent.NavigateToProfileSetup -> {
                        navController.navigate(AppScreens.UserProfileSetupScreen.route) {
                            popUpTo(AppScreens.LoginScreen.route) { inclusive = true }
                        }
                    }
                    is LoginViewModel.UiEvent.NavigateToPhysiotherapistProfileSetup -> {
                        navController.navigate(AppScreens.PhysiotherapistProfileSetupScreen.route) {
                            popUpTo(AppScreens.LoginScreen.route) { inclusive = true }
                        }
                    }
                    is LoginViewModel.UiEvent.NavigateToForgotPassword -> {
                        navController.navigate(AppScreens.ForgotPasswordScreen.route)
                    }
                }
            } catch (e: Exception) {
                if (state.user?.role == UserRole.USER) {
                    navController.navigate(AppScreens.UserMainScreen.route) {
                        popUpTo(AppScreens.LoginScreen.route) { inclusive = true }
                    }
                } else if (state.user?.role == UserRole.PHYSIOTHERAPIST) {
                    navController.navigate(AppScreens.PhysiotherapistMainScreen.route) {
                        popUpTo(AppScreens.LoginScreen.route) { inclusive = true }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Giriş Yap",
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
                onClick = { viewModel.onEvent(LoginEvent.RoleChanged(UserRole.USER)) }
            )
            Text(
                text = "Kullanıcı",
                modifier = Modifier.padding(start = 8.dp, end = 16.dp)
            )
            RadioButton(
                selected = state.selectedRole == UserRole.PHYSIOTHERAPIST,
                onClick = { viewModel.onEvent(LoginEvent.RoleChanged(UserRole.PHYSIOTHERAPIST)) }
            )
            Text(
                text = "Fizyoterapist",
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        OutlinedTextField(
            value = state.email,
            onValueChange = { viewModel.onEvent(LoginEvent.EmailChanged(it)) },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = state.password,
            onValueChange = { viewModel.onEvent(LoginEvent.PasswordChanged(it)) },
            label = { Text("Şifre") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

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
            onClick = { viewModel.onEvent(LoginEvent.SignIn) },
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
                Text("Giriş Yap")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { viewModel.onEvent(LoginEvent.NavigateToRegister) }
            ) {
                Text("Hesabınız yok mu? Kayıt Olun")
            }

            TextButton(
                onClick = { viewModel.onEvent(LoginEvent.NavigateToForgotPassword) }
            ) {
                Text("Şifremi Unuttum")
            }
        }
    }
}