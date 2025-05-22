package com.example.fizyoapp.presentation.user.illnessrecord.evaluationforms


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
import com.example.fizyoapp.domain.model.illnesrecordscreen.evaluationforms.FormQuestion
import com.example.fizyoapp.domain.model.illnesrecordscreen.evaluationforms.QuestionType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluationFormDetailScreen(
    navController: NavController,
    formId: String,
    viewModel: EvaluationFormDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(key1 = formId) {
        viewModel.loadForm(formId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.form?.title ?: "Form Detayı") },
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
                        imageVector = Icons.Default.Error,
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
                        onClick = { viewModel.loadForm(formId) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(59, 62, 104)
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
            } else if (state.form != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Form bilgileri
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
                                .padding(16.dp)
                        ) {
                            Text(
                                text = state.form!!.title,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(59, 62, 104)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (state.form!!.description.isNotEmpty()) {
                                Text(
                                    text = state.form!!.description,
                                    fontSize = 16.sp,
                                    color = Color.DarkGray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            Text(
                                text = "Oluşturulma Tarihi: ${
                                    SimpleDateFormat(
                                        "dd MMM yyyy",
                                        Locale.getDefault()
                                    ).format(state.form!!.dateCreated)
                                }",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )

                            if (state.form!!.maxScore > 0) {
                                Text(
                                    text = "Maksimum Puan: ${state.form!!.maxScore}",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    // Sorular
                    Text(
                        text = "Sorular",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(59, 62, 104),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    state.form!!.questions.forEachIndexed { index, question ->
                        QuestionCard(
                            question = question,
                            index = index,
                            answer = state.answers[question.id] ?: "",
                            onAnswerChanged = { viewModel.updateAnswer(question.id, it) }
                        )
                    }

                    // Notlar
                    Text(
                        text = "Notlar",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(59, 62, 104),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    OutlinedTextField(
                        value = state.notes,
                        onValueChange = { viewModel.updateNotes(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .height(120.dp),
                        placeholder = { Text("Notlarınızı buraya yazabilirsiniz...") },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(59, 62, 104),
                            cursorColor = Color(59, 62, 104)
                        )
                    )

                    // Gönder butonu
                    Button(
                        onClick = {
                            viewModel.submitForm()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(59, 62, 104)
                        ),
                        shape = RoundedCornerShape(28.dp),
                        enabled = !state.isSaving
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Gönder"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Değerlendirmeyi Tamamla")
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }

            // Başarı veya hata mesajları
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
                    containerColor = Color(0xFFB71C1C),
                    contentColor = Color.White
                ) {
                    Text(state.actionError!!)
                }
            }
        }
    }

    // Form başarıyla gönderildiğinde geriye dön
    LaunchedEffect(key1 = state.isSuccess) {
        if (state.isSuccess) {
            navController.popBackStack()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionCard(
    question: FormQuestion,
    index: Int,
    answer: String,
    onAnswerChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            color = Color(59, 62, 104),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = question.text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(59, 62, 104),
                    modifier = Modifier.weight(1f)
                )

                if (question.required) {
                    Text(
                        text = "*",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (question.type) {
                QuestionType.TEXT -> {
                    OutlinedTextField(
                        value = answer,
                        onValueChange = onAnswerChanged,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Yanıtınızı yazın...") },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(59, 62, 104),
                            cursorColor = Color(59, 62, 104)
                        )
                    )
                }
                QuestionType.NUMBER -> {
                    OutlinedTextField(
                        value = answer,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                onAnswerChanged(newValue)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Sayısal değer girin...") },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(59, 62, 104),
                            cursorColor = Color(59, 62, 104)
                        )
                    )
                }
                QuestionType.SCALE -> {
                    val min = question.minValue ?: 0
                    val max = question.maxValue ?: 10
                    val currentValue = answer.toIntOrNull() ?: min

                    Text(
                        text = "Ölçek: $min - $max",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$min",
                            color = Color.Gray
                        )

                        Slider(
                            value = currentValue.toFloat(),
                            onValueChange = { onAnswerChanged(it.toInt().toString()) },
                            valueRange = min.toFloat()..max.toFloat(),
                            steps = max - min - 1,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = Color(59, 62, 104),
                                activeTrackColor = Color(59, 62, 104)
                            )
                        )

                        Text(
                            text = "$max",
                            color = Color.Gray
                        )
                    }

                    Text(
                        text = "Seçilen değer: $currentValue",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = Color(59, 62, 104),
                        fontWeight = FontWeight.Bold
                    )
                }
                QuestionType.MULTIPLE_CHOICE -> {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        question.options.forEach { option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = answer == option,
                                    onClick = { onAnswerChanged(option) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color(59, 62, 104)
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = option,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                QuestionType.YES_NO -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { onAnswerChanged("Evet") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (answer == "Evet") Color(59, 62, 104) else Color.Gray
                            )
                        ) {
                            Text("Evet")
                        }

                        Button(
                            onClick = { onAnswerChanged("Hayır") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (answer == "Hayır") Color(59, 62, 104) else Color.Gray
                            )
                        ) {
                            Text("Hayır")
                        }
                    }
                }

            }
        }
    }
}