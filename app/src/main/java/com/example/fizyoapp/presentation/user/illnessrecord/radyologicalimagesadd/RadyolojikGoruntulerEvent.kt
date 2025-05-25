package com.example.fizyoapp.presentation.user.illnessrecord.radyologicalimagesadd

import android.net.Uri


sealed class RadyolojikGoruntulerEvent {
    object RefreshData : RadyolojikGoruntulerEvent()
    object DismissError : RadyolojikGoruntulerEvent()

    data class FileSelected(val uri: Uri) : RadyolojikGoruntulerEvent()

    data class AddImage(
        val title: String,
        val description: String
    ) : RadyolojikGoruntulerEvent()


    data class AddPdf(
        val title: String,
        val description: String
    ) : RadyolojikGoruntulerEvent()

    data class ShareImage(val imageId: String, val userId: String) : RadyolojikGoruntulerEvent()
    data class DeleteImage(val fileUrl: String) : RadyolojikGoruntulerEvent()}
