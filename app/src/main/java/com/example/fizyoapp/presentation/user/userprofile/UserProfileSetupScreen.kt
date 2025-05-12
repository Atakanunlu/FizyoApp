package com.example.fizyoapp.presentation.user.userprofile

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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
        AlertDialog(
            onDismissRequest = { showPhotoOptionsDialog = false },
            title = { Text("Profil Fotoğrafı") },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
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
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Camera,
                            contentDescription = "Kamera",
                            modifier = Modifier.padding(end = 16.dp)
                        )
                        Text("Kameradan Çek")
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                    Manifest.permission.READ_MEDIA_IMAGES
                                } else {
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                                }

                                if (ContextCompat.checkSelfPermission(context, permission) ==
                                    PackageManager.PERMISSION_GRANTED) {
                                    photoPickerLauncher.launch("image/*")
                                } else {
                                    storagePermissionLauncher.launch(permission)
                                }
                                showPhotoOptionsDialog = false
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Galeri",
                            modifier = Modifier.padding(end = 16.dp)
                        )
                        Text("Galeriden Seç")
                    }

                    if (selectedImageUri != null || (state.profilePhotoUrl.isNotEmpty() && state.profilePhotoUrl != "null")) {
                        HorizontalDivider()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedImageUri = null
                                    viewModel.onEvent(UserProfileEvent.PhotoChanged(""))
                                    showPhotoOptionsDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Kaldır",
                                modifier = Modifier.padding(end = 16.dp),
                                tint = Color.Red
                            )
                            Text("Fotoğrafı Kaldır", color = Color.Red)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPhotoOptionsDialog = false }) {
                    Text("İptal")
                }
            }
        )
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
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showGenderDialog) {
        AlertDialog(
            onDismissRequest = { showGenderDialog = false },
            title = { Text("Cinsiyet Seçin") },
            text = {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.onEvent(UserProfileEvent.GenderChanged("Erkek"))
                                showGenderDialog = false
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = state.gender == "Erkek",
                            onClick = null
                        )
                        Text("Erkek", modifier = Modifier.padding(start = 8.dp))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.onEvent(UserProfileEvent.GenderChanged("Kadın"))
                                showGenderDialog = false
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = state.gender == "Kadın",
                            onClick = null
                        )
                        Text("Kadın", modifier = Modifier.padding(start = 8.dp))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.onEvent(UserProfileEvent.GenderChanged("Diğer"))
                                showGenderDialog = false
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = state.gender == "Diğer",
                            onClick = null
                        )
                        Text("Diğer", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGenderDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    LaunchedEffect(key1 = state.isProfileSaved) {
        if (state.isProfileSaved) {
            if (isFirstSetup) {
                // İlk kurulumsa, ana ekrana yönlendir
                navController.navigate(AppScreens.UserMainScreen.route) {
                    popUpTo(AppScreens.UserProfileSetupScreen.route) { inclusive = true }
                }
            } else {
                // Güncelleme ise, bir önceki ekrana dön
                navController.popBackStack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = if (isFirstSetup) "Profil Bilgilerinizi Tamamlayın" else "Profil Bilgilerini Düzenle")
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape)
                    .clickable { showPhotoOptionsDialog = true },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {

                    Log.d("UserProfileSetup", "Displaying local photo: $selectedImageUri")
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Profil Fotoğrafı",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (state.profilePhotoUrl.isNotEmpty() && state.profilePhotoUrl != "null") {

                    Log.d("UserProfileSetup", "Displaying Firebase photo: ${state.profilePhotoUrl}")
                    AsyncImage(
                        model = state.profilePhotoUrl,
                        contentDescription = "Profil Fotoğrafı",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = rememberVectorPainter(image = Icons.Default.Person)
                    )
                } else {
                    Log.d("UserProfileSetup", "No photo to display")
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profil Fotoğrafı Ekle",
                            modifier = Modifier.size(40.dp),
                            tint = Color.Gray
                        )
                        Text(
                            text = "Fotoğraf Ekle",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = state.firstName,
                onValueChange = { viewModel.onEvent(UserProfileEvent.FirstNameChanged(it)) },
                label = { Text("İsim") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.firstNameError
            )
            if (state.firstNameError) {
                Text(
                    text = "İsim alanı boş bırakılamaz",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.lastName,
                onValueChange = { viewModel.onEvent(UserProfileEvent.LastNameChanged(it)) },
                label = { Text("Soy İsim") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.lastNameError
            )
            if (state.lastNameError) {
                Text(
                    text = "Soyisim alanı boş bırakılamaz",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.birthDate?.let { dateFormatter.format(it) } ?: "",
                onValueChange = { },
                label = { Text("Doğum Tarihiniz") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Takvim",
                        modifier = Modifier.clickable { showDatePicker = true }
                    )
                },
                isError = state.birthDateError
            )
            if (state.birthDateError) {
                Text(
                    text = "Doğum tarihi seçilmelidir",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.gender,
                onValueChange = { },
                label = { Text("Cinsiyet") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showGenderDialog = true },
                readOnly = true,
                isError = state.genderError,
                trailingIcon = {
                    IconButton(onClick = { showGenderDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Cinsiyet Seç"
                        )
                    }
                }
            )
            if (state.genderError) {
                Text(
                    text = "Cinsiyet seçilmelidir",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.city,
                onValueChange = { viewModel.onEvent(UserProfileEvent.CityChanged(it)) },
                label = { Text("Şehir") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.cityError
            )
            if (state.cityError) {
                Text(
                    text = "Şehir alanı boş bırakılamaz",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.district,
                onValueChange = { viewModel.onEvent(UserProfileEvent.DistrictChanged(it)) },
                label = { Text("İlçe") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.districtError
            )
            if (state.districtError) {
                Text(
                    text = "İlçe alanı boş bırakılamaz",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.phoneNumber,
                onValueChange = { viewModel.onEvent(UserProfileEvent.PhoneNumberChanged(it)) },
                label = { Text("Telefon numarası") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.phoneNumberError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            if (state.phoneNumberError) {
                Text(
                    text = "Telefon numarası alanı boş bırakılamaz",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))


            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = { viewModel.onEvent(UserProfileEvent.SaveProfile) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text("Kaydet")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}