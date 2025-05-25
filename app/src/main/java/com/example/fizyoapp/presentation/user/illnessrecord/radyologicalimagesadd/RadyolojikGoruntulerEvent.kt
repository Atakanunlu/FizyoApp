package com.example.fizyoapp.presentation.user.illnessrecord.radyologicalimagesadd

import android.net.Uri


sealed class RadyolojikGoruntulerEvent {
    object RefreshData : RadyolojikGoruntulerEvent()
    object DismissError : RadyolojikGoruntulerEvent()

    // Dosya seçimi için tek bir event
    data class FileSelected(val uri: Uri) : RadyolojikGoruntulerEvent()

    // Görsel ekleme
    data class AddImage(
        val title: String,
        val description: String
    ) : RadyolojikGoruntulerEvent()

    // PDF ekleme
    data class AddPdf(
        val title: String,
        val description: String
    ) : RadyolojikGoruntulerEvent()

    data class ShareImage(val imageId: String, val userId: String) : RadyolojikGoruntulerEvent()
    data class DeleteImage(val fileUrl: String) : RadyolojikGoruntulerEvent()}
