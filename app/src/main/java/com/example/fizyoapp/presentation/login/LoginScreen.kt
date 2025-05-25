package com.example.fizyoapp.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import kotlinx.coroutines.flow.collectLatest

private val primaryColor = Color(59, 62, 104)
private val backgroundColor = Color(245, 245, 250)
private val surfaceColor = Color.White
private val accentColor = Color(59, 62, 104)
private val textColor = Color.DarkGray

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                colors = CardDefaults.cardColors(containerColor = primaryColor),
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
                        text = "FizyoAPP",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Hoş Geldiniz",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 16.sp
                    )
                }
            }

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
                        text = "Giriş Yap",
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
                            onClick = { viewModel.onEvent(LoginEvent.RoleChanged(UserRole.USER)) },
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
                            onClick = { viewModel.onEvent(LoginEvent.RoleChanged(UserRole.PHYSIOTHERAPIST)) },
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
                        onValueChange = { viewModel.onEvent(LoginEvent.EmailChanged(it)) },
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
                        onValueChange = { viewModel.onEvent(LoginEvent.PasswordChanged(it)) },
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

                    if (state.errorMessage != null) {
                        Text(
                            text = state.errorMessage,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Button(
                        onClick = { viewModel.onEvent(LoginEvent.SignIn) },
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
                                "Giriş Yap",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { viewModel.onEvent(LoginEvent.NavigateToRegister) }
                        ) {
                            Text(
                                "Kayıt Ol",
                                color = primaryColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        TextButton(
                            onClick = { viewModel.onEvent(LoginEvent.NavigateToForgotPassword) }
                        ) {
                            Text(
                                "Şifremi Unuttum",
                                color = primaryColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}