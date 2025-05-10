package com.example.fizyoapp.domain.model.note

import java.util.Date

data class Note(
    val id: String = "",
    val physiotherapistId: String = "",
    val patientName: String = "",
    val title: String = "",
    val content: String = "",
    val creationDate: Date = Date(),
    val updateDate: Date = Date(),
    val color: NoteColor = NoteColor.WHITE,
    val updates: List<NoteUpdate> = emptyList()
)