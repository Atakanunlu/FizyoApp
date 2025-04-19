package com.example.fizyoapp.data.model.search

import java.util.UUID

data class SearchData(
    var ptID: String= UUID.randomUUID().toString(),
    var ptName: String? =  null,
    var ptSurname: String? = null,
    var ptCommunication: String?= null,
    var ptAddress:String?=null,
    var ptPhoto:String?=null,

)
