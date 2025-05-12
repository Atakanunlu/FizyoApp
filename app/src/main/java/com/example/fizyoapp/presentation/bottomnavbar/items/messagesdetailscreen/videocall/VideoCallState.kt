package com.example.fizyoapp.presentation.bottomnavbar.items.messagesdetailscreen.videocall


data class VideoCallState(
    val isInitialized: Boolean = false,
    val isJoining: Boolean = false,
    val isConnected: Boolean = false,
    val localUid: Int = 0,
    val remoteUid: Int = 0,
    val isRemoteUserJoined: Boolean = false,
    val isMuted: Boolean = false,
    val isCameraOn: Boolean = true,
    val error: String? = null
)