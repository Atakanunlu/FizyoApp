package com.example.fizyoapp.domain.model.messagesscreen

import java.util.Date

data class Message(
   val id:String="",
   val senderId:String="",
   val receiverId:String="",
   val content:String="",
   val timestamp: Date =Date(),
   val isRead:Boolean=false,
   val threadId:String=""
)


