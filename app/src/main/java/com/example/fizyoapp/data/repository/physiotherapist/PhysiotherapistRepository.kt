package com.example.fizyoapp.data.repository.physiotherapist

import android.util.Log
import com.example.fizyoapp.data.model.search.SearchData
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PhysiotherapistRepository {
    private var _physioList = MutableStateFlow<List<SearchData>>(emptyList())
    val physioList = _physioList.asStateFlow()
    private val db = Firebase.firestore

    fun getPhysioList() {
        // "physiotherapist" koleksiyonunu kullanıyoruz (AuthRepositoryImpl ile aynı)
        db.collection("physiotherapist")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("FirestoreError", "Error getting physio list", error)
                    return@addSnapshotListener
                }
                if (value != null) {
                    _physioList.value = value.toObjects()
                }
            }
    }

    fun searchPhysio(name: String) {
        db.collection("physiotherapist")
            .get()
            .addOnSuccessListener { documents ->
                val filteredList = documents.mapNotNull { document ->
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