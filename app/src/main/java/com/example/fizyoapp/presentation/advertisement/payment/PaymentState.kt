package com.example.fizyoapp.presentation.advertisement.payment

data class PaymentState(
    val cardHolderName: String = "",
    val cardNumber: String = "",
    val expireMonth: String = "",
    val expireYear: String = "",
    val cvc: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFormValid: Boolean = false
)