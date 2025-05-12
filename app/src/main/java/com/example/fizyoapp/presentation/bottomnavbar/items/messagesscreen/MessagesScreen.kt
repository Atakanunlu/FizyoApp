package com.example.fizyoapp.presentation.bottomnavbar.items.messagesscreen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import java.text.SimpleDateFormat
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fizyoapp.domain.model.messagesscreen.ChatThread
import com.example.fizyoapp.presentation.navigation.AppScreens
import com.example.fizyoapp.ui.bottomnavbar.BottomNavbarComponent

import java.util.*

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    navController: NavController,
    viewModel: MessagesScreenViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Hata mesajlarını otomatik temizle (özellikle oturum hatalarını)
    LaunchedEffect(state.error) {
        if (state.error?.lowercase()?.contains("oturum") == true ||
            state.error?.lowercase()?.contains("auth") == true) {
            viewModel.onEvent(MessagesScreenEvent.DismissError)
        }
    }

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
        // Ekrana her girişte mesajları yenile
        viewModel.onEvent(MessagesScreenEvent.RefreshChatThreads)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mesajlar") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = { BottomNavbarComponent(navController) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            // İlk yükleme durumunda sadece yükleme göstergesini göster
            if (state.isInitialLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (state.error != null) {
                // Oturum hatalarını gösterme
                if (!state.error!!.lowercase().contains("oturum") &&
                    !state.error!!.lowercase().contains("auth")) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.error ?: "Bir hata oluştu",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.onEvent(MessagesScreenEvent.RefreshChatThreads) }) {
                            Text("Tekrar Dene")
                        }
                    }
                }
            } else if (state.chatThreads.isEmpty()) {
                Text(
                    text = "Henüz bir mesaj konuşmanız bulunmamaktadır.",
                    color = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .padding(bottom = 60.dp)
                ) {
                    items(state.chatThreads) { chatThread ->
                        ChatThreadItem(
                            chatThread = chatThread,
                            onClick = {
                                // participantIds listesinin boyutunu kontrol et
                                if (chatThread.participantIds.size == 2) {
                                    // Sohbette iki kişi varsa (normal durum)
                                    val firstId = chatThread.participantIds[0]
                                    val secondId = chatThread.participantIds[1]
                                    // Mevcut kullanıcı ID'sini kontrol et
                                    val otherUserId = if (state.currentUserId.isNotEmpty()) {
                                        // Eğer mevcut kullanıcı ID'si biliniyorsa, diğer ID'yi bul
                                        if (firstId == state.currentUserId) secondId else firstId
                                    } else {
                                        // Mevcut kullanıcı ID'si bilinmiyorsa, ikinci ID'yi kullan
                                        // Bu bir tahmin ama birçok durumda çalışabilir
                                        secondId
                                    }
                                    navController.navigate(AppScreens.MessagesDetailScreen.createMessageDetailRoute(otherUserId))
                                } else if (chatThread.participantIds.isNotEmpty()) {
                                    // Listede sadece bir ID varsa onu kullan
                                    navController.navigate(AppScreens.MessagesDetailScreen.createMessageDetailRoute(chatThread.participantIds[0]))
                                } else {
                                    // Hiçbir ID bulunamadı - hata durumu
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

@Composable
fun ChatThreadItem(
    chatThread: ChatThread,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),

    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profil Resmi
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
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
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Mesaj içeriği
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
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = DateFormatter.formatDate(chatThread.lastMessageTimestamp),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chatThread.lastMessage,
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (chatThread.unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
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
    fun formatDate(date: Date): String {
        val now = Calendar.getInstance()
        val messageTime = Calendar.getInstance().apply { time = date }
        return when {
            now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) == messageTime.get(Calendar.DAY_OF_YEAR) -> {
                // Bugün, sadece saati göster
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
            }
            now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) == messageTime.get(Calendar.DAY_OF_YEAR) + 1 -> {
                // Dün
                "Dün"
            }
            else -> {
                // Diğer günler, tarihi göster
                SimpleDateFormat("dd MMM", Locale.getDefault()).format(date)
            }
        }
    }

    fun formatMessageTime(date: Date): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
    }
}