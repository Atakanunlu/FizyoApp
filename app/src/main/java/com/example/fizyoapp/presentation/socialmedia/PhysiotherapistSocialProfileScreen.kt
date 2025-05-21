// presentation/socialmedia/PhysiotherapistSocialProfileScreen.kt
package com.example.fizyoapp.presentation.socialmedia

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fizyoapp.presentation.navigation.AppScreens
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhysiotherapistSocialProfileScreen(
    navController: NavController,
    physiotherapistId: String? = null,
    viewModel: PhysiotherapistSocialProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val currentRoute = navController.currentBackStackEntry?.destination?.route ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fizyoterapist Profili") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        },
        bottomBar = {
            PhysiotherapistSocialMediaNavbar(navController, currentRoute)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Fizyoterapist Profil Başlığı
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Profil fotoğrafını yükleyip gösterme
                            // Akıllı dönüşüm hatası çözümü için state.profile'ı yerel değişkene atadık
                            val profile = state.profile

                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            ) {
                                // Profil fotoğrafı varsa onu göster
                                if (profile?.profilePhotoUrl?.isNotEmpty() == true) {
                                    AsyncImage(
                                        model = profile.profilePhotoUrl,
                                        contentDescription = "Profil fotoğrafı",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    // Profil fotoğrafı yoksa varsayılan ikon göster
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(60.dp)
                                            .align(Alignment.Center),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // İsim ve Unvan
                            Text(
                                text = "${profile?.firstName ?: ""} ${profile?.lastName ?: ""}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Fizyoterapist",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // İstatistikler
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatItem(title = "Paylaşımlar", value = state.posts.size.toString())
                                StatItem(title = "Beğeniler", value = state.totalLikes.toString())
                                StatItem(title = "Yorumlar", value = state.totalComments.toString())
                            }

                            Divider(modifier = Modifier.padding(vertical = 16.dp))
                        }
                    }

                    // Paylaşımlar Başlığı
                    item {
                        Text(
                            text = "Paylaşımlar",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    // Paylaşımlar Listesi
                    if (state.posts.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Henüz paylaşım yapılmamış",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        items(state.posts) { post ->
                            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    // Paylaşım içeriği
                                    Text(
                                        text = post.content,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )

                                    // İlk medyayı göster (eğer varsa)
                                    if (post.mediaUrls.isNotEmpty()) {
                                        AsyncImage(
                                            model = post.mediaUrls.first(),
                                            contentDescription = "Paylaşım medyası",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )

                                        // Eğer birden fazla medya varsa, kalan sayıyı göster
                                        if (post.mediaUrls.size > 1) {
                                            Text(
                                                text = "+${post.mediaUrls.size - 1} daha fazla",
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.align(Alignment.End)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))
                                    }

                                    // Paylaşım istatistikleri ve tarih
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Favorite,
                                                contentDescription = null,
                                                tint = Color.Red,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "${post.likeCount} beğeni",
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(start = 4.dp)
                                            )

                                            Spacer(modifier = Modifier.width(16.dp))

                                            Icon(
                                                imageVector = Icons.Default.Comment,
                                                contentDescription = null,
                                                tint = Color.Gray,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "${post.commentCount} yorum",
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(start = 4.dp)
                                            )
                                        }

                                        Text(
                                            text = dateFormat.format(post.timestamp),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }

                                    // Paylaşım detay butonu
                                    TextButton(
                                        onClick = {
                                            navController.navigate(AppScreens.PostDetailScreen.createRoute(post.id))
                                        },
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text("Detaylar")
                                    }
                                }
                            }
                        }
                    }

                    // Alt boşluk
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            // Hata gösterimi
            if (state.error != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Snackbar {
                        Text(state.error!!)
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(title: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}