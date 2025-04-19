package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_main_screen

import com.example.fizyoapp.domain.model.auth.User

data class PhysiotherapistState
  (val user: User? = null,
   val isLoading: Boolean = false,
   val errorMessage: String? = null
)
