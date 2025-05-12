package com.example.fizyoapp.presentation.user.usermainscreen

import com.example.fizyoapp.domain.model.usermainscreen.PainRecord

sealed class PainTrackingEvent {
    data class UpdatePainIntensity(val intensity: Int) : PainTrackingEvent()
    data class UpdatePainLocation(val location: String) : PainTrackingEvent()
    data class UpdatePainDescription(val description: String) : PainTrackingEvent()
    object ToggleAddRecord : PainTrackingEvent()
    object SavePainRecord : PainTrackingEvent()
    data class EditPainRecord(val painRecord: PainRecord) : PainTrackingEvent()
    data class DeletePainRecord(val painRecordId: String) : PainTrackingEvent()
    object DismissError : PainTrackingEvent()
    object RefreshData : PainTrackingEvent()
}