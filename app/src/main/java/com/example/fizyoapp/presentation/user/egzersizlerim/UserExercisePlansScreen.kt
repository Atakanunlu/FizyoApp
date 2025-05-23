package com.example.fizyoapp.presentation.user.egzersizlerim

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.fizyoapp.domain.model.exercise.ExercisePlan
import com.example.fizyoapp.domain.model.exercise.ExercisePlanStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserExercisePlansScreen(
    navController: NavController,
    viewModel: UserExercisePlansViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value
    val context = LocalContext.current

    // Hata mesajını göster
    LaunchedEffect(state.error) {
        if (state.error != null) {
            Toast.makeText(context, state.error, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Egzersiz Planlarım") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    // Yenileme butonu
                    IconButton(onClick = { viewModel.refreshPlans() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Yenile",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.exercisePlans.isEmpty()) {
                EmptyPlansView()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Aktif Planlarınız",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Planları grupla: Önce aktif planlar, sonra tamamlanan, en son iptal edilenler
                        val groupedPlans = state.exercisePlans.groupBy { it.status }

                        // Aktif planlar
                        val activePlans = groupedPlans[ExercisePlanStatus.ACTIVE] ?: emptyList()
                        if (activePlans.isNotEmpty()) {
                            items(activePlans) { plan ->
                                ExercisePlanCard(
                                    plan = plan,
                                    onClick = {
                                        // Plan detaylarına git
                                        navController.navigate("exercise_plan_detail_screen/${plan.id}")
                                    }
                                )
                            }
                        }

                        // Tamamlanan planlar
                        val completedPlans = groupedPlans[ExercisePlanStatus.COMPLETED] ?: emptyList()
                        if (completedPlans.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tamamlanan Planlar",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(completedPlans) { plan ->
                                ExercisePlanCard(
                                    plan = plan,
                                    onClick = {
                                        navController.navigate("exercise_plan_details/${plan.id}")
                                    }
                                )
                            }
                        }

                        // İptal edilen planlar
                        val cancelledPlans = groupedPlans[ExercisePlanStatus.CANCELLED] ?: emptyList()
                        if (cancelledPlans.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "İptal Edilen Planlar",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(cancelledPlans) { plan ->
                                ExercisePlanCard(
                                    plan = plan,
                                    onClick = {
                                        navController.navigate("exercise_plan_details/${plan.id}")
                                    }
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
fun EmptyPlansView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Henüz egzersiz planınız bulunmuyor",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Fizyoterapistiniz size egzersiz planı atadığında burada görüntülenecektir",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ExercisePlanCard(
    plan: ExercisePlan,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Başlık ve durum
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = plan.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Durum göstergesi
                val (color, text) = when (plan.status) {
                    ExercisePlanStatus.ACTIVE -> Pair(MaterialTheme.colorScheme.primary, "Aktif")
                    ExercisePlanStatus.COMPLETED -> Pair(MaterialTheme.colorScheme.tertiary, "Tamamlandı")
                    ExercisePlanStatus.CANCELLED -> Pair(MaterialTheme.colorScheme.error, "İptal Edildi")
                }

                Surface(
                    color = color.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelMedium,
                        color = color,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Açıklama
            if (plan.description.isNotBlank()) {
                Text(
                    text = plan.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Tarih aralığı
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))

                val dateText = if (plan.startDate != null && plan.endDate != null) {
                    "${dateFormat.format(plan.startDate)} - ${dateFormat.format(plan.endDate)}"
                } else if (plan.startDate != null) {
                    "${dateFormat.format(plan.startDate)}'den itibaren"
                } else {
                    "Tarih belirtilmedi"
                }

                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Egzersiz sayısı ve sıklık
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${plan.exercises.size} egzersiz",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.width(16.dp))

                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = plan.frequency,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Oluşturulma tarihi
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Oluşturulma: ${dateFormat.format(plan.createdAt)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Görüntüle butonu
            Button(
                onClick = onClick,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = "Görüntüle"
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Görüntüle")
            }
        }
    }
}