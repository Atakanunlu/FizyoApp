package com.example.fizyoapp.ui.bottomnavbar.items.messagesscreen.data.messagesrepository

interface MessagesRepository {


    fun getAllMessages()
    fun SearchMessages(searchText:String)
}