package com.example.fizyoapp.presentation.user.usermainscreen

import com.example.fizyoapp.domain.model.user_profile.UserProfile
import com.example.fizyoapp.domain.model.usermainscreen.PainRecord
import com.example.fizyoapp.domain.model.usermainscreen.Reminder
import com.example.fizyoapp.domain.model.usermainscreen.StepCount
import com.example.fizyoapp.domain.model.usermainscreen.WaterIntake


data class UserState(
    val userName: String? = null,
    val userProfile: UserProfile? = null,
    val latestPainRecord: PainRecord? = null,
    val waterIntake: WaterIntake = WaterIntake(),
    val stepCount: StepCount = StepCount(),
    val reminders: List<Reminder> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)