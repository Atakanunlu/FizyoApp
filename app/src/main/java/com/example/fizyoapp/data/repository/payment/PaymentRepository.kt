package com.example.fizyoapp.data.repository.payment

import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.payment.PaymentResult
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
    fun makePayment(
        cardHolderName: String,
        cardNumber: String,
        expireMonth: String,
        expireYear: String,
        cvc: String,
        amount: Double,
        physiotherapistId: String
    ): Flow<Resource<PaymentResult>>
}