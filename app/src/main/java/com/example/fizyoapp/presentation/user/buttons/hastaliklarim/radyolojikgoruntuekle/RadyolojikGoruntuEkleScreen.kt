package com.example.fizyoapp.presentation.user.buttons.hastaliklarim.radyolojikgoruntuekle
import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RadyolojikGoruntuEkle(navController: NavController) {
    var expanded1 by remember { mutableStateOf(false) }
    var expanded2 by remember { mutableStateOf(false) }
    var expanded3 by remember { mutableStateOf(false) }

    var imageUris1 by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var imageUris2 by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var imageUris3 by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val multiplePhotoPicker1 = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> imageUris1 = imageUris1 + uris }
    )
    val multiplePhotoPicker2 = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> imageUris2 = imageUris2 + uris }
    )
    val multiplePhotoPicker3 = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> imageUris3 = imageUris3 + uris }
    )



    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SectionCard("Radyolojik Görüntülerim", expanded1, { expanded1 = !expanded1 }, imageUris1, multiplePhotoPicker1)
        SectionCard("Raporlarım", expanded2, { expanded2 = !expanded2 }, imageUris2, multiplePhotoPicker2)
        SectionCard("Formlar", expanded3, { expanded3 = !expanded3 }, imageUris3, multiplePhotoPicker3)
    }
}

@Composable
fun SectionCard(
    title: String,
    expanded: Boolean,
    onClick: () -> Unit,
    imageUris: List<Uri>,
    photoPicker: androidx.activity.compose.ManagedActivityResultLauncher<PickVisualMediaRequest, List<Uri>>
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth().padding(20.dp).clickable { onClick() }.background(Color(59, 62, 104))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            if (expanded) {

                ElevatedButton(onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                    Text(text = "$title Ekle")
                }
                Spacer(modifier = Modifier.height(8.dp))
                imageUris.forEach { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(bottom=4.dp,top=4.dp).size(500.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PrevRadyolojik() {
    RadyolojikGoruntuEkle(navController = rememberNavController())
}
