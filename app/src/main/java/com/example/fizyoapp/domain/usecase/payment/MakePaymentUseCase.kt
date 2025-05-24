package com.example.fizyoapp.domain.usecase.payment

import com.example.fizyoapp.data.repository.payment.PaymentRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.payment.PaymentResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MakePaymentUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository
) {
    operator fun invoke(
        cardHolderName: String,
        cardNumber: String,
        expireMonth: String,
        expireYear: String,
        cvc: String,
        amount: Double,
        physiotherapistId: String
    ): Flow<Resource<PaymentResult>> {
        return paymentRepository.makePayment(
            cardHolderName, cardNumber, expireMonth, expireYear, cvc, amount, physiotherapistId
        )
    }
}