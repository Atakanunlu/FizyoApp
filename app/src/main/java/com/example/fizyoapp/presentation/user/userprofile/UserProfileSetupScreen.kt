package com.example.fizyoapp.presentation.user.userprofile

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fizyoapp.presentation.navigation.AppScreens
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileSetupScreen(
    navController: NavController,
    viewModel: UserProfileViewModel = hiltViewModel(),
    isFirstSetup: Boolean = true
) {
    val state = viewModel.state.collectAsState().value
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showGenderDialog by remember { mutableStateOf(false) }
    var showPhotoOptionsDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val primaryColor = Color(0xFF3B3E68)
    val accentColor = Color(0xFF6D72C3)
    val backgroundColor = Color(0xFFF8F9FC)
    val cardColor = Color.White
    val errorColor = Color(0xFFE57373)
    val dateFormatter = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

    val photoFile = remember {
        try {
            File.createTempFile(
                "temp_camera_photo_",
                ".jpg",
                context.cacheDir
            ).apply {
                deleteOnExit()
            }
        } catch (e: Exception) {
            Log.e("UserProfileSetup", "Dosya oluşturma hatası: ${e.message}")
            File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
        }
    }

    val photoUri = remember {
        try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
        } catch (e: Exception) {
            Log.e("UserProfileSetup", "URI oluşturma hatası: ${e.message}")
            null
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val cacheFile = File(context.cacheDir, "temp_profile_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(it)?.use { input ->
                    cacheFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                selectedImageUri = Uri.fromFile(cacheFile)
                viewModel.onEvent(UserProfileEvent.PhotoChanged(cacheFile.absolutePath))
            } catch (e: Exception) {
                Log.e("UserProfileSetup", "Fotoğraf seçme hatası: ${e.message}", e)
                Toast.makeText(context, "Fotoğraf işlenemedi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            try {
                selectedImageUri = photoUri
                photoFile.absolutePath.let { path ->
                    viewModel.onEvent(UserProfileEvent.PhotoChanged(path))
                }
            } catch (e: Exception) {
                Log.e("UserProfileSetup", "Kamera hatası: ${e.message}", e)
                Toast.makeText(context, "Fotoğraf işlenemedi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && photoUri != null) {
            cameraLauncher.launch(photoUri)
        } else if (isGranted) {
            Toast.makeText(context, "Kamera başlatılamadı", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Kamera izni gerekli", Toast.LENGTH_SHORT).show()
        }
    }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            photoPickerLauncher.launch("image/*")
        } else {
            Toast.makeText(context, "Depolama izni gerekli", Toast.LENGTH_SHORT).show()
        }
    }

    if (showPhotoOptionsDialog) {
        Dialog(onDismissRequest = { showPhotoOptionsDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = cardColor
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Profil Fotoğrafı",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    PhotoOptionItem(
                        icon = Icons.Filled.Camera,
                        text = "Kameradan Çek",
                        iconTint = accentColor
                    ) {
                        if (photoUri != null) {
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                cameraLauncher.launch(photoUri)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        } else {
                            Toast.makeText(context, "Kamera başlatılamadı", Toast.LENGTH_SHORT).show()
                        }
                        showPhotoOptionsDialog = false
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    PhotoOptionItem(
                        icon = Icons.Filled.PhotoLibrary,
                        text = "Galeriden Seç",
                        iconTint = accentColor
                    ) {
                        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            Manifest.permission.READ_MEDIA_IMAGES
                        } else {
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        }
                        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                            photoPickerLauncher.launch("image/*")
                        } else {
                            storagePermissionLauncher.launch(permission)
                        }
                        showPhotoOptionsDialog = false
                    }

                    if (selectedImageUri != null || (state.profilePhotoUrl.isNotEmpty() && state.profilePhotoUrl != "null")) {
                        Spacer(modifier = Modifier.height(8.dp))
                        PhotoOptionItem(
                            icon = Icons.Filled.Delete,
                            text = "Fotoğrafı Kaldır",
                            iconTint = errorColor
                        ) {
                            selectedImageUri = null
                            viewModel.onEvent(UserProfileEvent.PhotoChanged(""))
                            showPhotoOptionsDialog = false
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showPhotoOptionsDialog = false },
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text("Kapat")
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Date(millis)
                        viewModel.onEvent(UserProfileEvent.BirthDateChanged(date))
                    }
                    showDatePicker = false
                }) {
                    Text("Tamam")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("İptal")
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = cardColor,
                titleContentColor = primaryColor,
                headlineContentColor = primaryColor,
                weekdayContentColor = accentColor,
                subheadContentColor = Color.Gray,
                yearContentColor = accentColor,
                currentYearContentColor = primaryColor,
                selectedYearContainerColor = accentColor,
                selectedDayContainerColor = accentColor
            )
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showGenderDialog) {
        Dialog(onDismissRequest = { showGenderDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = cardColor
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Cinsiyet Seçin",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    GenderOptionItem(
                        selected = state.gender == "Erkek",
                        text = "Erkek",
                        accentColor = accentColor
                    ) {
                        viewModel.onEvent(UserProfileEvent.GenderChanged("Erkek"))
                        showGenderDialog = false
                    }

                    GenderOptionItem(
                        selected = state.gender == "Kadın",
                        text = "Kadın",
                        accentColor = accentColor
                    ) {
                        viewModel.onEvent(UserProfileEvent.GenderChanged("Kadın"))
                        showGenderDialog = false
                    }

                    GenderOptionItem(
                        selected = state.gender == "Diğer",
                        text = "Diğer",
                        accentColor = accentColor
                    ) {
                        viewModel.onEvent(UserProfileEvent.GenderChanged("Diğer"))
                        showGenderDialog = false
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showGenderDialog = false },
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text("Kapat")
                    }
                }
            }
        }
    }

    LaunchedEffect(key1 = state.isProfileSaved) {
        if (state.isProfileSaved) {
            if (isFirstSetup) {

                navController.navigate(AppScreens.UserMainScreen.route) {
                    popUpTo(AppScreens.UserProfileSetupScreen.route) { inclusive = true }
                }
            } else {

                navController.popBackStack()
            }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isFirstSetup) "Profil Bilgilerinizi Tamamlayın" else "Profil Bilgilerini Düzenle",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                navigationIcon = {
                    if (!isFirstSetup) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Geri"
                            )
                        }
                    }
                }
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                primaryColor,
                                accentColor
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .shadow(10.dp, CircleShape)
                            .background(Color.White)
                            .border(4.dp, Color.White, CircleShape)
                            .clickable { showPhotoOptionsDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Profil Fotoğrafı",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f))
                                    .clip(CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Değiştir",
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        } else if (state.profilePhotoUrl.isNotEmpty() && state.profilePhotoUrl != "null") {
                            AsyncImage(
                                model = state.profilePhotoUrl,
                                contentDescription = "Profil Fotoğrafı",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                                error = rememberVectorPainter(image = Icons.Default.Person)
                            )
                            // Edit overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f))
                                    .clip(CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Değiştir",
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color.LightGray,
                                                Color.Gray.copy(alpha = 0.7f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddAPhoto,
                                        contentDescription = "Fotoğraf Ekle",
                                        modifier = Modifier.size(50.dp),
                                        tint = Color.White
                                    )
                                    Text(
                                        text = "Fotoğraf Ekle",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = cardColor
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp  // a4 hatası düzeltildi
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Kişisel Bilgiler",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )


                    ProfileTextField(
                        value = state.firstName,
                        onValueChange = { viewModel.onEvent(UserProfileEvent.FirstNameChanged(it)) },
                        label = "İsim",
                        icon = Icons.Outlined.Person,
                        isError = state.firstNameError,
                        errorMessage = "İsim alanı boş bırakılamaz",
                        primaryColor = primaryColor,
                        errorColor = errorColor
                    )

                    Spacer(modifier = Modifier.height(16.dp))


                    ProfileTextField(
                        value = state.lastName,
                        onValueChange = { viewModel.onEvent(UserProfileEvent.LastNameChanged(it)) },
                        label = "Soy İsim",
                        icon = Icons.Outlined.Person,
                        isError = state.lastNameError,
                        errorMessage = "Soyisim alanı boş bırakılamaz",
                        primaryColor = primaryColor,
                        errorColor = errorColor
                    )

                    Spacer(modifier = Modifier.height(16.dp))


                    ProfileClickableField(
                        value = state.birthDate?.let { dateFormatter.format(it) } ?: "",
                        onClick = { showDatePicker = true },
                        label = "Doğum Tarihiniz",
                        icon = Icons.Outlined.CalendarMonth,
                        trailingIcon = Icons.Default.DateRange,
                        isError = state.birthDateError,
                        errorMessage = "Doğum tarihi seçilmelidir",
                        primaryColor = primaryColor,
                        errorColor = errorColor
                    )

                    Spacer(modifier = Modifier.height(16.dp))


                    ProfileClickableField(
                        value = state.gender,
                        onClick = { showGenderDialog = true },
                        label = "Cinsiyet",
                        icon = Icons.Outlined.Face,
                        trailingIcon = Icons.Default.ArrowDropDown,
                        isError = state.genderError,
                        errorMessage = "Cinsiyet seçilmelidir",
                        primaryColor = primaryColor,
                        errorColor = errorColor
                    )
                }
            }


            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = cardColor
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "İletişim Bilgileri",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )


                    ProfileTextField(
                        value = state.city,
                        onValueChange = { viewModel.onEvent(UserProfileEvent.CityChanged(it)) },
                        label = "Şehir",
                        icon = Icons.Outlined.LocationCity,
                        isError = state.cityError,
                        errorMessage = "Şehir alanı boş bırakılamaz",
                        primaryColor = primaryColor,
                        errorColor = errorColor
                    )

                    Spacer(modifier = Modifier.height(16.dp))


                    ProfileTextField(
                        value = state.district,
                        onValueChange = { viewModel.onEvent(UserProfileEvent.DistrictChanged(it)) },
                        label = "İlçe",
                        icon = Icons.Outlined.LocationOn,
                        isError = state.districtError,
                        errorMessage = "İlçe alanı boş bırakılamaz",
                        primaryColor = primaryColor,
                        errorColor = errorColor
                    )

                    Spacer(modifier = Modifier.height(16.dp))


                    ProfileTextField(
                        value = state.phoneNumber,
                        onValueChange = { viewModel.onEvent(UserProfileEvent.PhoneNumberChanged(it)) },
                        label = "Telefon numarası",
                        icon = Icons.Outlined.Phone,
                        isError = state.phoneNumberError,
                        errorMessage = "Telefon numarası alanı boş bırakılamaz",
                        primaryColor = primaryColor,
                        errorColor = errorColor,
                        keyboardType = KeyboardType.Phone
                    )
                }
            }


            if (state.errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = errorColor.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = errorColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = state.errorMessage,
                            color = errorColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }


            Button(
                onClick = { viewModel.onEvent(UserProfileEvent.SaveProfile) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                enabled = !state.isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    disabledContainerColor = primaryColor.copy(alpha = 0.5f)
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Profili Kaydet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PhotoOptionItem(
    icon: ImageVector,
    text: String,
    iconTint: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                fontSize = 16.sp,
                color = if (icon == Icons.Default.Delete) iconTint else Color.DarkGray
            )
        }
    }
}

@Composable
fun GenderOptionItem(
    selected: Boolean,
    text: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) accentColor.copy(alpha = 0.1f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = accentColor
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 16.sp,
                color = if (selected) accentColor else Color.DarkGray,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    isError: Boolean,
    errorMessage: String,
    primaryColor: Color,
    errorColor: Color,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            isError = isError,
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isError) errorColor else primaryColor
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                focusedLabelColor = primaryColor,
                cursorColor = primaryColor,
                errorBorderColor = errorColor,
                errorLeadingIconColor = errorColor,
                errorLabelColor = errorColor
            )
        )
        if (isError) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = errorColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = errorMessage,
                    color = errorColor,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun ProfileClickableField(
    value: String,
    onClick: () -> Unit,
    label: String,
    icon: ImageVector,
    trailingIcon: ImageVector,
    isError: Boolean,
    errorMessage: String,
    primaryColor: Color,
    errorColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            isError = isError,
            readOnly = true,
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isError) errorColor else primaryColor
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = if (isError) errorColor else primaryColor,
                    modifier = Modifier.clickable(onClick = onClick)
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                focusedLabelColor = primaryColor,
                cursorColor = primaryColor,
                errorBorderColor = errorColor,
                errorLeadingIconColor = errorColor,
                errorTrailingIconColor = errorColor,
                errorLabelColor = errorColor
            )
        )
        if (isError) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = errorColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = errorMessage,
                    color = errorColor,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}