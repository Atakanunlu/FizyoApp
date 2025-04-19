package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_main_screen

sealed class PhysiotherapistEvent {
    data object SignOut: PhysiotherapistEvent()
}