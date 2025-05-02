package com.example.fizyoapp.domain.usecase.mainscreen

import com.example.fizyoapp.data.repository.mainscreen.reminder.ReminderRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.usermainscreen.Reminder
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveRemindersUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository
) {
    operator fun invoke(userId: String): Flow<Resource<List<Reminder>>> {
        return reminderRepository.getActiveReminders(userId)
    }
}

class AddReminderUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository
) {
    suspend operator fun invoke(reminder: Reminder): Resource<Unit> {
        return reminderRepository.addReminder(reminder)
    }
}