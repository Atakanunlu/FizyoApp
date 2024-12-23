package com.example.fizyoapp.ui.bottomnavbar


import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fizyoapp.navigation.AppScreens

@Composable
fun BottomNavbarComponent(navController: NavController){

    var selectedItemIndex by rememberSaveable {
        mutableStateOf(0)
    }

    NavigationBar() {

        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedItemIndex == index,

                onClick = {
                    selectedItemIndex = index

                   // navController.navigate(item.route)
                },
                label = {Text(text = item.title)},

                alwaysShowLabel = false,
                icon ={Icon(
                    imageVector= if(index == selectedItemIndex){
                        item.selectedicon
                        }else
                            item.unselectedicon, contentDescription = item.title
                )}

            )
        }

    }

}

