package com.example.fizyoapp.presentation.user.illnessrecord.medicalrecord

import android.net.Uri

sealed class MedicalReportEvent {
    object RefreshData : MedicalReportEvent()
    object DismissError : MedicalReportEvent()
    data class FileSelected(val uri: Uri) : MedicalReportEvent()
    data class AddReport(
        val title: String,
        val description: String,
        val doctorName: String = "",
        val hospitalName: String = ""
    ) : MedicalReportEvent()
    data class ShareReport(val reportId: String, val userId: String) : MedicalReportEvent()
    data class DeleteReport(val fileUrl: String) : MedicalReportEvent()
}