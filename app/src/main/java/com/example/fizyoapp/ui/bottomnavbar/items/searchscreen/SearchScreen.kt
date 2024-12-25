package com.example.fizyoapp.ui.bottomnavbar.items.searchscreen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fizyoapp.ui.bottomnavbar.BottomNavbarComponent
import com.example.fizyoapp.ui.bottomnavbar.items.searchscreen.data.CircleImage
import com.example.fizyoapp.ui.bottomnavbar.items.searchscreen.data.Fizyoterapistler
import com.example.fizyoapp.ui.bottomnavbar.items.searchscreen.data.FizyoterapistlerEntity
import com.example.fizyoapp.ui.bottomnavbar.items.searchscreen.data.getFizyo
import com.example.fizyoapp.ui.bottomnavbar.items.searchscreen.data.viewmodel.FizyoterapistViewModel


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SearchScreen(navController: NavController ){

    //val searchquery by viewModel.searchQuery.collectAsState()
    //val filtered by viewModel.filteredList.collectAsState()


    var selectedItemIndex by rememberSaveable {
        mutableStateOf(0)
    }
    var query by remember { mutableStateOf("") }
    //var filtered by remember { mutableStateOf(items) }

    var active by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            Column{
                TextField(
                    value=query,
                    onValueChange = {it ->
                        query = it

                    },
                    label={
                        Text(text="Fizyoterapist Ara")

                    },
                    trailingIcon = {
                        Icon( imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon")
                    },


                    modifier = Modifier.fillMaxWidth().padding(vertical = 15.dp, horizontal = 10.dp),
                    shape = RoundedCornerShape(30.dp)

                )
            }

        },

        bottomBar = { BottomNavbarComponent(navController) }
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(59, 62, 104))
                .padding(it)
        ){
            LazyColumn {

                items(getFizyo()){ fizyoterapistlerItem ->
                    FizyoRow(fizyoterapistlerItem =fizyoterapistlerItem)
                }
            }
        }



    }
}




@Composable
fun FizyoRow(
    fizyoterapistlerItem: FizyoterapistlerEntity,
    onItemClick:(String)->Unit = {})
{

    Card(
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            .fillMaxWidth()
            .clickable {
                onItemClick(fizyoterapistlerItem.id.toString())
            },
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color.DarkGray
        ),
        shape = RoundedCornerShape(corner = CornerSize(20.dp)),
        elevation = CardDefaults.cardElevation(5.dp)
    ){

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ){
            RowPosterImage(fizyoterapistlerItem = fizyoterapistlerItem)

            Column(modifier = Modifier.padding(10.dp)) {

                RowMainData(fizyoterapistlerItem=fizyoterapistlerItem)
            }

        }

    }
}
@Composable
fun RowPosterImage(
    fizyoterapistlerItem: FizyoterapistlerEntity
){
    Surface(modifier = Modifier.padding(10.dp)
        .size(100.dp),
        shape=CircleShape,
        shadowElevation = 10.dp) {
        CircleImage(imageUrl = fizyoterapistlerItem.fizyoImages)
    }
}

@Composable
fun RowMainData(fizyoterapistlerItem: FizyoterapistlerEntity){
    Text(text=fizyoterapistlerItem.unvan,
        style=MaterialTheme.typography.titleMedium)
    Text(
        text = fizyoterapistlerItem.name,
        style = MaterialTheme.typography.labelMedium
    )
    Text(
        text = fizyoterapistlerItem.surname,
        style = MaterialTheme.typography.labelMedium
    )

}

@Preview
@Composable
fun PrevSearch(){
    SearchScreen(navController = rememberNavController())
}



