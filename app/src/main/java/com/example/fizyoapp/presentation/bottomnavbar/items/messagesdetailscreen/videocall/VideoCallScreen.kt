package com.example.fizyoapp.presentation.bottomnavbar.items.messagesdetailscreen.videocall

import android.Manifest
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.fizyoapp.domain.model.messagesscreen.Message
import com.example.fizyoapp.presentation.bottomnavbar.items.messagesscreen.DateFormatter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VideoCallScreen(
    otherUserId: String,
    otherUserName: String,
    onCallEnded: (Boolean, Map<String, Any>) -> Unit
) {
    // Aramanın sonlandırıldığını takip etmek için
    var isCallEndingHandled by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    )
    val callChannel = remember { "call_${UUID.randomUUID().toString().take(8)}" }
    val agoraCall = remember {
        AgoraVideoCall(
            context = context,
            channelId = callChannel,
            uid = 0
        )
    }

    val callState by agoraCall.callState.collectAsState()
    var localVideoView by remember { mutableStateOf<FrameLayout?>(null) }
    var remoteVideoView by remember { mutableStateOf<FrameLayout?>(null) }
    var showControls by remember { mutableStateOf(true) }
    var callAnswered by remember { mutableStateOf(false) }
    var callDuration by remember { mutableStateOf(0L) }
    var callStartTime by remember { mutableStateOf(0L) }

    // Arama başladığında zamanı kaydet
    LaunchedEffect(Unit) {
        callStartTime = System.currentTimeMillis()
    }

    // İzinler verildiğinde aramayı başlat
    LaunchedEffect(key1 = permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted) {
            agoraCall.initialize()
            agoraCall.joinChannel()
        }
    }

    // Uzak kullanıcı katıldığında aramanın cevaplanmış olduğunu işaretle
    LaunchedEffect(callState.isRemoteUserJoined) {
        if (callState.isRemoteUserJoined) {
            callAnswered = true
        }
    }

    // Arama süresi hesaplayıcı
    LaunchedEffect(callAnswered) {
        if (callAnswered) {
            while (true) {
                delay(1000) // Her saniye güncelle
                callDuration = (System.currentTimeMillis() - callStartTime) / 1000
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY && !isCallEndingHandled) {
                isCallEndingHandled = true
                agoraCall.release()
                // Ekrana çıkışta uyarı gönderilmesini önlemek için boş işlev
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            agoraCall.release()

            // Buradaki koşul çok önemli - yalnızca kullanıcı butona basarak çıktıysa mesaj gönderilsin
            if (!isCallEndingHandled) {
                isCallEndingHandled = true
                val metadata = if (callAnswered) {
                    mapOf("duration" to callDuration)
                } else {
                    emptyMap<String, Any>()
                }
                onCallEnded(callAnswered, metadata)
            }
        }
    }


    var callEndedByUser by remember { mutableStateOf(false) }

    // Arama sonlandırma fonksiyonu
    val endCall = {
        if (!isCallEndingHandled) {
            isCallEndingHandled = true
            val metadata = if (callAnswered) {
                mapOf("duration" to callDuration)
            } else {
                emptyMap<String, Any>()
            }
            agoraCall.leaveChannel()
            onCallEnded(callAnswered, metadata)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (!permissionsState.allPermissionsGranted) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Görüntülü arama için kamera ve mikrofon izinleri gereklidir",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { permissionsState.launchMultiplePermissionRequest() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6D72C3)
                    )
                ) {
                    Text("İzinleri Ver")
                }
            }
        } else {
            AndroidView(
                factory = { ctx ->
                    FrameLayout(ctx).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )
                        remoteVideoView = this
                        agoraCall.setRemoteVideoContainer(this)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(120.dp, 160.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                AndroidView(
                    factory = { ctx ->
                        FrameLayout(ctx).apply {
                            layoutParams = FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT
                            )
                            localVideoView = this
                            agoraCall.setLocalVideoContainer(this)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (callAnswered) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = formatDuration(callDuration),
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }

            if (showControls) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FloatingActionButton(
                            onClick = { agoraCall.toggleMute() },
                            containerColor = if (callState.isMuted) Color.Red else Color.Gray,
                            contentColor = Color.White,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                imageVector = if (callState.isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                                contentDescription = "Mikrofon"
                            )
                        }

                        // Aramayı sonlandırma butonu
                        FloatingActionButton(
                            onClick = endCall,
                            containerColor = Color.Red,
                            contentColor = Color.White,
                            modifier = Modifier.size(70.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CallEnd,
                                contentDescription = "Aramayı Sonlandır",
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        FloatingActionButton(
                            onClick = { agoraCall.toggleCamera() },
                            containerColor = if (!callState.isCameraOn) Color.Red else Color.Gray,
                            contentColor = Color.White,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                imageVector = if (callState.isCameraOn) Icons.Filled.Videocam else Icons.Filled.VideocamOff,
                                contentDescription = "Kamera"
                            )
                        }

                        FloatingActionButton(
                            onClick = { agoraCall.switchCamera() },
                            containerColor = Color.Gray,
                            contentColor = Color.White,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FlipCameraAndroid,
                                contentDescription = "Kamera Değiştir"
                            )
                        }
                    }
                }
            }

            if (!callState.isRemoteUserJoined && callState.isConnected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$otherUserName aranıyor...",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        CircularProgressIndicator(
                            color = Color(0xFF6D72C3),
                            modifier = Modifier.size(60.dp),
                            strokeWidth = 5.dp
                        )
                    }
                }
            }

            callState.error?.let { error ->
                AlertDialog(
                    onDismissRequest = { endCall() },
                    title = { Text("Arama Hatası") },
                    text = { Text(error) },
                    confirmButton = {
                        Button(onClick = { endCall() }) {
                            Text("Tamam")
                        }
                    }
                )
            }
        }
    }
}





private fun formatDuration(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return if (minutes > 0) {
        "$minutes dakika $remainingSeconds saniye"
    } else {
        "$remainingSeconds saniye"
    }
}

