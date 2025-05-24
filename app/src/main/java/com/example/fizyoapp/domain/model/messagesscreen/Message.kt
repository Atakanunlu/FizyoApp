package com.example.fizyoapp.domain.model.messagesscreen

import java.util.Date

data class Message(
   val id: String = "",
   val senderId: String = "",
   val receiverId: String = "",
   val content: String = "",
   val timestamp: Date = Date(),
   val isRead: Boolean = false,
   val threadId: String = "",
   val messageType: String = "text", // Yeni alan: text, video_call, missed_video_call, vs.
   val metadata: Map<String, Any> = emptyMap() // İlave bilgiler için (süre, durum vb.)
)


