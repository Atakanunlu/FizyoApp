package com.example.fizyoapp.presentation.user.userprofile

import java.util.Date

sealed class UserProfileEvent {
    data class FirstNameChanged(val firstName: String): UserProfileEvent()
    data class LastNameChanged(val lastName: String): UserProfileEvent()
    data class BirthDateChanged(val birthDate: Date): UserProfileEvent()
    data class GenderChanged(val gender: String): UserProfileEvent()
    data class CityChanged(val city: String): UserProfileEvent()
    data class DistrictChanged(val district: String): UserProfileEvent()
    data class PhoneNumberChanged(val phoneNumber: String): UserProfileEvent()
    data class PhotoChanged(val photoUrl: String): UserProfileEvent()
    data object SaveProfile: UserProfileEvent()
    data object ResetState: UserProfileEvent()
}