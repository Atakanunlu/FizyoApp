package com.example.fizyoapp.presentation.bottomnavbar.items.messagesdetailscreen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.fizyoapp.presentation.bottomnavbar.items.messagesscreen.DateFormatter
import kotlinx.coroutines.launch

// Renk tanımlamaları
private val backgroundColor = Color(0xFFF5F5F5)
private val myMessageColor = Color(0xFF2196F3)
private val otherMessageColor = Color(0xFFE0E0E0)
private val accentColor = Color(0xFF2196F3)
private val textFieldColor = Color(0xFFF5F5F5)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesDetailScreen(
    navController: NavController,
    userId: String,
    viewModel: MessagesDetailScreenViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val currentUserId = state.currentUserId
    val context = LocalContext.current

    // Video arama aktifse video ekranını göster
    if (state.isVideoCallActive) {
        val otherUserName = if (state.isPhysiotherapist) {
            "${state.physiotherapist?.firstName ?: ""} ${state.physiotherapist?.lastName ?: ""}"
        } else {
            "${state.user?.firstName ?: ""} ${state.user?.lastName ?: ""}"
        }
        VideoCallScreen(
            otherUserId = userId,
            otherUserName = otherUserName.ifEmpty { "Karşı Taraf" },
            onCallEnded = { viewModel.onEvent(MessageDetailScreenEvent.EndVideoCall) }
        )
        return
    }

    LaunchedEffect(state.messages.size) {
        if(state.messages.isNotEmpty()){
            scrollState.animateScrollToItem(state.messages.size-1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically){
                        Box(modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ){
                            val photoUrl=if(state.isPhysiotherapist){
                                state.physiotherapist?.profilePhotoUrl
                            }
                            else{
                                state.user?.profilePhotoUrl
                            }
                            if(!photoUrl.isNullOrEmpty()){
                                AsyncImage(
                                    model = photoUrl,
                                    contentDescription = "Profil Fotoğrafı",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            else{
                                Icon(imageVector = Icons.Default.Person, contentDescription = "Profil",
                                    tint = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            val name = if (state.isPhysiotherapist) {
                                if (state.physiotherapist != null) {
                                    "FZT. ${state.physiotherapist!!.firstName} ${state.physiotherapist!!.lastName}"
                                }
                                else{
                                    "Fizyoterapist"
                                }
                            }
                            else{
                                if(state.user != null){
                                    "${state.user!!.firstName} ${state.user!!.lastName}"
                                }
                                else{
                                    "Kullanıcı"
                                }
                            }
                            Text(text = name,
                                maxLines = 1,
                                fontWeight = FontWeight.Bold)
                            if(state.isPhysiotherapist && state.physiotherapist != null){
                                Text(
                                    text = "${state.physiotherapist!!.city} / ${state.physiotherapist!!.district}",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.7f)
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
                    IconButton(
                        onClick = { viewModel.onEvent(MessageDetailScreenEvent.StartVideoCall) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Görüntülü Arama",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
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
        ){
            Column(modifier = Modifier.fillMaxSize()) {
                if(state.isLoading && state.messages.isEmpty()){
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ){
                        CircularProgressIndicator()
                    }
                } else if(state.error != null && state.messages.isEmpty()) {
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
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(backgroundColor)
                    ) {
                        // Mesajlar Listesi
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            state = scrollState,
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            items(state.messages) { message ->
                                ModernMessageItem(
                                    message = message,
                                    isFromCurrentUser = message.senderId == currentUserId,
                                    myMessageColor = myMessageColor,
                                    otherMessageColor = otherMessageColor
                                )
                            }
                        }

                        val showScrollToBottom by remember {
                            derivedStateOf {
                                scrollState.firstVisibleItemIndex < state.messages.size - 2 &&
                                        state.messages.size > 5
                            }
                        }
                        if (showScrollToBottom) {
                            FloatingActionButton(
                                onClick = {
                                    coroutineScope.launch {
                                        if (state.messages.isNotEmpty()) {
                                            scrollState.animateScrollToItem(state.messages.size - 1)
                                        }
                                    }
                                },
                                containerColor = accentColor,
                                contentColor = Color.White,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 16.dp, bottom = 16.dp)
                                    .size(46.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "En aşağı kaydır"
                                )
                            }
                        }
                    }
                }

                if (state.error != null && state.messages.isNotEmpty()) {
                    Surface(
                        color = Color(0xFFFEEAEA),
                        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                    ) {
                        Text(
                            text = state.error ?: "Bağlantı hatası",
                            modifier = Modifier.padding(8.dp),
                            color = Color.Red
                        )
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
                            onClick = {viewModel.onEvent(MessageDetailScreenEvent.SendMessage)},
                            modifier = Modifier.size(48.dp),
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                        ) {
                            if(state.isSending){
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                            else{
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Gönder"
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
    otherMessageColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start
    ) {
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
                    tint = Color.Gray,
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