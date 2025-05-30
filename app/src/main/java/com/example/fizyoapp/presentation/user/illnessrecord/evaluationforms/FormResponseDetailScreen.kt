package com.example.fizyoapp.presentation.user.illnessrecord.evaluationforms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.fizyoapp.presentation.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormResponseDetailScreen(
    navController: NavController,
    responseId: String,
    viewModel: FormResponseDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showShareDialog by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = responseId) {
        viewModel.loadResponse(responseId)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.response?.title ?: "Form Yanıtı") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showShareDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Paylaş"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
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
                    CircularProgressIndicator(
                        color = primaryColor
                    )
                }
            } else if (state.error != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = errorColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.error!!,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.loadResponse(responseId) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tekrar Dene")
                    }
                }
            } else if (state.response != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = surfaceColor
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = state.response!!.title ?: "Form Yanıtı",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Score,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Puan: ${state.response!!.score}/${state.response!!.maxScore}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = primaryColor
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tamamlama Tarihi: ${
                                    SimpleDateFormat(
                                        "dd MMM yyyy, HH:mm",
                                        Locale.getDefault()
                                    ).format(state.response!!.dateCompleted)
                                }",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Text(
                        text = "Yanıtlar",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    state.questionMap.entries.forEach { (questionId, questionText) ->
                        val answer = state.response!!.answers[questionId] ?: ""
                        if (answer.isNotEmpty()) {
                            AnswerCard(
                                question = questionText,
                                answer = answer
                            )
                        }
                    }

                    if (state.response!!.notes.isNotEmpty()) {
                        Text(
                            text = "Notlar",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = surfaceColor
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Text(
                                text = state.response!!.notes,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                color = textColor
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }

            if (state.actionError != null) {
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                    action = {
                        TextButton(onClick = { viewModel.dismissError() }) {
                            Text("TAMAM", color = Color.White)
                        }
                    },
                    containerColor = errorColor,
                    contentColor = Color.White
                ) {
                    Text(state.actionError!!)
                }
            }
            AnimatedVisibility(
                visible = state.successMessage != null,
                enter = fadeIn() + slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                ),
                exit = fadeOut() + slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                ),
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = successColor
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = state.successMessage ?: "",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
    if (showShareDialog && state.response != null) {
        ShareDialog(
            threads = state.recentThreads,
            currentUserId = state.currentUserId,
            onDismiss = { showShareDialog = false },
            onShareClicked = { userId ->
                viewModel.shareResponse(state.response!!.id, userId)
                showShareDialog = false
            }
        )
    }
}

@Composable
fun AnswerCard(
    question: String,
    answer: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = question,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = primaryColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = answer,
                fontSize = 16.sp,
                color = textColor
            )
        }
    }
}