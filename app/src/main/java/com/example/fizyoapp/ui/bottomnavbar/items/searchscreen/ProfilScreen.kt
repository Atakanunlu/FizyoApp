package com.example.fizyoapp.ui.bottomnavbar.items.searchscreen
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FollowTheSigns
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fizyoapp.ui.bottomnavbar.BottomNavbarComponent

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilScreen(navController: NavController){


    Scaffold (
        topBar ={ TopAppBar(title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment=Alignment.Center
            ) {
                Text(text = "Profil")
            }
        },
            modifier = Modifier.padding(10.dp).clip(RoundedCornerShape(20.dp)),
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White,
                titleContentColor = Color.DarkGray
            ))
        },
        bottomBar = { BottomNavbarComponent(navController) }
    ){
        Column(modifier = Modifier.fillMaxSize().background(Color(59, 62, 104)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Row{
                Button(onClick = {},
                    shape = CutCornerShape(8.dp),
                    modifier = Modifier.size(150.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.DarkGray
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 10.dp,
                        pressedElevation = 6.dp
                    )
                ){
                    Text(text ="Ayarlar")

                    Icon(
                        imageVector= Icons.Filled.Settings,
                        contentDescription=null,

                        )

                }

                Spacer(modifier = Modifier.padding(10.dp))

                Button(onClick = {},
                    shape = CutCornerShape(8.dp),
                    modifier = Modifier.size(150.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.DarkGray
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 10.dp,
                        pressedElevation = 6.dp
                    ),){
                    Text(text = "Finansal Bilgiler")
                    Icon(
                        imageVector= Icons.Filled.AttachMoney,
                        contentDescription=null)
                }
            }
            Spacer(modifier = Modifier.padding(10.dp))

            Row {
                Button(onClick ={},
                    shape = CutCornerShape(8.dp),
                    modifier = Modifier.size(150.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.DarkGray
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 10.dp,
                        pressedElevation = 6.dp
                    ),
                ) {
                    Text(text = "Takip Edilenler")
                    Icon(Icons.Filled.FollowTheSigns, contentDescription = null)

                }
                Spacer(modifier = Modifier.padding(10.dp))

                Button(onClick = {},shape = CutCornerShape(8.dp),
                    modifier = Modifier.size(150.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.DarkGray
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 10.dp,
                        pressedElevation = 6.dp
                    )) {
                    Text(text = "Çıkış Yap")
                    Icon(imageVector = Icons.Filled.ExitToApp,
                        contentDescription = null)

                }
            }
        }



    }



}

@Preview
@Composable
fun ProfilScreenPrev(){
    ProfilScreen(navController = rememberNavController())
}