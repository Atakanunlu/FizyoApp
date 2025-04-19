package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_profile_screen

import java.util.Date

sealed class PhysiotherapistProfileEvent {
    data class FirstNameChanged(val firstName: String) : PhysiotherapistProfileEvent()
    data class LastNameChanged(val lastName: String) : PhysiotherapistProfileEvent()
    data class BirthDateChanged(val birthDate: Date) : PhysiotherapistProfileEvent()
    data class GenderChanged(val gender: String) : PhysiotherapistProfileEvent()
    data class CityChanged(val city: String) : PhysiotherapistProfileEvent()
    data class DistrictChanged(val district: String) : PhysiotherapistProfileEvent()
    data class FullAddressChanged(val fullAddress: String) : PhysiotherapistProfileEvent()
    data class PhoneNumberChanged(val phoneNumber: String) : PhysiotherapistProfileEvent()
    data class CertificatesChanged(val certificates: List<String>) : PhysiotherapistProfileEvent()
    data class PriceInfoChanged(val priceInfo: String) : PhysiotherapistProfileEvent()
    data class PhotoChanged(val photoUri: String) : PhysiotherapistProfileEvent()
    data object PhotoRemoved : PhysiotherapistProfileEvent()
    data object SaveProfile : PhysiotherapistProfileEvent()
    data object ResetState : PhysiotherapistProfileEvent()
}