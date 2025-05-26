package com.example.fizyoapp.presentation.user.egzersizlerim

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.NotStarted
import androidx.compose.material.icons.rounded.SportsGymnastics
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.fizyoapp.domain.model.exercisemanagescreen.ExercisePlan
import com.example.fizyoapp.domain.model.exercisemanagescreen.ExercisePlanStatus
import java.text.SimpleDateFormat
import java.util.*

private val primaryColor = Color(59, 62, 104)
private val backgroundColor = Color(245, 245, 250)
private val surfaceColor = Color.White
private val accentColor = Color(59, 62, 104)
private val textColor = Color.DarkGray
private val lightGray = Color.Gray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserExercisePlansScreen(
    navController: NavController,
    viewModel: UserExercisePlansViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value
    val context = LocalContext.current

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
                    containerColor = primaryColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { viewModel.refreshPlans() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Yenile",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = primaryColor)
                }
            } else if (state.exercisePlans.isEmpty()) {
                EmptyPlansViewRedesigned()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Egzersiz Programınız",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Fizyoterapistiniz tarafından size özel hazırlanan egzersiz programları",
                        fontSize = 16.sp,
                        color = lightGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )


                    val groupedPlans = state.exercisePlans.groupBy { it.status }


                    val activePlans = groupedPlans[ExercisePlanStatus.ACTIVE] ?: emptyList()
                    if (activePlans.isNotEmpty()) {
                        ExerciseStatusGroup(
                            title = "Aktif Egzersiz Planları",
                            description = "Devam etmekte olan egzersiz programlarınız",
                            icon = Icons.Rounded.DirectionsRun,
                            plans = activePlans,
                            navController = navController
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))


                    val completedPlans = groupedPlans[ExercisePlanStatus.COMPLETED] ?: emptyList()
                    if (completedPlans.isNotEmpty()) {
                        ExerciseStatusGroup(
                            title = "Tamamlanan Egzersiz Planları",
                            description = "Başarıyla tamamladığınız programlar",
                            icon = Icons.Rounded.SportsGymnastics,
                            plans = completedPlans,
                            navController = navController
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))


                    val cancelledPlans = groupedPlans[ExercisePlanStatus.CANCELLED] ?: emptyList()
                    if (cancelledPlans.isNotEmpty()) {
                        ExerciseStatusGroup(
                            title = "İptal Edilen Planlar",
                            description = "Çeşitli nedenlerle iptal edilmiş programlar",
                            icon = Icons.Rounded.NotStarted,
                            plans = cancelledPlans,
                            navController = navController
                        )
                    }


                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(230, 230, 250)
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 0.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Description,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(24.dp)
                            )

                            Text(
                                text = "Egzersiz planlarınızı düzenli olarak takip etmek iyileşme sürecinizi hızlandırır.",
                                fontSize = 14.sp,
                                color = Color.DarkGray,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun ExerciseStatusGroup(
    title: String,
    description: String,
    icon: ImageVector,
    plans: List<ExercisePlan>,
    navController: NavController
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )

                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = lightGray
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))


        plans.forEach { plan ->
            ExercisePlanCardRedesigned(
                plan = plan,
                onClick = {
                    navController.navigate("exercise_plan_detail_screen/${plan.id}")
                }
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ExercisePlanCardRedesigned(
    plan: ExercisePlan,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(59, 62, 104, 20)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FitnessCenter,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))


                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = plan.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )


                        val (color, text) = when (plan.status) {
                            ExercisePlanStatus.ACTIVE -> Pair(Color(0, 150, 136), "Aktif")
                            ExercisePlanStatus.COMPLETED -> Pair(Color(76, 175, 80), "Tamamlandı")
                            ExercisePlanStatus.CANCELLED -> Pair(Color(244, 67, 54), "İptal")
                        }

                        Surface(
                            color = color.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = text,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = color,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (plan.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = plan.description,
                            fontSize = 14.sp,
                            color = textColor.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))


                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        InfoIcon(
                            icon = Icons.Default.DateRange,
                            text = if (plan.startDate != null && plan.endDate != null) {
                                "${dateFormat.format(plan.startDate)} - ${dateFormat.format(plan.endDate)}"
                            } else {
                                "Tarih belirtilmedi"
                            }
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        InfoIcon(
                            icon = Icons.Default.FitnessCenter,
                            text = "${plan.exercises.size} egzersiz"
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        InfoIcon(
                            icon = Icons.Default.Repeat,
                            text = plan.frequency
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        InfoIcon(
                            icon = Icons.Default.Event,
                            text = "Oluşturulma: ${dateFormat.format(plan.createdAt)}"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))


            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = "Görüntüle"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Plan Detaylarını Görüntüle",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun InfoIcon(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = primaryColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = textColor.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun EmptyPlansViewRedesigned() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(59, 62, 104, 20)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.FitnessCenter,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Egzersiz Planı Bulunamadı",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = primaryColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Fizyoterapistiniz size egzersiz planı atadığında burada görüntülenecektir.",
            fontSize = 16.sp,
            color = lightGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))


        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(230, 230, 250)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = "Fizyoterapistinizden egzersiz planınızı oluşturmasını isteyebilirsiniz.",
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        }
    }
}