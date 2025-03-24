package com.example.fizyoapp.presentation.physiotherapist

import com.example.fizyoapp.domain.model.User

data class PhysiotherapistState
  (  val user: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
