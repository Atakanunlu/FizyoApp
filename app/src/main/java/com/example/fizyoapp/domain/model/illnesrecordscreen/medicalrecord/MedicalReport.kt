package com.example.fizyoapp.domain.model.illnesrecordscreen.medicalrecord

import java.util.Date

data class MedicalReport(
    val id: String,
    val title: String,
    val description: String,
    val fileUrl: String,
    val thumbnailUrl: String,
    val timestamp: Date,
    val userId: String,
    val doctorName: String = "",
    val hospitalName: String = "",
    val fileType: String = "pdf" )