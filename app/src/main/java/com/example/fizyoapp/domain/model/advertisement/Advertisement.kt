package com.example.fizyoapp.domain.model.advertisement

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Advertisement(
    @DocumentId
    val id: String = "",

    @PropertyName("physiotherapistId")
    val physiotherapistId: String = "",

    @PropertyName("imageUrl")
    val imageUrl: String = "",

    @PropertyName("description")
    val description: String = "",

    @PropertyName("paymentId")
    val paymentId: String = "",

    @PropertyName("createdAt")
    val createdAt: Timestamp = Timestamp.now(),

    @PropertyName("expiresAt")
    val expiresAt: Timestamp = Timestamp.now(),

    @PropertyName("isActive")
    val isActive: Boolean = true
)