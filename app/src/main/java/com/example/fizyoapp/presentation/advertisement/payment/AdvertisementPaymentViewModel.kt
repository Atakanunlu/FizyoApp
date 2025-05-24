package com.example.fizyoapp.presentation.advertisement.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.repository.advertisement.AdvertisementDataRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.payment.PaymentResult
import com.example.fizyoapp.domain.usecase.advertisement.CreateAdvertisementUseCase
import com.example.fizyoapp.domain.usecase.auth.GetCurrentPhysiotherapistUseCase
import com.example.fizyoapp.domain.usecase.payment.MakePaymentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdvertisementPaymentViewModel @Inject constructor(
    private val makePaymentUseCase: MakePaymentUseCase,
    private val createAdvertisementUseCase: CreateAdvertisementUseCase,
    private val getCurrentPhysiotherapistUseCase: GetCurrentPhysiotherapistUseCase,
    private val advertisementDataRepository: AdvertisementDataRepository
) : ViewModel() {
    private val _state = MutableStateFlow(PaymentState())
    val state: StateFlow<PaymentState> = _state.asStateFlow()

    private val _eventChannel = Channel<UIEvent>()
    val eventFlow = _eventChannel.receiveAsFlow()

    init {
        _state.update {
            it.copy(
                cardHolderName = "Test User",
                cardNumber = "5528790000000008",
                expireMonth = "12",
                expireYear = "2030",
                cvc = "123"
            )
        }
        validateForm()
    }

    fun onCardHolderNameChanged(value: String) {
        _state.update { it.copy(cardHolderName = value) }
        validateForm()
    }

    fun onCardNumberChanged(value: String) {
        val filteredValue = value.replace("[^0-9]".toRegex(), "")
        val limitedValue = filteredValue.take(16)
        _state.update { it.copy(cardNumber = limitedValue) }
        validateForm()
    }

    fun onExpireMonthChanged(value: String) {
        val filteredValue = value.replace("[^0-9]".toRegex(), "").take(2)
        _state.update { it.copy(expireMonth = filteredValue) }
        validateForm()
    }

    fun onExpireYearChanged(value: String) {
        val filteredValue = value.replace("[^0-9]".toRegex(), "").take(4)
        _state.update { it.copy(expireYear = filteredValue) }
        validateForm()
    }

    fun onCvcChanged(value: String) {
        val filteredValue = value.replace("[^0-9]".toRegex(), "").take(3)
        _state.update { it.copy(cvc = filteredValue) }
        validateForm()
    }

    private fun validateForm() {
        val isFormValid = state.value.cardHolderName.isNotBlank() &&
                state.value.cardNumber.length >= 16 &&
                state.value.expireMonth.length == 2 &&
                state.value.expireYear.length == 4 &&
                state.value.cvc.length == 3
        _state.update { it.copy(isFormValid = isFormValid) }
    }

    fun onPayClicked() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getCurrentPhysiotherapistUseCase().collect { userResult ->
                when (userResult) {
                    is Resource.Success -> {
                        val physiotherapistId = userResult.data.id
                        makePaymentUseCase(
                            cardHolderName = state.value.cardHolderName,
                            cardNumber = state.value.cardNumber,
                            expireMonth = state.value.expireMonth,
                            expireYear = state.value.expireYear,
                            cvc = state.value.cvc,
                            amount = 50.0,
                            physiotherapistId = physiotherapistId
                        ).collect { paymentResult ->
                            when (paymentResult) {
                                is Resource.Success -> {
                                    when (val result = paymentResult.data) {
                                        is PaymentResult.Success -> {
                                            createAdvertisement(
                                                physiotherapistId = physiotherapistId,
                                                paymentId = result.paymentId
                                            )
                                        }
                                        is PaymentResult.Error -> {
                                            _state.update {
                                                it.copy(
                                                    isLoading = false,
                                                    error = result.message
                                                )
                                            }
                                        }
                                        is PaymentResult.Loading -> {
                                            _state.update { it.copy(isLoading = true) }
                                        }
                                    }
                                }
                                is Resource.Error -> {
                                    _state.update {
                                        it.copy(
                                            isLoading = false,
                                            error = paymentResult.message ?: "Ödeme işlemi başarısız oldu"
                                        )
                                    }
                                }
                                is Resource.Loading -> {
                                    _state.update { it.copy(isLoading = true) }
                                }
                            }
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = userResult.message ?: "Kullanıcı bilgisi alınamadı"
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    private fun createAdvertisement(physiotherapistId: String, paymentId: String) {
        viewModelScope.launch {
            val imageUri = advertisementDataRepository.imageUri.value
            val description = advertisementDataRepository.description.value ?: ""
            if (imageUri == null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Reklam görseli seçilmedi"
                    )
                }
                return@launch
            }
            createAdvertisementUseCase(
                physiotherapistId = physiotherapistId,
                imageUri = imageUri,
                description = description,
                paymentId = paymentId
            ).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update { it.copy(isLoading = false) }
                        advertisementDataRepository.clear()
                        _eventChannel.send(UIEvent.NavigateToSuccess)
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Reklam oluşturulurken bir hata oluştu"
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    sealed class UIEvent {
        object NavigateToSuccess : UIEvent()
    }
}