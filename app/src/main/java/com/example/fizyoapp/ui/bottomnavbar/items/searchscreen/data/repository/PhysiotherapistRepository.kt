package com.example.fizyoapp.ui.bottomnavbar.items.searchscreen.data.repository

import android.util.Log
import com.example.fizyoapp.ui.bottomnavbar.items.searchscreen.data.model.SearchData
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PhysiotherapistRepository {

    private var _physioList= MutableStateFlow<List<SearchData>>(emptyList())
    val physioList=_physioList.asStateFlow()
    private val db = Firebase.firestore



    fun getPhysioList(){
        db.collection("physioterapists")
            .addSnapshotListener{value,error ->
                if(error != null){
                    return@addSnapshotListener
                }

                if(value != null){
                    _physioList.value=value.toObjects()
                }
            }

    }

    fun searchPhysio(name: String) {
        db.collection("physioterapists")
            .get()
            .addOnSuccessListener { documents ->
                val filteredList = documents.mapNotNull {document ->
                    document.toObject(SearchData::class.java)
                }.filter {
                    it.ptName?.lowercase()?.contains(name.lowercase()) == true
                }
                _physioList.value = filteredList
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Error searching physio", exception)
            }
    }



}