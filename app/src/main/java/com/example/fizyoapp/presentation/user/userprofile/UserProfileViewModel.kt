package com.example.fizyoapp.presentation.user.userprofile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.auth.User
import com.example.fizyoapp.domain.model.user_profile.UserProfile
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.user_profile.GetUserProfileUseCase
import com.example.fizyoapp.domain.usecase.user_profile.UpdateUserProfileUseCase
import com.example.fizyoapp.domain.usecase.user_profile.UploadProfilePhotoUseCase
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
class UserProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val uploadProfilePhotoUseCase: UploadProfilePhotoUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _state = MutableStateFlow(UserProfileState())
    val state: StateFlow<UserProfileState> = _state.asStateFlow()


    private val _currentUser = MutableStateFlow<User?>(null)

    init {
        savedStateHandle.get<String>("userId")?.let { userId ->
            loadUserProfile(userId)
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
                            loadUserProfile(user.id)
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

    private fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            getUserProfileUseCase(userId).collect { result ->
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
                            phoneNumber = profile.phoneNumber,
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

    fun onEvent(event: UserProfileEvent) {
        when (event) {
            is UserProfileEvent.FirstNameChanged -> {
                _state.value = _state.value.copy(
                    firstName = event.firstName,
                    firstNameError = event.firstName.isBlank()
                )
            }
            is UserProfileEvent.LastNameChanged -> {
                _state.value = _state.value.copy(
                    lastName = event.lastName,
                    lastNameError = event.lastName.isBlank()
                )
            }
            is UserProfileEvent.BirthDateChanged -> {
                _state.value = _state.value.copy(
                    birthDate = event.birthDate,
                    birthDateError = false
                )
            }
            is UserProfileEvent.GenderChanged -> {
                _state.value = _state.value.copy(
                    gender = event.gender,
                    genderError = false
                )
            }
            is UserProfileEvent.CityChanged -> {
                _state.value = _state.value.copy(
                    city = event.city,
                    cityError = event.city.isBlank()
                )
            }
            is UserProfileEvent.DistrictChanged -> {
                _state.value = _state.value.copy(
                    district = event.district,
                    districtError = event.district.isBlank()
                )
            }
            is UserProfileEvent.PhoneNumberChanged -> {
                _state.value = _state.value.copy(
                    phoneNumber = event.phoneNumber,
                    phoneNumberError = event.phoneNumber.isBlank()
                )
            }
            is UserProfileEvent.PhotoChanged -> {
                _state.value = _state.value.copy(
                    profilePhotoUrl = event.photoUrl
                )
            }
            is UserProfileEvent.SaveProfile -> {
                saveProfile()
            }
            is UserProfileEvent.ResetState -> {
                _state.value = UserProfileState(userId = _state.value.userId)
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
        val phoneNumberError = currentState.phoneNumber.isBlank()

        // Eğer herhangi bir hata varsa, state'i güncelle ve işlemi durdur
        if (firstNameError || lastNameError || birthDateError || genderError ||
            cityError || districtError || phoneNumberError) {
            _state.value = _state.value.copy(
                firstNameError = firstNameError,
                lastNameError = lastNameError,
                birthDateError = birthDateError,
                genderError = genderError,
                cityError = cityError,
                districtError = districtError,
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

                var finalPhotoUrl = currentState.profilePhotoUrl

                if (finalPhotoUrl.isNotEmpty() &&
                    !finalPhotoUrl.startsWith("http") &&
                    !finalPhotoUrl.startsWith("https")) {


                    val result = uploadPhoto(finalPhotoUrl, currentState.userId)

                    finalPhotoUrl = when (result) {
                        is Resource.Success -> {
                            Log.d("UserProfileViewModel", "Photo uploaded successfully: ${result.data}")
                            result.data
                        }
                        is Resource.Error -> {
                            Log.e("UserProfileViewModel", "Photo upload failed: ${result.message}")
                            ""
                        }
                        is Resource.Loading -> {
                            ""
                        }
                    }
                }

                val userProfile = UserProfile(
                    userId = currentState.userId,
                    firstName = currentState.firstName,
                    lastName = currentState.lastName,
                    birthDate = currentState.birthDate,
                    gender = currentState.gender,
                    city = currentState.city,
                    district = currentState.district,
                    phoneNumber = currentState.phoneNumber,
                    profilePhotoUrl = finalPhotoUrl,
                    isProfileCompleted = true
                )


                updateUserProfileUseCase(userProfile).collect { result ->
                    when (result) {
                        is Resource.Loading -> {

                        }
                        is Resource.Success -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                isProfileSaved = true,
                                errorMessage = null,
                                profilePhotoUrl = finalPhotoUrl
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
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "İşlem sırasında bir hata oluştu: ${e.localizedMessage}"
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
                Log.e("UserProfileViewModel", "Photo file does not exist: $photoPath")
                return Resource.Error("Fotoğraf dosyası bulunamadı: $photoPath")
            }

            var uploadResult: Resource<String> = Resource.Loading()

            withContext(Dispatchers.IO) {
                val fileUri = Uri.fromFile(photoFile)
                uploadProfilePhotoUseCase(fileUri.toString(), userId).collect {
                    uploadResult = it
                }
            }

            uploadResult

        } catch (e: Exception) {
            Log.e("UserProfileViewModel", "Error uploading photo", e)
            Resource.Error("Fotoğraf yükleme hatası: ${e.localizedMessage}", e)
        }
    }
}