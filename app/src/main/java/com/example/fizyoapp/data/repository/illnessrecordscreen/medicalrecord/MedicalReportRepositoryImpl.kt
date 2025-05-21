package com.example.fizyoapp.data.repository.illnessrecordscreen.medicalreport

import android.net.Uri
import android.util.Log
import com.example.fizyoapp.data.repository.illnessrecordscreen.medicalrecord.MedicalReportRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.illnesrecordscreen.medicalrecord.MedicalReport

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class MedicalReportRepositoryImpl @Inject constructor(
    private val storage: FirebaseStorage
) : MedicalReportRepository {

    companion object {
        private const val TAG = "MedicalReportRepo"
        private const val REPORTS_PATH = "medicalReports"
    }

    override fun getMedicalReports(userId: String): Flow<Resource<List<MedicalReport>>> = callbackFlow {
        trySend(Resource.Loading())

        try {
            val reportsRef = storage.reference.child("$REPORTS_PATH/$userId")
            val result = reportsRef.listAll().await()

            val reports = mutableListOf<MedicalReport>()

            for (item in result.items) {
                try {
                    val metadata = item.metadata.await()

                    // Meta veriler metadata'nın customMetadata alanından alınır
                    val reportTitle = metadata.getCustomMetadata("title") ?: "Untitled Report"
                    val reportDescription = metadata.getCustomMetadata("description") ?: ""
                    val reportDoctorName = metadata.getCustomMetadata("doctorName") ?: ""
                    val reportHospitalName = metadata.getCustomMetadata("hospitalName") ?: ""
                    val timestampStr = metadata.getCustomMetadata("timestamp") ?: "${Date().time}"
                    val timestamp = Date(timestampStr.toLong())

                    val fileUrl = item.downloadUrl.await().toString()
                    val reportId = item.name

                    reports.add(
                        MedicalReport(
                            id = reportId,
                            title = reportTitle,
                            description = reportDescription,
                            fileUrl = fileUrl,
                            thumbnailUrl = fileUrl, // PDF için aynı URL'i kullanabiliriz
                            timestamp = timestamp,
                            userId = userId,
                            doctorName = reportDoctorName,
                            hospitalName = reportHospitalName
                        )
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting report metadata: ${e.message}")
                }
            }

            // Raporları tarihe göre sırala, en yeniler en üstte
            reports.sortByDescending { it.timestamp }

            trySend(Resource.Success(reports))
        } catch (e: Exception) {
            Log.e(TAG, "Error listing reports: ${e.message}")
            trySend(Resource.Error(e.localizedMessage ?: "Failed to load medical reports"))
        }

        awaitClose()
    }

    override fun uploadMedicalReport(
        fileUri: Uri,
        title: String,
        description: String,
        userId: String,
        doctorName: String,
        hospitalName: String
    ): Flow<Resource<MedicalReport>> = callbackFlow {
        trySend(Resource.Loading())

        try {
            val fileId = UUID.randomUUID().toString()
            val timestamp = Date().time

            // Custom metadata oluştur
            val metadata = StorageMetadata.Builder()
                .setContentType("application/pdf")
                .setCustomMetadata("title", title)
                .setCustomMetadata("description", description)
                .setCustomMetadata("userId", userId)
                .setCustomMetadata("doctorName", doctorName)
                .setCustomMetadata("hospitalName", hospitalName)
                .setCustomMetadata("timestamp", timestamp.toString())
                .build()

            val storageRef = storage.reference.child("$REPORTS_PATH/$userId/$fileId")

            // Dosyayı metadata ile birlikte yükle
            storageRef.putFile(fileUri, metadata).await()
            val fileUrl = storageRef.downloadUrl.await().toString()

            val report = MedicalReport(
                id = fileId,
                title = title,
                description = description,
                fileUrl = fileUrl,
                thumbnailUrl = fileUrl, // PDF için aynı URL'i kullanabiliriz
                timestamp = Date(timestamp),
                userId = userId,
                doctorName = doctorName,
                hospitalName = hospitalName
            )

            trySend(Resource.Success(report))
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading report: ${e.message}")
            trySend(Resource.Error(e.localizedMessage ?: "An error occurred while uploading the report"))
        }

        awaitClose()
    }

    override fun deleteMedicalReport(fileUrl: String): Flow<Resource<Boolean>> = callbackFlow {
        trySend(Resource.Loading())

        try {
            // Storage'dan URL ile referans al
            val storageRef = storage.getReferenceFromUrl(fileUrl)

            // Dosyayı sil
            storageRef.delete().await()

            trySend(Resource.Success(true))
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting report: ${e.message}")
            trySend(Resource.Error(e.localizedMessage ?: "An error occurred while deleting the report"))
        }

        awaitClose()
    }
}