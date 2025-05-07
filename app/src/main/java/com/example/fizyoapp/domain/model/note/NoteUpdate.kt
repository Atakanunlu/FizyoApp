package com.example.fizyoapp.domain.model.note

import java.util.Date

data class NoteUpdate(
    val updateText: String = "",
    val updateDate: Date = Date()
)