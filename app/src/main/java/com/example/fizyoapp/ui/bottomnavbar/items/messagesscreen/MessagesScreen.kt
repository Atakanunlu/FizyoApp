package com.example.fizyoapp.ui.bottomnavbar.items.messagesscreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fizyoapp.ui.bottomnavbar.items.messagesscreen.data.messagesviewmodel.MessagesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(navController: NavController) {

    val viewmodel:MessagesViewModel= viewModel()
    val messagesList by viewmodel.messagesList.collectAsState()
    val context= LocalContext.current
    val isSearchState= remember { mutableStateOf(false) }
    val searchText= remember { mutableStateOf("") }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if(isSearchState.value){
                        TextField(modifier = Modifier.fillMaxWidth(),
                            value = searchText.value,
                            onValueChange = {
                                searchText.value=it
                                viewmodel.searchMessagesData(it)
                            },
                            label = {
                                Text(text = "Search")
                            },
                            textStyle = TextStyle(fontSize = 16.sp),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedIndicatorColor = Color.LightGray,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent

                            ),
                            maxLines = 1,
                            singleLine = true
                        )
                    }
                    else{
                        Text(text = "Mesajlar", fontSize = 20.sp)
                    }
                },
                actions = {
                    if(isSearchState.value){
                        IconButton(
                            onClick = {
                                isSearchState.value=false
                                searchText.value=""
                                viewmodel.gettAllData()
                            }
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                    else{
                        IconButton(
                            onClick = {
                                isSearchState.value=true
                            }
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null)
                        }
                    }
                }
            )


        }

    ) {paddingValues ->

        Column(modifier = Modifier.padding(paddingValues)) {
            LazyColumn {
                items(
                    items=messagesList
                ){
                  MessagesListRow()
                }
            }

        }


    }

}

@Composable
fun MessagesListRow() {



}
