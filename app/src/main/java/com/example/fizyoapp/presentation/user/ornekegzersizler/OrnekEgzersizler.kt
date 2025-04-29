// com.example.fizyoapp.presentation.user.ornekegzersizler.OrnekEgzersizler.kt
package com.example.fizyoapp.presentation.user.ornekegzersizler


import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.fizyoapp.data.local.entity.exerciseexamplesscreen.OrnekEgzersizlerGiris
import com.example.fizyoapp.presentation.navigation.AppScreens
import com.example.fizyoapp.ui.bottomnavbar.BottomNavbarComponent

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun OrnekEgzersizler(
    navController: NavController,
    viewModel: ExercisesExamplesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    // Seçilen kategori ID'sini gözlemleme
    state.selectedCategoryId?.let { categoryId ->
        LaunchedEffect(categoryId) {
            // Kategori ID'sine göre navigasyon yapma
            when (categoryId) {
                "shoulder" -> navController.navigate(AppScreens.ShoulderExercisesScreen.route)
                "neck" -> navController.navigate(AppScreens.NeckExercisesScreen.route)
                "lower_back" -> navController.navigate(AppScreens.LowerBackExercisesScreen.route)
                "leg" -> navController.navigate(AppScreens.LegExercisesScreen.route)
                "core" -> navController.navigate(AppScreens.CoreExercisesScreen.route)
                "hip" -> navController.navigate(AppScreens.HipExercisesScreen.route)
            }
            // Navigasyonun tamamlandığını ViewModel'e bildir
            viewModel.onEvent(ExercisesExamplesEvent.CategoryNavigationHandled)
        }
    }

    Scaffold(bottomBar = { BottomNavbarComponent(navController) }) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(59, 62, 104))
        ) {
            when {
                 state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White
                    )
                }
                state.error != null -> {
                    Text(
                        text = "Hata: ${state.error}",
                        color = Color.Red,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                else -> {
                    CategoryList(
                        categories = state.categories,
                        onCategoryClick = { category ->
                            viewModel.onEvent(ExercisesExamplesEvent.CategorySelected(category))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryList(
    categories: List<OrnekEgzersizlerGiris>,
    onCategoryClick: (OrnekEgzersizlerGiris) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(categories.size) { index ->
            val category = categories[index]
            CategoryItem(
                category = category,
                onCategoryClick = { onCategoryClick(category) }
            )
        }
        item {
            Spacer(modifier = Modifier.height(100.dp)) // BottomNavbarComponent için alan
        }
    }
}

@Composable
private fun CategoryItem(
    category: OrnekEgzersizlerGiris,
    onCategoryClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(vertical = 15.dp, horizontal = 30.dp)
            .fillMaxWidth()
            .clickable(onClick = onCategoryClick)
    ) {
        Image(
            painter = painterResource(id = category.imageResourceId),
            contentDescription = category.baslik,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .height(130.dp)
        )
        Text(
            text = category.baslik ?: "",
            style = TextStyle(
                color = Color.DarkGray,
                fontSize = 25.sp,
                fontStyle = FontStyle.Italic
            ),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
