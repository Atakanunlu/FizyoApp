package com.example.fizyoapp.presentation.advertisement.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.fizyoapp.presentation.navigation.AppScreens
import kotlinx.coroutines.flow.collectLatest

private val primaryColor = Color(59, 62, 104)
private val backgroundColor = Color(245, 245, 250)
private val surfaceColor = Color.White
private val textColor = Color.DarkGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvertisementPaymentScreen(
    navController: NavController,
    viewModel: AdvertisementPaymentViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value
    val scrollState = rememberScrollState()

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AdvertisementPaymentViewModel.UIEvent.NavigateToSuccess -> {
                    navController.navigate(AppScreens.AdvertisementSuccessScreen.route) {
                        popUpTo(AppScreens.CreateAdvertisementScreen.route) { inclusive = true }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Ödeme",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ödeme Bilgileri",
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = primaryColor.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Ödeme Özeti",
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Reklam Ücreti:",
                                color = textColor
                            )
                            Text(
                                text = "50,00 ₺",
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = primaryColor.copy(alpha = 0.2f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Toplam:",
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                            Text(
                                text = "50,00 ₺",
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = state.cardHolderName,
                    onValueChange = { viewModel.onCardHolderNameChanged(it) },
                    label = { Text("Kart Üzerindeki İsim") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = primaryColor
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        focusedLabelColor = primaryColor,
                        cursorColor = primaryColor,
                        unfocusedBorderColor = primaryColor.copy(alpha = 0.5f),
                        unfocusedLabelColor = primaryColor.copy(alpha = 0.7f)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.cardNumber,
                    onValueChange = { viewModel.onCardNumberChanged(it) },
                    label = { Text("Kart Numarası") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = {
                        Icon(
                            Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = primaryColor
                        )
                    },
                    placeholder = { Text("5528 7900 0000 0008") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        focusedLabelColor = primaryColor,
                        cursorColor = primaryColor,
                        unfocusedBorderColor = primaryColor.copy(alpha = 0.5f),
                        unfocusedLabelColor = primaryColor.copy(alpha = 0.7f)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = state.expireMonth,
                        onValueChange = { viewModel.onExpireMonthChanged(it) },
                        label = { Text("Ay") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("12") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            focusedLabelColor = primaryColor,
                            cursorColor = primaryColor,
                            unfocusedBorderColor = primaryColor.copy(alpha = 0.5f),
                            unfocusedLabelColor = primaryColor.copy(alpha = 0.7f)
                        )
                    )

                    OutlinedTextField(
                        value = state.expireYear,
                        onValueChange = { viewModel.onExpireYearChanged(it) },
                        label = { Text("Yıl") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("2030") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            focusedLabelColor = primaryColor,
                            cursorColor = primaryColor,
                            unfocusedBorderColor = primaryColor.copy(alpha = 0.5f),
                            unfocusedLabelColor = primaryColor.copy(alpha = 0.7f)
                        )
                    )

                    OutlinedTextField(
                        value = state.cvc,
                        onValueChange = { viewModel.onCvcChanged(it) },
                        label = { Text("CVC") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = primaryColor
                            )
                        },
                        placeholder = { Text("123") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            focusedLabelColor = primaryColor,
                            cursorColor = primaryColor,
                            unfocusedBorderColor = primaryColor.copy(alpha = 0.5f),
                            unfocusedLabelColor = primaryColor.copy(alpha = 0.7f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF6200EA).copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Test Kart Bilgileri",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6200EA)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Kart No: 5528790000000008",
                            color = textColor
                        )
                        Text(
                            text = "Son Kullanma: 12/2030",
                            color = textColor
                        )
                        Text(
                            text = "CVC: 123",
                            color = textColor
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { viewModel.onPayClicked() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !state.isLoading && state.isFormValid,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        disabledContainerColor = primaryColor.copy(alpha = 0.5f)
                    )
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text("Ödeme Yap - 50,00 ₺")
                    }
                }

                if (state.error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.error,
                        color = Color(0xFFB71C1C),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}