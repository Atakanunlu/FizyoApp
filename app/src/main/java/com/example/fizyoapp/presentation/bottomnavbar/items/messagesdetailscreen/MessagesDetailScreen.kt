package com.example.fizyoapp.presentation.bottomnavbar.items.messagesdetailscreen

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fizyoapp.domain.model.messagesscreen.Message
import com.example.fizyoapp.presentation.bottomnavbar.items.messagesdetailscreen.videocall.VideoCallScreen
import com.example.fizyoapp.presentation.bottomnavbar.items.messagesscreen.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.internal.concurrent.formatDuration
import java.text.SimpleDateFormat
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesDetailScreen(
    navController: NavController,
    userId: String,
    viewModel: MessagesDetailScreenViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val currentUserId = state.currentUserId
    val context = LocalContext.current
    val primaryColor = Color(0xFF3B3E68)
    val backgroundColor = Color(0xFFF8F9FC)
    val accentColor = Color(0xFF6D72C3)
    val myMessageColor = Color(0xFF6D72C3)
    val otherMessageColor = Color(0xFFF0F0F6)
    val textFieldColor = Color.White

    var selectedRadiologicalMessage by remember { mutableStateOf<Message?>(null) }
    var selectedMedicalReportMessage by remember { mutableStateOf<Message?>(null) }
    var selectedEvaluationFormMessage by remember { mutableStateOf<Message?>(null) }

    // Mesajları günlere göre grupla
    val groupedMessages = remember(state.messages) {
        state.messages.groupBy { message ->
            DateFormatter.getMessageDay(message.timestamp)
        }
    }


    if (state.isVideoCallActive) {
        val otherUserName = if (state.isPhysiotherapist) {
            "${state.physiotherapist?.firstName ?: ""} ${state.physiotherapist?.lastName ?: ""}"
        } else {
            "${state.user?.firstName ?: ""} ${state.user?.lastName ?: ""}"
        }
        VideoCallScreen(
            otherUserId = userId,
            otherUserName = otherUserName.ifEmpty { "Karşı Taraf" },
            onCallEnded = { wasAnswered, metadata ->
                viewModel.onEvent(MessageDetailScreenEvent.EndVideoCall(wasAnswered, metadata))
            }
        )
        return
    }

    LaunchedEffect(state.messages, state.isLoading) {
        if (state.messages.isNotEmpty() && !state.isLoading) {
            // Mesajlar yüklendikten hemen sonra en alta kaydır
            scrollState.scrollToItem(state.messages.size - 1, scrollOffset = 0)
        }
    }


    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF3B3E68),
                                            Color(0xFF6D72C3)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            val photoUrl = if (state.isPhysiotherapist) {
                                state.physiotherapist?.profilePhotoUrl
                            } else {
                                state.user?.profilePhotoUrl
                            }
                            if (!photoUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = photoUrl,
                                    contentDescription = "Profil Fotoğrafı",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profil",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            val name = if (state.isPhysiotherapist) {
                                if (state.physiotherapist != null) {
                                    "FZT. ${state.physiotherapist!!.firstName ?: ""} ${state.physiotherapist!!.lastName ?: ""}"
                                } else {
                                    "Fizyoterapist"
                                }
                            } else {
                                if (state.user != null) {
                                    "${state.user!!.firstName ?: ""} ${state.user!!.lastName ?: ""}"
                                } else {
                                    "Kullanıcı"
                                }
                            }
                            Text(
                                text = name,
                                maxLines = 1,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            if (state.isPhysiotherapist && state.physiotherapist != null) {
                                Text(
                                    text = "${state.physiotherapist!!.city} / ${state.physiotherapist!!.district}",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Sadece fizyoterapistler için görüntülü arama butonu göster
                    if (!state.isPhysiotherapist) {
                        IconButton(
                            onClick = { viewModel.onEvent(MessageDetailScreenEvent.StartVideoCall) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Görüntülü Arama",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading && state.messages.isEmpty() -> {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = accentColor,
                                    strokeWidth = 4.dp,
                                    modifier = Modifier.size(50.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Mesajlar yükleniyor...",
                                    fontSize = 16.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                    state.error != null && state.messages.isEmpty() -> {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = Color.Red.copy(alpha = 0.7f),
                                    modifier = Modifier.size(70.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = state.error ?: "Bir hata oluştu",
                                    color = Color.DarkGray,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = { viewModel.onEvent(MessageDetailScreenEvent.RefreshMessages) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = accentColor
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Tekrar Dene")
                                }
                            }
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(backgroundColor)
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                state = scrollState,
                                contentPadding = PaddingValues(vertical = 16.dp)
                            ) {
                                groupedMessages.forEach { (date, messagesForDate) ->
                                    // Tarih başlığı
                                    item {
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Surface(
                                                color = Color(0xFFE0E0E0),
                                                shape = RoundedCornerShape(16.dp),
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            ) {
                                                Text(
                                                    text = date,
                                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color.DarkGray
                                                )
                                            }
                                        }
                                    }

                                    // Günün mesajları
                                    items(messagesForDate) { message ->
                                        ModernMessageItem(
                                            message = message,
                                            isFromCurrentUser = message.senderId == currentUserId,
                                            myMessageColor = myMessageColor,
                                            otherMessageColor = otherMessageColor,
                                            onMessageClick = { clickedMessage ->
                                                when {
                                                    isRadiologicalImageMessage(clickedMessage) -> {
                                                        selectedRadiologicalMessage = clickedMessage
                                                    }
                                                    isMedicalReportMessage(clickedMessage) -> {
                                                        selectedMedicalReportMessage = clickedMessage
                                                    }
                                                    isEvaluationFormMessage(clickedMessage) -> {
                                                        selectedEvaluationFormMessage = clickedMessage
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            selectedRadiologicalMessage?.let { message ->
                                RadiologicalImageDetailDialog(
                                    message = message,
                                    onDismiss = { selectedRadiologicalMessage = null }
                                )
                            }

                            selectedMedicalReportMessage?.let { message ->
                                MedicalReportDetailDialog(
                                    message = message,
                                    onDismiss = { selectedMedicalReportMessage = null }
                                )
                            }

                            selectedEvaluationFormMessage?.let { message ->
                                EvaluationFormDetailDialog(
                                    message = message,
                                    onDismiss = { selectedEvaluationFormMessage = null }
                                )
                            }

                            val showScrollToBottom by remember {
                                derivedStateOf {
                                    scrollState.firstVisibleItemIndex < state.messages.size - 2 &&
                                            state.messages.size > 5
                                }
                            }

                        }
                    }
                }

                if (state.error != null && state.messages.isNotEmpty()) {
                    Surface(
                        color = Color(0xFFFEEAEA),
                        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = state.error ?: "Bir hata oluştu",
                                color = Color.Red.copy(alpha = 0.8f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = textFieldColor
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = state.messageText,
                                onValueChange = { viewModel.onEvent(MessageDetailScreenEvent.MessageTextChanged(it)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                placeholder = {
                                    Text(
                                        "Mesajınızı yazın...",
                                        color = Color.Gray
                                    )
                                },
                                maxLines = 4,
                                shape = RoundedCornerShape(24.dp),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    cursorColor = accentColor,
                                    containerColor = textFieldColor
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        FloatingActionButton(
                            onClick = { viewModel.onEvent(MessageDetailScreenEvent.SendMessage) },
                            modifier = Modifier.size(52.dp),
                            containerColor = accentColor,
                            contentColor = Color.White,
                            shape = CircleShape
                        ) {
                            if (state.isSending) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Gönder",
                                    modifier = Modifier.padding(start = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernMessageItem(
    message: Message,
    isFromCurrentUser: Boolean,
    myMessageColor: Color,
    otherMessageColor: Color,
    onMessageClick: (Message) -> Unit = {}
) {
    // Bu fonksiyonu ekleyin:
    fun shouldShowUnreadIndicator(message: Message, isFromCurrentUser: Boolean): Boolean {
        // Eğer mesaj benden geldiyse, okunma göstergesini hiç gösterme
        if (isFromCurrentUser) {
            return false
        }
        // Başkasından gelen mesajlar için okunma durumunu göster
        return !message.isRead
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start
    ) {
        when {
            message.messageType == "video_call" || message.messageType == "missed_video_call" -> {
                VideoCallNotificationBubble(
                    message = message,
                    isCurrentUser = isFromCurrentUser
                )
            }
            isRadiologicalImageMessage(message) -> {
                RadiologicalImageMessageBubble(
                    message = message,
                    isCurrentUser = isFromCurrentUser,
                    onClick = { onMessageClick(message) }
                )
            }
            isMedicalReportMessage(message) -> {
                MedicalReportMessageBubble(
                    message = message,
                    isCurrentUser = isFromCurrentUser,
                    onClick = { onMessageClick(message) }
                )
            }
            isEvaluationFormMessage(message) -> {
                EvaluationFormMessageBubble(
                    message = message,
                    isCurrentUser = isFromCurrentUser,
                    onClick = { onMessageClick(message) }
                )
            }
            else -> {
                // Normal metin mesajı
                Box(
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .clip(
                            RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isFromCurrentUser) 16.dp else 4.dp,
                                bottomEnd = if (isFromCurrentUser) 4.dp else 16.dp
                            )
                        )
                        .background(
                            if (isFromCurrentUser) myMessageColor else otherMessageColor
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = message.content,
                        color = if (isFromCurrentUser) Color.White else Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    if (isFromCurrentUser) {
                        Text(
                            text = DateFormatter.formatMessageTime(message.timestamp),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = null,
                            tint = if (message.isRead) Color(0xFF6D72C3) else Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                    } else {
                        Text(
                            text = DateFormatter.formatMessageTime(message.timestamp),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VideoCallNotificationBubble(
    message: Message,
    isCurrentUser: Boolean
) {
    val isMissedCall = message.messageType == "missed_video_call"

    // Animasyon için
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Üst Kısım: İkon ve Başlık
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(
                                if (isMissedCall)
                                    Color(0xFFFF3B30)
                                else
                                    Color(0xFF34C759)
                            )
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isMissedCall)
                                Icons.Default.VideocamOff
                            else
                                Icons.Default.Videocam,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isMissedCall)
                            "Cevapsız Görüntülü Arama"
                        else
                            "Görüntülü Arama",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isMissedCall) Color(0xFFFF3B30) else Color(0xFF34C759)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Orta Kısım: Süre (eğer tamamlanmış arama ise)
                    if (!isMissedCall && message.metadata.containsKey("duration")) {
                        val duration = message.metadata["duration"] as? Long ?: 0L
                        if (duration > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Arama Süresi: ${formatDuration(duration)}",
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }

                    // Alt Kısım: Tarih ve Zaman
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        val dateText = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("tr"))
                            .format(message.timestamp)
                        Text(
                            text = dateText,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    // Cevapsız arama için ek bilgi
                    if (isMissedCall) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Divider(
                            modifier = Modifier.padding(horizontal = 32.dp),
                            color = Color.LightGray
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFFF3B30).copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isCurrentUser)
                                    "Aramanız cevaplanmadı"
                                else
                                    "Cevapsız bir arama aldınız",
                                fontSize = 13.sp,
                                color = Color.DarkGray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}