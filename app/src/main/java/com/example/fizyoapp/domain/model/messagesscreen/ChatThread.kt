package com.example.fizyoapp.domain.model.messagesscreen

import java.util.Date

data class ChatThread(
    val id:String,
    val participantIds:List<String> =emptyList(),
    val lastMessage:String="",
    val lastMessageTimestamp: Date =Date(),
    val unreadCount:Int=0,
    val otherParticipantName: String = "",
    val lastMessageSenderId: String = "", // Yeni alan eklendi
    val otherParticipantPhotoUrl: String = "",
)


//video aramayı sadece fizyoterapist yapabilsin istiyorum user girişli kişiler videocall yapamasın.Hatta videocall
//        ikonu bile user ekranında olmasın ki arayamasın.Bir de bir arama olduğunda ve cevap verilmediğinde mesaj olarak gözümeisni istiyorum.

  //      kişiye mesaj geldiğinde ve görmediğinde messages screende görmediğine dair bildirim ikonu gözükebilir

   //     ayrıca mesaj geldiğinde bottomnavbardaki bildirim ikonu da görünene kadar kaç mesaj geldiği hakkkında bilgi verebilir

