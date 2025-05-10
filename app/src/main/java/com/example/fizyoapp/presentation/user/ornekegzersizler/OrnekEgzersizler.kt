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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.fizyoapp.data.local.entity.exerciseexamplesscreen.OrnekEgzersizlerGiris
import com.example.fizyoapp.presentation.navigation.AppScreens
import com.example.fizyoapp.presentation.user.ornekegzersizler.ExercisesExamplesEvent
import com.example.fizyoapp.presentation.user.ornekegzersizler.ExercisesExamplesViewModel
import com.example.fizyoapp.ui.bottomnavbar.BottomNavbarComponent

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun OrnekEgzersizler(
    navController: NavController,
    viewModel: ExercisesExamplesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    state.selectedCategoryId?.let { categoryId ->
        LaunchedEffect(categoryId) {
            when (categoryId) {
                "shoulder" -> navController.navigate(AppScreens.ShoulderExercisesScreen.route)
                "neck" -> navController.navigate(AppScreens.NeckExercisesScreen.route)
                "lower_back" -> navController.navigate(AppScreens.LowerBackExercisesScreen.route)
                "leg" -> navController.navigate(AppScreens.LegExercisesScreen.route)
                "core" -> navController.navigate(AppScreens.CoreExercisesScreen.route)
                "hip" -> navController.navigate(AppScreens.HipExercisesScreen.route)
            }
            viewModel.onEvent(ExercisesExamplesEvent.CategoryNavigationHandled)
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF3B3E68))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Örnek Egzersizler",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        },
        bottomBar = { BottomNavbarComponent(navController) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF2A2D47))
        ) {
            when {
                state.isLoading -> {
                    LoadingIndicator()
                }
                state.error != null -> {
                    ErrorMessage(state.error)
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
private fun LoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            color = Color(0xFF6D72C3),
            strokeWidth = 3.dp
        )
    }
}

@Composable
private fun ErrorMessage(error: String?) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = android.R.drawable.ic_dialog_alert),
                contentDescription = "Error",
                modifier = Modifier.size(48.dp),
                colorFilter = ColorFilter.tint(Color.Red)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Hata: $error",
                color = Color.Red,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CategoryList(
    categories: List<OrnekEgzersizlerGiris>,
    onCategoryClick: (OrnekEgzersizlerGiris) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 72.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(categories.size) { index ->
            val category = categories[index]
            ModernCategoryItem(
                category = category,
                onCategoryClick = { onCategoryClick(category) }
            )
        }
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun ModernCategoryItem(
    category: OrnekEgzersizlerGiris,
    onCategoryClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onCategoryClick)
    ) {
        Image(
            painter = painterResource(id = category.imageResourceId),
            contentDescription = category.baslik,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Dark overlay to make text more readable
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x80000000)
                        )
                    )
                )
        )
        // Category title with card
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xDDFFFFFF))
                    .padding(vertical = 8.dp, horizontal = 16.dp)
                    .align(Alignment.Center)
            ) {
                Text(
                    text = category.baslik ?: "",
                    style = TextStyle(
                        color = Color(0xFF3B3E68),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}