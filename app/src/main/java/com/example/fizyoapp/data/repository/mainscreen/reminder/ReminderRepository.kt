package com.example.fizyoapp.data.repository.mainscreen.reminder

import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.usermainscreen.Reminder
import kotlinx.coroutines.flow.Flow


interface ReminderRepository {
    suspend fun addReminder(reminder: Reminder): Resource<Unit>
    suspend fun updateReminder(reminder: Reminder): Resource<Unit>
    suspend fun deleteReminder(id: String): Resource<Unit>
    fun getActiveReminders(userId: String): Flow<Resource<List<Reminder>>>
}