package com.example.fizyoapp.ui.bottomnavbar.items.searchscreen.data.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fizyoapp.ui.bottomnavbar.items.searchscreen.data.FizyoterapistlerEntity
import com.example.fizyoapp.ui.bottomnavbar.items.searchscreen.data.repository.FizyoterapistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch




class FizyoterapistViewModel constructor(
    private val repository: FizyoterapistRepository
) : ViewModel() {


    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val filteredList = _searchQuery.flatMapLatest { query ->
        repository.searchByName(query)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    var entities= mutableListOf<FizyoterapistlerEntity>()
    private fun refreshNotes() {
        viewModelScope.launch {
            entities = repository.getAll().toMutableList()
        }

    }
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }


}
