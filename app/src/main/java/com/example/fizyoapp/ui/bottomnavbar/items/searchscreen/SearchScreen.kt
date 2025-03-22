package com.example.fizyoapp.ui.bottomnavbar.items.searchscreen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fizyoapp.ui.bottomnavbar.BottomNavbarComponent
import com.example.fizyoapp.ui.bottomnavbar.items.searchscreen.data.model.SearchData
import com.example.fizyoapp.ui.bottomnavbar.items.searchscreen.data.viewmodel.PhysiotherapistViewModel
import com.google.gson.Gson



@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SearchScreen(navController: NavController ) {


    val context= LocalContext.current
    val viewModel:PhysiotherapistViewModel= viewModel()
    val physiolist by viewModel.physiolist.collectAsState()

    val isSearchState= remember { mutableStateOf(false) }
    val searchText= remember { mutableStateOf("") }


    Scaffold(
        bottomBar = { BottomNavbarComponent(navController) },
        topBar = {
            TopAppBar(
                title = {
                    if(isSearchState.value){
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = searchText.value,
                            onValueChange = {
                                searchText.value= it
                                viewModel.searchPhysioData(it)
                            },
                            textStyle = TextStyle(fontSize = 16.sp),
                            label = { Text(text = "Search") },
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
                        Text(text = "Physiotherapists", fontSize = 20.sp)
                    }
                },
                actions = {
                    if(isSearchState.value){
                        IconButton(
                            onClick = {
                                isSearchState.value=false
                                searchText.value=""
                                viewModel.getAllPhysioData()
                            }
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Search Close Icon")
                        }
                    }
                    else{
                        IconButton(
                            onClick = {isSearchState.value=true}
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Search Icon")
                        }
                    }
                }
            )
        }


    ) {contentPadding ->

        Column(modifier = Modifier.padding(contentPadding)) {
            LazyColumn {
                items(
                    items=physiolist,
                    key={it.ptID}
                ){
                    PhysioListRow(it,navController,context)
                }
            }
        }

    }

}


@Composable
fun PhysioListRow(selected:SearchData,
                  navController: NavController,
                  context: Context
) {
    Card(  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        .fillMaxWidth()
        .height(70.dp)
        .border(0.5.dp, Color.LightGray, RoundedCornerShape(20.dp)),
        shape= RoundedCornerShape(20.dp)
    ){
        Row(
            modifier = Modifier.fillMaxSize()
                .clickable {
                    val physioJson= Gson().toJson(selected)
                    navController.navigate("")
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){

            Column{
                Text(
                    text = "FZT. ${selected.ptName} ${selected.ptSurname}",
                    modifier = Modifier.padding(start = 10.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black
                )
                Text(
                    text = "${selected.ptAddress}",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }

            IconButton(
                modifier = Modifier.padding(10.dp),
                onClick = {
                    //
                }
            ) {
                Icon(Icons.Default.ArrowForward,null)
            }



        }
    }
}



