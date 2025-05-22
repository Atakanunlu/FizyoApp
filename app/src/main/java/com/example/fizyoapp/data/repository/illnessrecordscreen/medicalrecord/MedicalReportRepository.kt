package com.example.fizyoapp.data.repository.illnessrecordscreen.medicalrecord

import android.net.Uri
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.illnesrecordscreen.medicalrecord.MedicalReport
import kotlinx.coroutines.flow.Flow

interface MedicalReportRepository {
    fun getMedicalReports(userId: String): Flow<Resource<List<MedicalReport>>>
    fun uploadMedicalReport(
        fileUri: Uri,
        title: String,
        description: String,
        userId: String,
        doctorName: String = "",
        hospitalName: String = ""
    ): Flow<Resource<MedicalReport>>
    fun deleteMedicalReport(fileUrl: String): Flow<Resource<Boolean>>
}