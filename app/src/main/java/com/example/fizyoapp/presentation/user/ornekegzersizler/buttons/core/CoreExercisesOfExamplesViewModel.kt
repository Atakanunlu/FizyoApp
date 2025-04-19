package com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.core

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.local.entity.exercisevideos.ExamplesOfExercisesEntity
import com.example.fizyoapp.data.repository.exercisevideos.ExamplesOfExerciseRepository
import com.example.fizyoapp.domain.model.exercisevideos.VideoResource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * CoreExercisesOfExamplesViewModel: Core egzersiz videolarını yöneten ViewModel
 *
 * Bu ViewModel:
 * 1. Core egzersiz videolarını veritabanından yükler
 * 2. Raw klasöründeki video kaynaklarını veritabanına ekler
 * 3. UI için gerekli video verilerini StateFlow olarak sunar
 *
 * @param repository: Video verilerine erişim sağlayan repository
 * @param context: Raw kaynaklarına erişim için uygulama context'i
 */
@HiltViewModel
class CoreExercisesOfExamplesViewModel@Inject constructor(
    private val repository: ExamplesOfExerciseRepository,
    @ApplicationContext private val context: Context
): ViewModel() {
    // Video listesi için state holder - değiştirilebilir
    private val _videoList = MutableStateFlow<List<ExamplesOfExercisesEntity>>(emptyList())

    // Dışarıya sunulan salt okunur state
    val videoList: StateFlow<List<ExamplesOfExercisesEntity>> = _videoList

    // Bu ViewModel'in kategori sabiti
    private val CATEGORY = "core"

    // ViewModel oluşturulduğunda videoları yükle
    init {
        loadVideos()
    }

    /**
     * Core egzersiz videolarını yükler
     *
     * Bu fonksiyon:
     * 1. Önce mevcut kategori videolarını temizler
     * 2. Raw klasöründeki videoları veritabanına ekler
     * 3. Veritabanından videoları alarak state'i günceller
     */
    fun loadVideos() {
        viewModelScope.launch {
            try {
                // Veritabanını temizle - artık suspend func olarak çağrılabilir
                repository.deleteVideosByCategory(CATEGORY)

                // Video listesi
                val videoResources = listOf(
                    VideoResource("bridge", "Sırt üstü yatın, dizlerinizi bükün, ayaklarınızı yere basın.\n" +
                            "\n" +
                            "Kalçanızı yukarı kaldırarak köprü pozisyonuna gelin.\n" +
                            "\n" +
                            "\uD83D\uDD52 5 saniye tutun, sonra kalçanızı yavaşça yere indirin.\n" +
                            "\n" +
                            "\uD83D\uDD01 10 tekrar yapın."),
                    VideoResource("bridgemarching", "Köprü pozisyonunda kalın.\n" +
                            "\n" +
                            "Bir bacağınızı yukarı kaldırın, sonra yere indirin.\n" +
                            "\n" +
                            "Diğer bacakla aynı hareketi yapın.\n" +
                            "\n" +
                            "\uD83D\uDD01 10 tekrar yapın."),
                    VideoResource("deadbugjustleg", "Sırt üstü yatın, bacaklarınızı 90 derece bükün.\n" +
                            "\n" +
                            "Bir bacağınızı düz tutarak yere paralel olarak indirin.\n" +
                            "\n" +
                            "\uD83D\uDD52 5 saniye tutun, sonra bacağı yukarı kaldırın.\n" +
                            "\n" +
                            "Diğer bacakla aynı hareketi yapın.\n" +
                            "\n" +
                            "\uD83D\uDD01 10 tekrar yapın."),
                    VideoResource("deadbug", "Sırt üstü yatın, bacaklarınızı 90 derece bükün.\n" +
                            "\n" +
                            "Bir bacağınızı düz tutarak yere paralel olarak indirin.\n" +
                            "\n" +
                            "Diğer kolunuzu yukarı kaldırarak paralel tutun.\n" +
                            "\n" +
                            "\uD83D\uDD52 5 saniye tutun, sonra bacak ve kolu geri getirin.\n" +
                            "\n" +
                            "\uD83D\uDD01 10 tekrar yapın.\n" +
                            "\n"),
                    VideoResource("bicycle", "Sırt üstü yatın, bacaklarınızı havaya kaldırın.\n" +
                            "\n" +
                            "Bir bacağınızı uzatarak bisiklet pedalı çevirme hareketi yapın.\n" +
                            "\n" +
                            "\uD83D\uDD52 5 saniye tutun, sonra bacakları değiştirin.\n" +
                            "\n" +
                            "\uD83D\uDD01 10 tekrar yapın.\n" +
                            "\n"),
                    VideoResource("straightlegraise", "Sırt üstü yatın, bacaklarınızı düz tutun.\n" +
                            "\n" +
                            "Bir bacağınızı yukarı doğru kaldırın.\n" +
                            "\n" +
                            "\uD83D\uDD52 5 saniye tutun, sonra bacağı indirin.\n" +
                            "\n" +
                            "Diğer bacakla aynı hareketi yapın.\n" +
                            "\n" +
                            "\uD83D\uDD01 10 tekrar yapın.\n" +
                            "\n"),
                    VideoResource("plank", "Yere yüzüstü uzanın, dirseklerinizi 90 derece bükün.\n" +
                            "\n" +
                            "Vücudunuzu düz tutarak, ayak parmak uçlarınız ve dirsekleriniz üzerinde durun.\n" +
                            "\n" +
                            "\uD83D\uDD52 durabildiğiniz kadar bu pozisyonda durun.\n" +
                            "\n" +
                            "\uD83D\uDD01 2 tekrar yapın.\n" +
                            "\n"),
                    VideoResource("catcow", "Elleriniz ve dizlerinizin üzerinde durun.\n" +
                            "\n" +
                            "Sırtınızı yukarı doğru kavis yaparak kedi pozisyonuna gelin.\n" +
                            "\n" +
                            "Sonra sırtınızı aşağıya doğru indirerek inek pozisyonuna geçin.\n" +
                            "\n" +
                            "\uD83D\uDD01 10 tekrar yapın.\n" +
                            "\n")
                )

                // Video URI'lerini oluştur ve Entity'lere dönüştür
                //mapNotNull, bir koleksiyonun her elemanını dönüştürür (map) ve dönüşüm sonucu null olan elemanları koleksiyondan çıkarır (filter). Böylece, dönüşüm sonucu null olmayan elemanlardan oluşan yeni bir liste oluşturur.
                //Sizin kodunuzda mapNotNull, video kaynaklarını (videoResources) veritabanı varlıklarına (ExamplesOfExercisesEntity) dönüştürmek için kullanılıyor:
                val videoEntities = videoResources.mapNotNull { videoResource ->
                    // Raw klasöründen video kaynak ID'sini bul
                    val resourceId = context.resources.getIdentifier(
                        videoResource.name, // Örn: "bridge"
                        "raw",              // Klasör adı: raw
                        context.packageName // Uygulama paketi
                    )

                    // Eğer kaynak bulunduysa Entity oluştur
                    if (resourceId != 0) {
                        // Android resource URI formatını kullan
                        val uri = "android.resource://${context.packageName}/$resourceId"

                        // Entity'yi oluştur
                        ExamplesOfExercisesEntity(
                            uri = uri,
                            description = videoResource.description,
                            category = CATEGORY
                        )
                    } else null // Kaynak bulunamazsa null döndür
                }
                // Videoları veritabanına ekle (toplu işlem)
                if (videoEntities.isNotEmpty()) {
                    repository.insertVideos(videoEntities)
                }

                // Veritabanından videoları al ve state'i güncelle
                repository.getVideosByCategory(CATEGORY).collect { videos ->
                    _videoList.value = videos
                }
            } catch (e: Exception) {
                // Hata durumunda boş liste kalır (_videoList ilk değeri)
            }
        }
    }
}