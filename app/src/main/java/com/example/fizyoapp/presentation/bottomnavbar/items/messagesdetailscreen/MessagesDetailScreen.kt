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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fizyoapp.data.repository.auth.AuthRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.messagesscreen.Message
import com.example.fizyoapp.presentation.bottomnavbar.items.messagesscreen.DateFormatter
import kotlinx.coroutines.flow.first

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesDetailScreen(
    navController: NavController,
    userId:String,
    viewModel: MessagesDetailScreenViewModel= hiltViewModel(),

){
    val state by viewModel.state.collectAsState()
    val scrollState =  rememberLazyListState()

    var currentUserId  =state.currentUserId


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
                        Column {
                            val name = if(state.isPhysiotherapist){
                                if(state.physiotherapist != null){
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
                    IconButton(onClick = {navController.popBackStack()}) {
                        Icon(imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )

            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier.fillMaxSize()
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
                }
                else if(state.error != null && state.messages.isEmpty()){
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ){
                        Column (horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.error!!,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.onEvent(MessageDetailScreenEvent.RefreshMessages) }) {
                                Text("Tekrar Dene")
                            }
                        }
                    }
                }
                else{
                    //Mesajlar burada

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        state = scrollState,
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(state.messages){ message ->
                            MessageItem(
                                message =message,
                                isFromCurrentUser = message.senderId == currentUserId
                            )
                        }
                    }
                }

                if(state.error != null && state.messages.isNotEmpty()){
                    Text(text = state.error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp))
                }
//mesaj giriş alanı
                Surface( modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface) {

                    Row (modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically){


                        OutlinedTextField(
                            value = state.messageText,
                            onValueChange = {viewModel.onEvent(MessageDetailScreenEvent.MessageTextChanged(it))},
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Mesajınızı yazın...") },
                            maxLines = 4,
                            shape = RoundedCornerShape(24.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

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
fun MessageItem(message: Message, isFromCurrentUser: Boolean) {
    Column( modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp),
        horizontalAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start) {


        Box( modifier = Modifier
            .widthIn(max = 280.dp)
            .clip(
                RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isFromCurrentUser) 16.dp else 4.dp,
                    bottomEnd = if (isFromCurrentUser) 4.dp else 16.dp
                )
            ).background(
                if (isFromCurrentUser) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            ).padding(12.dp)
        ){

            Text(text=message.content,
            color=if (isFromCurrentUser) Color.White else Color.Black)
        }
        Spacer(modifier = Modifier.height(2.dp))

        Text(text = DateFormatter.formatMessageTime(message.timestamp),
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 4.dp))

    }
}
