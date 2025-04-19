package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_profile_screen

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
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
fun PhysiotherapistProfileSetupScreen(
    navController: NavController,
    viewModel: PhysiotherapistProfileViewModel = hiltViewModel(),
    isFirstSetup: Boolean = true
) {
    val state = viewModel.state.collectAsState().value
    val context = LocalContext.current

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showGenderDialog by remember { mutableStateOf(false) }
    var showPhotoOptionsDialog by remember { mutableStateOf(false) }
    var certificatesText by remember { mutableStateOf(state.certificates.joinToString("\n")) }

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
            File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
        }
    }


    val photoUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            viewModel.onEvent(PhysiotherapistProfileEvent.PhotoChanged(it.toString()))
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImageUri = photoUri
            viewModel.onEvent(PhysiotherapistProfileEvent.PhotoChanged(photoUri.toString()))
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(photoUri)
        }
    }

    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            photoPickerLauncher.launch("image/*")
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
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    cameraLauncher.launch(photoUri)
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
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
                                val permission =
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                        Manifest.permission.READ_MEDIA_IMAGES
                                    } else {
                                        Manifest.permission.READ_EXTERNAL_STORAGE
                                    }

                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        permission
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    photoPickerLauncher.launch("image/*")
                                } else {
                                    galleryPermissionLauncher.launch(permission)
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
                                    viewModel.onEvent(PhysiotherapistProfileEvent.PhotoRemoved)
                                    showPhotoOptionsDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Kaldır",
                                tint = Color.Red,
                                modifier = Modifier.padding(end = 16.dp)
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
                        viewModel.onEvent(PhysiotherapistProfileEvent.BirthDateChanged(date))
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
                                viewModel.onEvent(PhysiotherapistProfileEvent.GenderChanged("Erkek"))
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
                                viewModel.onEvent(PhysiotherapistProfileEvent.GenderChanged("Kadın"))
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
                                viewModel.onEvent(PhysiotherapistProfileEvent.GenderChanged("Diğer"))
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
                navController.navigate(AppScreens.PhysiotherapistMainScreen.route) {
                    popUpTo(AppScreens.PhysiotherapistProfileSetupScreen.route) { inclusive = true }
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
                    Text(text = if (isFirstSetup) "Fizyoterapist Profil Bilgileri" else "Profil Bilgilerini Düzenle")
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
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Profil Fotoğrafı",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (state.profilePhotoUrl.isNotEmpty() && state.profilePhotoUrl != "null") {
                    AsyncImage(
                        model = state.profilePhotoUrl,
                        contentDescription = "Profil Fotoğrafı",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
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
                onValueChange = { viewModel.onEvent(PhysiotherapistProfileEvent.FirstNameChanged(it)) },
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
                onValueChange = { viewModel.onEvent(PhysiotherapistProfileEvent.LastNameChanged(it)) },
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
                onValueChange = { viewModel.onEvent(PhysiotherapistProfileEvent.CityChanged(it)) },
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
                onValueChange = { viewModel.onEvent(PhysiotherapistProfileEvent.DistrictChanged(it)) },
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
                value = state.fullAddress,
                onValueChange = {
                    viewModel.onEvent(
                        PhysiotherapistProfileEvent.FullAddressChanged(
                            it
                        )
                    )
                },
                label = { Text("Açık Adres") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                isError = state.fullAddressError,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text
                )
            )
            if (state.fullAddressError) {
                Text(
                    text = "Açık adres alanı boş bırakılamaz",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.phoneNumber,
                onValueChange = {
                    viewModel.onEvent(
                        PhysiotherapistProfileEvent.PhoneNumberChanged(
                            it
                        )
                    )
                },
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

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = certificatesText,
                onValueChange = {
                    certificatesText = it
                    val certificates = if (it.isBlank()) emptyList() else it.split("\n")
                        .filter { line -> line.isNotBlank() }
                    viewModel.onEvent(PhysiotherapistProfileEvent.CertificatesChanged(certificates))
                },
                label = { Text("Sertifikalar (Opsiyonel)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text
                ),
                placeholder = { Text("Her satıra bir sertifika yazın") }
            )

            Spacer(modifier = Modifier.height(8.dp))


            OutlinedTextField(
                value = state.priceInfo,
                onValueChange = { viewModel.onEvent(PhysiotherapistProfileEvent.PriceInfoChanged(it)) },
                label = { Text("Fiyat Bilgilendirmesi") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text
                ),
                placeholder = { Text("Fiyat bilgilerinizi buraya yazabilirsiniz. Boş bırakırsanız 'Görüşme sonunda bilgilendirilecektir' yazısı eklenecektir.") }
            )

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
                onClick = { viewModel.onEvent(PhysiotherapistProfileEvent.SaveProfile) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
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