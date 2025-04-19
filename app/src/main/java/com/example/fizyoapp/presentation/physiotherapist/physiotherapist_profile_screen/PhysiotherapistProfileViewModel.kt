package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_profile_screen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.auth.User
import com.example.fizyoapp.domain.model.physiotherapist_profile.PhysiotherapistProfile
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.physiotherapist_profile.GetPhysiotherapistProfileUseCase
import com.example.fizyoapp.domain.usecase.physiotherapist_profile.UpdatePhysiotherapistProfileUseCase
import com.example.fizyoapp.domain.usecase.physiotherapist_profile.UploadPhysiotherapistProfilePhotoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhysiotherapistProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUseCase,
    private val getPhysiotherapistProfileUseCase: GetPhysiotherapistProfileUseCase,
    private val updatePhysiotherapistProfileUseCase: UpdatePhysiotherapistProfileUseCase,
    private val uploadPhysiotherapistProfilePhotoUseCase: UploadPhysiotherapistProfilePhotoUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _state = MutableStateFlow(PhysiotherapistProfileState())
    val state: StateFlow<PhysiotherapistProfileState> = _state.asStateFlow()


    private val _currentUser = MutableStateFlow<User?>(null)

    init {
        savedStateHandle.get<String>("userId")?.let { userId ->
            loadPhysiotherapistProfile(userId)
        } ?: run {
            loadCurrentUser()
        }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }

                    is Resource.Success -> {
                        val user = result.data
                        if (user != null) {
                            _currentUser.value = user
                            loadPhysiotherapistProfile(user.id)
                        } else {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                errorMessage = "Kullanıcı bulunamadı"
                            )
                        }
                    }

                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    private fun loadPhysiotherapistProfile(userId: String) {
        viewModelScope.launch {
            getPhysiotherapistProfileUseCase(userId).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }

                    is Resource.Success -> {
                        val profile = result.data
                        _state.value = _state.value.copy(
                            isLoading = false,
                            userId = profile.userId,
                            firstName = profile.firstName,
                            lastName = profile.lastName,
                            birthDate = profile.birthDate,
                            gender = profile.gender,
                            city = profile.city,
                            district = profile.district,
                            fullAddress = profile.fullAddress,
                            phoneNumber = profile.phoneNumber,
                            certificates = profile.certificates,
                            priceInfo = profile.priceInfo,
                            profilePhotoUrl = profile.profilePhotoUrl
                        )

                    }

                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: PhysiotherapistProfileEvent) {
        when (event) {
            is PhysiotherapistProfileEvent.FirstNameChanged -> {
                _state.value = _state.value.copy(
                    firstName = event.firstName,
                    firstNameError = event.firstName.isBlank()
                )
            }

            is PhysiotherapistProfileEvent.LastNameChanged -> {
                _state.value = _state.value.copy(
                    lastName = event.lastName,
                    lastNameError = event.lastName.isBlank()
                )
            }

            is PhysiotherapistProfileEvent.BirthDateChanged -> {
                _state.value = _state.value.copy(
                    birthDate = event.birthDate,
                    birthDateError = false
                )
            }

            is PhysiotherapistProfileEvent.GenderChanged -> {
                _state.value = _state.value.copy(
                    gender = event.gender,
                    genderError = false
                )
            }

            is PhysiotherapistProfileEvent.CityChanged -> {
                _state.value = _state.value.copy(
                    city = event.city,
                    cityError = event.city.isBlank()
                )
            }

            is PhysiotherapistProfileEvent.DistrictChanged -> {
                _state.value = _state.value.copy(
                    district = event.district,
                    districtError = event.district.isBlank()
                )
            }

            is PhysiotherapistProfileEvent.FullAddressChanged -> {
                _state.value = _state.value.copy(
                    fullAddress = event.fullAddress,
                    fullAddressError = event.fullAddress.isBlank()
                )
            }

            is PhysiotherapistProfileEvent.PhoneNumberChanged -> {
                _state.value = _state.value.copy(
                    phoneNumber = event.phoneNumber,
                    phoneNumberError = event.phoneNumber.isBlank()
                )
            }

            is PhysiotherapistProfileEvent.CertificatesChanged -> {
                _state.value = _state.value.copy(
                    certificates = event.certificates
                )
            }

            is PhysiotherapistProfileEvent.PriceInfoChanged -> {
                _state.value = _state.value.copy(
                    priceInfo = event.priceInfo,
                    priceInfoError = false
                )
            }

            is PhysiotherapistProfileEvent.PhotoChanged -> {
                _state.value = _state.value.copy(
                    tempPhotoUri = event.photoUri
                )

            }

            is PhysiotherapistProfileEvent.PhotoRemoved -> {
                _state.value = _state.value.copy(
                    tempPhotoUri = "",
                    profilePhotoUrl = ""
                )

            }

            is PhysiotherapistProfileEvent.SaveProfile -> {
                saveProfile()
            }

            is PhysiotherapistProfileEvent.ResetState -> {
                _state.value = PhysiotherapistProfileState(userId = _state.value.userId)
            }

        }
    }

    private fun saveProfile() {
        val currentState = _state.value

        val firstNameError = currentState.firstName.isBlank()
        val lastNameError = currentState.lastName.isBlank()
        val birthDateError = currentState.birthDate == null
        val genderError = currentState.gender.isBlank()
        val cityError = currentState.city.isBlank()
        val districtError = currentState.district.isBlank()
        val fullAddressError = currentState.fullAddress.isBlank()
        val phoneNumberError = currentState.phoneNumber.isBlank()

        if (firstNameError || lastNameError || birthDateError || genderError ||
            cityError || districtError || fullAddressError || phoneNumberError
        ) {
            _state.value = _state.value.copy(
                firstNameError = firstNameError,
                lastNameError = lastNameError,
                birthDateError = birthDateError,
                genderError = genderError,
                cityError = cityError,
                districtError = districtError,
                fullAddressError = fullAddressError,
                phoneNumberError = phoneNumberError,
                errorMessage = "Lütfen zorunlu alanları doldurun"
            )
            return
        }

        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(
                    isLoading = true,
                    errorMessage = null
                )

                val finalPriceInfo = currentState.priceInfo.ifBlank { "Görüşme sonunda bilgilendirilecektir" }


                var finalPhotoUrl = currentState.profilePhotoUrl


                if (currentState.tempPhotoUri.isNotEmpty()) {
                    try {
                        // Fotoğrafı yükle
                        uploadPhysiotherapistProfilePhotoUseCase(
                            currentState.tempPhotoUri,
                            currentState.userId
                        ).collect { uploadResult ->
                            when (uploadResult) {
                                is Resource.Success -> {
                                    finalPhotoUrl = uploadResult.data
                                }

                                is Resource.Error -> {
                                    throw Exception("Fotoğraf yüklenirken hata oluştu")
                                }

                                else -> {}
                            }
                        }
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            errorMessage = "Fotoğraf yükleme işleminde hata oluştu"
                        )
                        return@launch
                    }
                }

                val profile = PhysiotherapistProfile(
                    userId = currentState.userId,
                    firstName = currentState.firstName,
                    lastName = currentState.lastName,
                    birthDate = currentState.birthDate,
                    gender = currentState.gender,
                    city = currentState.city,
                    district = currentState.district,
                    fullAddress = currentState.fullAddress,
                    phoneNumber = currentState.phoneNumber,
                    certificates = currentState.certificates,
                    priceInfo = finalPriceInfo,
                    profilePhotoUrl = finalPhotoUrl,
                    isProfileCompleted = true
                )

                updatePhysiotherapistProfileUseCase(profile).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                isProfileSaved = true,
                                profilePhotoUrl = finalPhotoUrl,
                                tempPhotoUri = "",
                                priceInfo = finalPriceInfo
                            )
                        }

                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                errorMessage = "Profil güncellenirken bir hata oluştu"
                            )
                        }

                        else -> {}
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "İşlem sırasında bir hata oluştu"
                )
            }
        }
    }
}