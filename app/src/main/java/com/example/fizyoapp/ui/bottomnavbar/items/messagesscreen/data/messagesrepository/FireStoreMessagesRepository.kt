package com.example.fizyoapp.ui.bottomnavbar.items.messagesscreen.data.messagesrepository

import android.util.Log
import com.example.fizyoapp.ui.bottomnavbar.items.messagesscreen.data.model.MessagesData
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FireStoreMessagesRepository():MessagesRepository {

    private val db=Firebase.firestore
    private var _message = MutableStateFlow<List<MessagesData>>(emptyList())
    val messages = _message.asStateFlow()

   override fun getAllMessages(){
        db.collection("messages")
            .addSnapshotListener{value,error ->
                if(error != null){
                    return@addSnapshotListener
                }
                if(value != null){
                    _message.value=value.toObjects()
                }
            }
    }

   override fun SearchMessages(searchText:String){
        db.collection("messages")
            .get()
            .addOnSuccessListener {documents ->
                val filteredList= documents.mapNotNull {document->
                    document.toObject(MessagesData::class.java)
                }.filter {
                    it.name?.lowercase()?.contains(searchText.lowercase()) == true
                }
                _message.value=filteredList
            }
            .addOnFailureListener {exception ->
                Log.e("FirestoreError", "Error searching physio", exception)

            }
    }
}