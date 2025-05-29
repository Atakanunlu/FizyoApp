package com.example.fizyoapp.data.util

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object EventBus {
    // Yeni olayların hemen işlenmesini sağlamak için extraBufferCapacity ve onBufferOverflow ayarları
    private val _events = MutableSharedFlow<AppEvent>(
        replay = 0,
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events = _events.asSharedFlow()

    // Olayları yayınlamak için suspend fonksiyon
    suspend fun emitEvent(event: AppEvent) {
        _events.emit(event)
    }

    // Suspend olmayan bir emit fonksiyonu da ekleyelim
    fun tryEmitEvent(event: AppEvent) {
        _events.tryEmit(event)
    }
}

sealed class AppEvent {
    data class AppointmentCreated(val appointmentId: String, val timestamp: Long = System.currentTimeMillis()) : AppEvent()
    data object RefreshAppointments : AppEvent()
    data class ForceRefreshAppointments(val source: String = "unknown") : AppEvent()
}