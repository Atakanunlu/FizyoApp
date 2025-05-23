package com.example.fizyoapp.domain.model.note

import java.util.Date

data class NoteUpdate(
    val updateText: String = "",
    val updateDate: Date = Date(),
    val images: List<String> = emptyList(),
    val documents: List<String> = emptyList()
)

data class Attachment(
    val id: String = "",
    val url: String = "",
    val name: String = "",
    val type: AttachmentType = AttachmentType.IMAGE,
    val uploadDate: Date = Date()
)

enum class AttachmentType {
    IMAGE,
    DOCUMENT
}