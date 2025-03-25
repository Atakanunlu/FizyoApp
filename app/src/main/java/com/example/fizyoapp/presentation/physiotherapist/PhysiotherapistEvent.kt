package com.example.fizyoapp.presentation.physiotherapist

sealed class PhysiotherapistEvent {
    data object SignOut: PhysiotherapistEvent()
}