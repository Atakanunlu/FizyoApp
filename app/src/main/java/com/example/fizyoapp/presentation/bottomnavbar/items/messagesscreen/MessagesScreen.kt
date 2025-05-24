package com.example.fizyoapp.presentation.bottomnavbar.items.messagesscreen
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import java.text.SimpleDateFormat
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fizyoapp.domain.model.messagesscreen.ChatThread
import com.example.fizyoapp.presentation.navigation.AppScreens
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar
import java.util.Date
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    navController: NavController,
    viewModel: MessagesScreenViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val primaryColor = Color(0xFF3B3E68)
    val backgroundColor = Color(0xFFF8F9FC)
    val accentColor = Color(0xFF6D72C3)
    val cardColor = Color.White

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is MessagesScreenUiEvent.NavigateToMessageDetail -> {
                    navController.navigate(AppScreens.MessagesDetailScreen.createMessageDetailRoute(event.userId))
                }
            }
        }
    }

    LaunchedEffect(key1 = navController.currentBackStackEntry) {
        viewModel.onEvent(MessagesScreenEvent.RefreshChatThreads)
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Mesajlar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = {
                        viewModel.onEvent(MessagesScreenEvent.RefreshChatThreads)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Yenile",
                            tint = Color.White
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .background(backgroundColor)
        ) {
            when {
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                state.error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Color.Red.copy(alpha = 0.7f),
                                modifier = Modifier.size(70.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = state.error ?: "Bir hata oluştu",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                color = Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.onEvent(MessagesScreenEvent.RefreshChatThreads) },
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
                state.chatThreads.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = Color.Gray.copy(alpha = 0.7f),
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Henüz bir mesaj konuşmanız bulunmamaktadır",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                color = Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Fizyoterapistlerle iletişime geçerek yeni konuşmalar başlatabilirsiniz",
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                color = Color.Gray
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .padding(bottom = 60.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.chatThreads) { chatThread ->
                            ModernChatThreadItem(
                                chatThread = chatThread,
                                onClick = {
                                    if (chatThread.participantIds.size == 2) {
                                        val currentAuthId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                                        val firstId = chatThread.participantIds[0]
                                        val secondId = chatThread.participantIds[1]
                                        val otherUserId = if (currentAuthId.isNotEmpty()) {
                                            if (firstId == currentAuthId) secondId else firstId
                                        } else if (state.currentUserId.isNotEmpty()) {
                                            if (firstId == state.currentUserId) secondId else firstId
                                        } else {
                                            secondId
                                        }
                                        navController.navigate(AppScreens.MessagesDetailScreen.createMessageDetailRoute(otherUserId))
                                    } else if (chatThread.participantIds.isNotEmpty()) {
                                        navController.navigate(AppScreens.MessagesDetailScreen.createMessageDetailRoute(chatThread.participantIds[0]))
                                    } else {
                                        android.util.Log.e("IDDebug", "Geçerli katılımcı ID'si bulunamadı!")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernChatThreadItem(
    chatThread: ChatThread,
    onClick: () -> Unit
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val accentColor = Color(0xFF6D72C3)

    val iLastSentMessage = chatThread.lastMessageSenderId == currentUserId


    val hasUnreadMessages = chatThread.unreadCount > 0 && !iLastSentMessage


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)

            .then(if (hasUnreadMessages) Modifier.border(
                width = 2.dp,
                color = accentColor,
                shape = RoundedCornerShape(16.dp)
            ) else Modifier),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(

            containerColor = if (hasUnreadMessages) Color(0xFFF0F2FF) else Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .shadow(4.dp, CircleShape)
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
                if (chatThread.otherParticipantPhotoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = chatThread.otherParticipantPhotoUrl,
                        contentDescription = "Profil fotoğrafı",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profil",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }


                if (hasUnreadMessages) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 6.dp, y = (-6).dp)
                            .clip(CircleShape)
                            .background(Color.Red),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (chatThread.unreadCount > 9) "9+" else chatThread.unreadCount.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chatThread.otherParticipantName,
                        fontSize = 17.sp,

                        fontWeight = if (hasUnreadMessages) FontWeight.Bold else FontWeight.SemiBold,
                        color = Color(0xFF3B3E68),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFFF0F0F6),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = DateFormatter.formatDate(chatThread.lastMessageTimestamp),
                            fontSize = 12.sp,

                            color = if (hasUnreadMessages) Color.DarkGray else Color.Gray,
                            fontWeight = if (hasUnreadMessages) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chatThread.lastMessage,
                        fontSize = 14.sp,

                        color = if (hasUnreadMessages) Color.Black else Color.DarkGray,
                        fontWeight = if (hasUnreadMessages) FontWeight.Medium else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (hasUnreadMessages) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(accentColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = chatThread.unreadCount.toString(),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
object DateFormatter {
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("tr"))
    private val shortDateFormat = SimpleDateFormat("dd MMM", Locale("tr"))

    fun formatMessageTime(date: Date): String {
        return timeFormat.format(date)
    }

    fun formatDate(date: Date): String {
        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()
        calendar.time = date


        if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            return timeFormat.format(date)
        }


        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        if (calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)) {
            return "Dün"
        }


        if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
            return shortDateFormat.format(date)
        }


        return dateFormat.format(date)
    }

    fun formatMessageDate(date: Date): String {
        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()
        calendar.time = date


        if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            return "Bugün"
        }


        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        if (calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)) {
            return "Dün"
        }


        val currentWeek = Calendar.getInstance()
        currentWeek.add(Calendar.DAY_OF_YEAR, -7)
        if (date.after(currentWeek.time)) {
            val dayFormat = SimpleDateFormat("EEEE", Locale("tr"))
            return dayFormat.format(date).capitalize(Locale("tr"))
        }


        return dateFormat.format(date)
    }


    fun getMessageDay(date: Date): String {
        return formatMessageDate(date)
    }
}