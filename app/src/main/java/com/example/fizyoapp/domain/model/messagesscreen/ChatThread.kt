package com.example.fizyoapp.domain.model.messagesscreen

import java.util.Date

data class ChatThread(
    val id:String,
    val participantIds:List<String> =emptyList(),
    val lastMessage:String="",
    val lastMessageTimestamp: Date =Date(),
    val unreadCount:Int=0,
    val otherParticipantName: String = "",
    val lastMessageSenderId: String = "",
    val otherParticipantPhotoUrl: String = "",
)
