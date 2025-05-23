package com.example.fizyoapp.data.repository.payment

import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.payment.PaymentResult
import com.iyzipay.Options
import com.iyzipay.model.*
import com.iyzipay.model.Currency
import com.iyzipay.model.Locale
import com.iyzipay.request.CreatePaymentRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject

class PaymentRepositoryImpl @Inject constructor() : PaymentRepository {
    override fun makePayment(
        cardHolderName: String,
        cardNumber: String,
        expireMonth: String,
        expireYear: String,
        cvc: String,
        amount: Double,
        physiotherapistId: String
    ): Flow<Resource<PaymentResult>> = flow {
        emit(Resource.Loading())
        try {
            val result = withContext(Dispatchers.IO) {
                val options = Options()
                options.apiKey = "sandbox-JPTtLK9adQEuqFbSnvXFMRW2chaGdYZ3"
                options.secretKey = "sandbox-UT8tqQqdD713H6DGc0ucbkOs0vv9kQCb"
                options.baseUrl = "https://sandbox-api.iyzipay.com"

                val request = CreatePaymentRequest()
                request.locale = Locale.TR.toString()
                request.conversationId = UUID.randomUUID().toString()

                request.price = BigDecimal(amount.toString())
                request.paidPrice = BigDecimal(amount.toString())

                request.currency = Currency.TRY.name
                request.installment = 1
                request.basketId = "B${System.currentTimeMillis()}"
                request.paymentChannel = PaymentChannel.WEB.name
                request.paymentGroup = PaymentGroup.PRODUCT.name

                val paymentCard = PaymentCard()
                paymentCard.cardHolderName = cardHolderName
                paymentCard.cardNumber = cardNumber
                paymentCard.expireMonth = expireMonth
                paymentCard.expireYear = expireYear
                paymentCard.cvc = cvc
                paymentCard.registerCard = 0
                request.paymentCard = paymentCard

                val buyer = Buyer()
                buyer.id = "BY_${physiotherapistId}"
                buyer.name = "Fizyoterapist"
                buyer.surname = "Kullanıcı"
                buyer.email = "fizyoterapist@example.com"
                buyer.identityNumber = "74300864791"
                buyer.registrationAddress = "İstanbul, Türkiye"
                buyer.ip = "85.34.78.112"
                buyer.city = "Istanbul"
                buyer.country = "Turkey"
                buyer.zipCode = "34000"
                request.buyer = buyer

                val billingAddress = Address()
                billingAddress.contactName = "Fizyoterapist Kullanıcı"
                billingAddress.city = "Istanbul"
                billingAddress.country = "Turkey"
                billingAddress.address = "İstanbul, Türkiye"
                billingAddress.zipCode = "34000"
                request.billingAddress = billingAddress

                val shippingAddress = Address()
                shippingAddress.contactName = "Fizyoterapist Kullanıcı"
                shippingAddress.city = "Istanbul"
                shippingAddress.country = "Turkey"
                shippingAddress.address = "İstanbul, Türkiye"
                shippingAddress.zipCode = "34000"
                request.shippingAddress = shippingAddress

                val basketItems = ArrayList<BasketItem>()
                val item = BasketItem()
                item.id = "ADV${System.currentTimeMillis()}"
                item.name = "Reklam Hizmeti"
                item.category1 = "Reklam"
                item.category2 = "Dijital"
                item.itemType = BasketItemType.VIRTUAL.name

                item.price = BigDecimal(amount.toString())

                basketItems.add(item)
                request.basketItems = basketItems

                Payment.create(request, options)
            }

            if (result.status == "success") {
                emit(Resource.Success(
                    PaymentResult.Success(
                        paymentId = result.paymentId,
                        status = result.status,
                        amount = amount,
                        cardHolder = cardHolderName,
                        last4Digits = cardNumber.takeLast(4)
                    )
                ))
            } else {
                emit(Resource.Error(result.errorMessage ?: "Ödeme işlemi başarısız oldu"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Ödeme işlemi sırasında bir hata oluştu: ${e.message}"))
        }
    }
}