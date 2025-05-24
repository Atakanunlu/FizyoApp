package com.example.fizyoapp.domain.model.payment

sealed class PaymentResult {
    data class Success(
        val paymentId: String,
        val status: String,
        val amount: Double,
        val cardHolder: String,
        val last4Digits: String
    ) : PaymentResult()

    data class Error(val message: String) : PaymentResult()

    object Loading : PaymentResult()
}