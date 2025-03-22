package com.example.fizyoapp.ui.bottomnavbar.items.searchscreen.data.viewmodel

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fizyoapp.ui.bottomnavbar.items.searchscreen.data.model.SearchData
import com.example.fizyoapp.ui.bottomnavbar.items.searchscreen.data.repository.PhysiotherapistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PhysiotherapistViewModel:ViewModel() {

    private val physiorepo=PhysiotherapistRepository()

    val physiolist = physiorepo.physioList

    init {
        getAllPhysioData()


    }
    fun getAllPhysioData(){
            physiorepo.getPhysioList()


    }
    fun searchPhysioData(searchText:String){
            physiorepo.searchPhysio(searchText)
    }
}