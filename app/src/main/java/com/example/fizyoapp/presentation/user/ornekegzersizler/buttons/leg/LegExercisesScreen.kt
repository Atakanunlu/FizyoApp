package com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.core

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.leg.LegExercisesOfExamplesViewModel
import kotlinx.coroutines.launch

/**
 * CoreExercisesScreen: Core egzersiz videolarını gösteren Compose ekranı
 *
 * Bu ekran, karın ve bel bölgesi (core) egzersizlerini video olarak gösterir,
 * kullanıcının videolar arasında gezinmesini sağlar ve her video için açıklamalar sunar.
 *
 * @param navController Ekranlar arası navigasyonu kontrol eden nesne
 */
@androidx.annotation.OptIn(UnstableApi::class) // Media3 API'nin deneysel olduğunu belirtir
@OptIn(ExperimentalMaterial3Api::class) // TopAppBar için gerekli annotation
@Composable
fun LegExercisesScreen(navController: NavController) {
    // ViewModel'i Hilt kullanarak enjekte eder - videoları ve verileri yönetmek için
    val viewModel: LegExercisesOfExamplesViewModel = hiltViewModel()

    // ViewModel'den video listesini alır ve UI'da değişiklikleri gözlemlemek için state'e dönüştürür
    val videoList = viewModel.videoList.collectAsState()

    // Şu anda gösterilen videonun listede kaçıncı sırada olduğunu takip eder
    var currentIndex by remember { mutableStateOf(0) }

    // Asenkron işlemler (navigasyon, ExoPlayer işlemleri vb.) için coroutine scope
    val coroutineScope = rememberCoroutineScope()

    // Android bağlamını alır - ExoPlayer oluşturmak için gerekli
    val context = LocalContext.current

    // ExoPlayer nesnesini oluşturur ve yapılandırır - video oynatma motoru
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            // Videoların sürekli tekrarlanmasını sağlar
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }

    // Video oynatıcının görünürlüğünü kontrol eder - navigasyon sırasında gizlemek için
    var showPlayer by remember { mutableStateOf(true) }

    // PlayerView referansını tutar - ExoPlayer'ı bağlamak ve kaynakları temizlemek için
    var playerView by remember { mutableStateOf<PlayerView?>(null) }

    // Geri navigasyon işlemini gerçekleştiren fonksiyon - temizleme ve navigasyon
    val navigateBack = {
        coroutineScope.launch {
            // PlayerView'ı gizler - video görüntüsünün ekranda kalmasını önler
            showPlayer = false

            // ExoPlayer'ı durdurur - ses ve video oynatmayı bitirir
            exoPlayer.stop()

            // PlayerView'dan ExoPlayer bağlantısını kaldırır
            playerView?.player = null

            // PlayerView referansını temizler
            playerView = null

            // ExoPlayer'ın kullandığı bellek kaynaklarını serbest bırakır
            exoPlayer.release()

            // Önceki ekrana geri döner
            navController.navigateUp()
        }
    }

    // Sistem geri tuşuna basınca çalışacak işleyici - Android back tuşu için
    BackHandler { navigateBack() }

    // Compose bileşeni yok edildiğinde kaynakları temizler
    // (ekrandan çıkılınca veya uygulama kapatılınca)
    DisposableEffect(Unit) {
        onDispose {
            // ExoPlayer kaynaklarını serbest bırakır
            exoPlayer.stop()
            playerView?.player = null
            exoPlayer.release()
        }
    }

    // Ekran ilk açıldığında videoları yükler
    LaunchedEffect(Unit) {
        viewModel.loadVideos()
    }

    // Video değiştiğinde (kullanıcı ileri/geri tuşlarına bastığında) veya
    // video listesi değiştiğinde ExoPlayer'ı günceller
    LaunchedEffect(currentIndex, videoList.value) {
        if (videoList.value.isNotEmpty()) {
            // Şu anki videonun URI'sini alır
            val videoUri = Uri.parse(videoList.value[currentIndex].uri)

            // Mevcut videoyu durdurur
            exoPlayer.stop()

            // Önceki video öğelerini temizler
            exoPlayer.clearMediaItems()

            // Yeni videoyu ExoPlayer'a yükler
            exoPlayer.setMediaItem(MediaItem.fromUri(videoUri))

            // ExoPlayer'ı hazırlar (buffering başlar)
            exoPlayer.prepare()

            // Videoyu otomatik olarak başlatır
            exoPlayer.playWhenReady = true
        }
    }

    // Şu anki videonun açıklamasını alır
    // currentIndex veya videoList değiştiğinde yeniden hesaplanır
    val currentDescription = remember(currentIndex, videoList.value) {
        // Güvenli bir şekilde video açıklamasını alır, yoksa boş string döndürür
        videoList.value.getOrNull(currentIndex)?.description ?: ""
    }

    // Material3 Scaffold bileşeni - temel sayfa yapısını sağlar
    Scaffold(
        topBar = {
            // Üst çubuk - başlık ve geri düğmesi içerir
            TopAppBar(
                title = { Text("Bacak Egzersizleri") },
                navigationIcon = {
                    // Geri düğmesi - tıklanınca navigateBack fonksiyonunu çağırır
                    IconButton(onClick = { navigateBack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri Dön"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        // Ana içerik kolonu - dikey düzende bileşenleri gösterir
        Column(
            modifier = Modifier
                .fillMaxSize() // Tüm ekranı kaplar
                .padding(paddingValues), // Scaffold padding'ini uygular
            horizontalAlignment = Alignment.CenterHorizontally, // İçeriği yatayda ortalar
            verticalArrangement = Arrangement.Center // İçeriği dikeyde ortalar
        ) {
            if (videoList.value.isNotEmpty()) {
                // Video sayısı gösterimi - hangi videoda olduğumuzu gösterir
                Text(
                    text = "Video ${currentIndex + 1} / ${videoList.value.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Video oynatıcı kartı - video içeriğini çevreler
                Card(
                    modifier = Modifier
                        .fillMaxWidth() // Genişliği ekran genişliğine eşit
                        .height(300.dp) // Sabit yükseklik
                        .padding(10.dp) // İç boşluk
                ) {
                    // Eğer showPlayer true ise video oynatıcıyı göster
                    if (showPlayer) {
                        // AndroidView - Android native view'larını Compose'da kullanmak için
                        AndroidView(
                            factory = { ctx ->
                                // PlayerView oluştur - ExoPlayer'ı göstermek için
                                PlayerView(ctx).apply {
                                    player = exoPlayer // ExoPlayer'ı bağla
                                    useController = true // Video kontrol arayüzünü göster
                                    controllerShowTimeoutMs = 1000 // Kontroller ne kadar süre görünür kalacak
                                    playerView = this // PlayerView referansını güncelle
                                }
                            },
                            modifier = Modifier.fillMaxSize() // Kart içinde tüm alanı kapla
                        )
                    }
                }

                // Video açıklaması için boşluk
                Spacer(modifier = Modifier.height(10.dp))

                // Video açıklaması metni
                Text(
                    text = currentDescription, // Şu anki videonun açıklaması
                    fontSize = 18.sp, // Metin boyutu
                    textAlign = TextAlign.Center // Metni ortala
                )

                // Kontrol butonları için boşluk
                Spacer(modifier = Modifier.height(20.dp))

                // Video navigasyon kontrolleri satırı - ileri/geri butonları
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly // Butonları eşit aralıklarla yerleştir
                ) {
                    // Önceki video butonu - birden fazla video varsa gösterilir
                    if (videoList.value.size > 1) {
                        IconButton(
                            onClick = {
                                // Önceki videoya geç, ilk videodaysa sonuncuya dön (dairesel)
                                currentIndex = if (currentIndex > 0)
                                    currentIndex - 1
                                else
                                    videoList.value.size - 1
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Previous Video" // Ekran okuyucular için açıklama
                            )
                        }
                    }

                    // Sonraki video butonu
                    IconButton(
                        onClick = {
                            // Sonraki videoya geç, son videodaysa ilk videoya dön (dairesel)
                            currentIndex = (currentIndex + 1) % videoList.value.size
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Next Video" // Ekran okuyucular için açıklama
                        )
                    }
                }
            } else {
                // Video listesi boşsa veya yüklenmediyse loading göstergesi
                CircularProgressIndicator() // Dönen yükleniyor animasyonu
                Text(
                    "Videolar yükleniyor...", // Yükleme durumu metni
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}