package com.example.fizyoapp.presentation.user.usermainscreen

sealed class UserEvent {
    data class UpdateWaterIntake(val glasses: Int) : UserEvent()
    data class UpdateStepCount(val steps: Int) : UserEvent()
    object Logout : UserEvent()
    object DismissError : UserEvent()
}