package com.example.fizyoapp.ui.bottomnavbar.items.messagesscreen.data.messagesviewmodel

import androidx.lifecycle.ViewModel
import com.example.fizyoapp.ui.bottomnavbar.items.messagesscreen.data.messagesrepository.FireStoreMessagesRepository
import com.example.fizyoapp.ui.bottomnavbar.items.messagesscreen.data.messagesrepository.MessagesRepository

class MessagesViewModel(private val repository: FireStoreMessagesRepository):ViewModel() {


    val messagesList = repository.messages

    init {
        repository.getAllMessages()
    }

    fun gettAllData(){
        repository.getAllMessages()
    }

    fun searchMessagesData(name:String){
        repository.SearchMessages(name)
    }



}