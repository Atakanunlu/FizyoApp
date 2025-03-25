package com.example.fizyoapp.presentation.bottomnavbar.items.searchscreen

import androidx.lifecycle.ViewModel
import com.example.fizyoapp.data.repository.physiotherapist.PhysiotherapistRepository

class PhysiotherapistViewModel:ViewModel() {

    private val physiorepo= PhysiotherapistRepository()

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