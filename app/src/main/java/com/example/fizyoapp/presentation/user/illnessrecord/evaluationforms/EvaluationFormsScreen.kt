package com.example.fizyoapp.presentation.user.illnessrecord.evaluationforms

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import com.example.fizyoapp.domain.model.illnesrecordscreen.evaluationforms.EvaluationForm
import com.example.fizyoapp.domain.model.illnesrecordscreen.evaluationforms.FormResponse
import com.example.fizyoapp.domain.model.messagesscreen.ChatThread
import com.example.fizyoapp.presentation.navigation.AppScreens
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluationFormsScreen(
    navController: NavController,
    viewModel: EvaluationFormsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedResponse by remember { mutableStateOf<FormResponse?>(null) }
    var showShareDialog by remember { mutableStateOf(false) }

    // İlk yükleme için
    LaunchedEffect(Unit) {
        // Sayfa açıldığında verileri yükle
        viewModel.onEvent(EvaluationFormsEvent.RefreshData)
    }

    // Periyodik yenileme için
    LaunchedEffect(Unit) {
        while(true) {
            // 60 saniyede bir otomatik yenileme
            delay(60000)
            viewModel.refreshUserResponses()
        }
    }

    // NavController'dan dönüş için yenileme
    LaunchedEffect(Unit) {
        val navBackStackEntry = navController.currentBackStackEntry
        navBackStackEntry?.lifecycle?.addObserver(object : androidx.lifecycle.DefaultLifecycleObserver {
            override fun onResume(owner: androidx.lifecycle.LifecycleOwner) {
                // Ekrana geri dönüldüğünde verileri yenile
                viewModel.refreshUserResponses()
            }
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Değerlendirme Formları") },
                actions = {
                    IconButton(onClick = { viewModel.refreshUserResponses() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Yenile",
                            tint = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(59, 62, 104),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(245, 245, 250))
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(59, 62, 104)
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
                        imageVector = Icons.Outlined.Error,
                        contentDescription = null,
                        tint = Color.Red.copy(alpha = 0.7f),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.error!!,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.onEvent(EvaluationFormsEvent.RefreshData) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(59, 62, 104)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Yenile")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Tüm değerlendirme formları
                    item {
                        Text(
                            text = "Tüm Değerlendirme Formları",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(59, 62, 104),
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                    if (state.forms.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Assignment,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Henüz form bulunmuyor",
                                        textAlign = TextAlign.Center,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    } else {
                        items(state.forms) { form ->
                            EvaluationFormItem(
                                form = form,
                                onClick = {
                                    navController.navigate(AppScreens.EvaluationFormDetailScreen.route + "/${form.id}")
                                }
                            )
                        }
                    }
                    // Tamamlanan formlar
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Tamamlanan Formlarınız (${state.userResponses.size})",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(59, 62, 104)
                            )

                            // Küçük yenileme butonu
                            IconButton(
                                onClick = { viewModel.refreshUserResponses() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(59, 62, 104).copy(alpha = 0.1f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Yanıtları Yenile",
                                    tint = Color(59, 62, 104),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    if (state.userResponses.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AssignmentTurnedIn,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Henüz bir form tamamlamadınız",
                                        textAlign = TextAlign.Center,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    } else {
                        items(state.userResponses) { response ->
                            FormResponseItem(
                                response = response,
                                onClick = {
                                    navController.navigate(AppScreens.FormResponseDetailScreen.route + "/${response.id}")
                                },
                                onShareClick = {
                                    selectedResponse = response
                                    showShareDialog = true
                                },
                                onDeleteClick = {
                                    viewModel.onEvent(EvaluationFormsEvent.DeleteFormResponse(response.id))
                                }
                            )
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
            // Aksiyon mesajları
            if (state.actionError != null) {
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                    action = {
                        TextButton(onClick = { viewModel.onEvent(EvaluationFormsEvent.DismissError) }) {
                            Text("TAMAM", color = Color.White)
                        }
                    },
                    containerColor = Color(0xFFB71C1C),
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
                        containerColor = Color(0xFF4CAF50)
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

            // Pull-to-Refresh efekti gösterildiğinde yenileme göstergesi
            AnimatedVisibility(
                visible = state.isLoading,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            ) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(59, 62, 104)
                )
            }
        }
    }
    if (showShareDialog && selectedResponse != null) {
        ShareDialog(
            threads = state.recentThreads,
            currentUserId = state.currentUserId,
            onDismiss = {
                showShareDialog = false
                selectedResponse = null
            },
            onShareClicked = { userId ->
                viewModel.onEvent(
                    EvaluationFormsEvent.ShareFormResponse(
                        selectedResponse!!.id,
                        userId
                    )
                )
                showShareDialog = false
                selectedResponse = null
            }
        )
    }
}

@Composable
fun EvaluationFormItem(
    form: EvaluationForm,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (form.isCompleted) Color(59, 62, 104, 0x40)
                        else Color(59, 62, 104, 0x20)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (form.isCompleted) Icons.Outlined.AssignmentTurnedIn else Icons.Outlined.Assignment,
                    contentDescription = "Form",
                    tint = Color(59, 62, 104),
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = form.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(59, 62, 104)
                )
                if (form.description.isNotEmpty()) {
                    Text(
                        text = form.description,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            if (form.isCompleted) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Tamamlandı",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = onClick
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Detay",
                        tint = Color(59, 62, 104)
                    )
                }
            }
        }
    }
}

@Composable
fun FormResponseItem(
    response: FormResponse,
    onClick: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(59, 62, 104, 0x40)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AssignmentTurnedIn,
                        contentDescription = "Tamamlanan Form",
                        tint = Color(59, 62, 104),
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = response.title ?: "Form Yanıtı",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(59, 62, 104)
                    )
                    Text(
                        text = "Puan: ${response.score}/${response.maxScore}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(59, 62, 104)
                    )
                    Text(
                        text = "Tamamlanma: ${
                            SimpleDateFormat(
                                "dd MMM yyyy",
                                Locale.getDefault()
                            ).format(response.dateCompleted)
                        }",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                IconButton(
                    onClick = { showDeleteConfirmation = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Sil",
                        tint = Color.Red.copy(alpha = 0.7f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(59, 62, 104)
                    ),
                    border = BorderStroke(1.dp, Color(59, 62, 104))
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Görüntüle"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Görüntüle")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onShareClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(59, 62, 104)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Paylaş"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Paylaş")
                }
            }
        }
    }
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Form Yanıtını Sil") },
            text = { Text("Bu form yanıtını silmek istediğinize emin misiniz?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteClick()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("İptal")
                }
            }
        )
    }
}

@Composable
fun ShareDialog(
    threads: List<ChatThread>,
    currentUserId: String,
    onDismiss: () -> Unit,
    onShareClicked: (String) -> Unit
) {
    println("ShareDialog içinde thread sayısı: ${threads.size}")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Değerlendirme Formunu Paylaş",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Formu göndermek istediğiniz kişiyi seçin",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (threads.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Mesajlaştığınız kimse bulunmuyor",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(threads) { thread ->
                            println("Thread gösteriliyor: ${thread.otherParticipantName}")
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val otherUserId = thread.participantIds.firstOrNull {
                                            it != currentUserId
                                        } ?: return@clickable
                                        onShareClicked(otherUserId)
                                    },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFF5F5F5)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(59, 62, 104, 0x33))
                                    ) {
                                        if (thread.otherParticipantPhotoUrl.isNotEmpty()) {
                                            AsyncImage(
                                                model = thread.otherParticipantPhotoUrl,
                                                contentDescription = "Profil fotoğrafı",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = "Profil",
                                                tint = Color(59, 62, 104),
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(8.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = thread.otherParticipantName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss
                    ) {
                        Text("İptal")
                    }
                }
            }
        }
    }
}