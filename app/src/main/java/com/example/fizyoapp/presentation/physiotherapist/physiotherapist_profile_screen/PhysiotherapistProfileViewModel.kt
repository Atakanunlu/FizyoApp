package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_profile_screen

import android.net.Uri
import android.util.Log
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
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
                Log.d("PhysiotherapistProfileViewModel", "Photo URI set: ${event.photoUri}")
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

                val currentState = _state.value
                val finalPriceInfo = currentState.priceInfo.ifBlank { "Görüşme sonunda bilgilendirilecektir" }

                // Önce boş bir profil oluşturalım, böylece doküman var olacak
                val initialProfile = PhysiotherapistProfile(
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
                    isProfileCompleted = false
                )

                updatePhysiotherapistProfileUseCase(initialProfile).collect { result ->
                    if (result is Resource.Error) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            errorMessage = "Profil oluşturulurken bir hata oluştu: ${result.message}"
                        )
                        return@collect
                    }
                }

                var finalPhotoUrl = currentState.profilePhotoUrl
                if (currentState.tempPhotoUri.isNotEmpty() &&
                    !currentState.tempPhotoUri.startsWith("http") &&
                    !currentState.tempPhotoUri.startsWith("https")) {

                    val result = uploadPhoto(currentState.tempPhotoUri, currentState.userId)
                    finalPhotoUrl = when (result) {
                        is Resource.Success -> result.data ?: ""
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                errorMessage = "Fotoğraf yükleme işleminde hata: ${result.message}"
                            )
                            return@launch
                        }
                        is Resource.Loading -> ""
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
                                errorMessage = "Profil güncellenirken bir hata oluştu: ${result.message}"
                            )
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Profil kaydetme hatası: ${e.message}")
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "İşlem sırasında bir hata oluştu: ${e.message}"
                )
            }
        }
    }

    private suspend fun uploadPhoto(photoPath: String, userId: String): Resource<String> {
        return try {
            if (photoPath.isEmpty()) {
                return Resource.Success("")
            }

            val photoFile = File(photoPath)
            if (!photoFile.exists()) {
                Log.e("PhysiotherapistProfileViewModel", "Fotoğraf dosyası bulunamadı: $photoPath")
                return Resource.Error("Fotoğraf dosyası bulunamadı: $photoPath")
            }

            var uploadResult: Resource<String> = Resource.Loading()
            withContext(Dispatchers.IO) {
                val fileUri = Uri.fromFile(photoFile)
                uploadPhysiotherapistProfilePhotoUseCase(fileUri.toString(), userId).collect {
                    uploadResult = it
                }
            }

            uploadResult
        } catch (e: Exception) {
            Log.e("PhysiotherapistProfileViewModel", "Fotoğraf yükleme hatası", e)
            Resource.Error("Fotoğraf yükleme hatası: ${e.localizedMessage}", e)
        }
    }
}