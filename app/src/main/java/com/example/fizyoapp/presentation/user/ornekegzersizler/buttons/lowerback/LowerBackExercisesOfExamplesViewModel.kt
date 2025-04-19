package com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.data.viewmodel


import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.data.model.ExamplesOfExercisesEntity
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.data.repository.ExamplesOfExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LowerBackExercisesOfExamplesViewModel @Inject constructor(
    private val repository: ExamplesOfExerciseRepository,
    @ApplicationContext private val context: Context
): ViewModel() {
    private val _videoList = MutableStateFlow<List<ExamplesOfExercisesEntity>>(emptyList())
    val videoList: StateFlow<List<ExamplesOfExercisesEntity>> = _videoList

    private val CATEGORY="lowerback"
    // Uygulama başladığında videoları yükle
    init {
        loadVideos()
    }

    fun loadVideos() {
        viewModelScope.launch {
            try {
                // Veritabanını temizle - artık suspend func olarak çağrılabilir
                repository.deleteVideosByCategory(CATEGORY)

                // Video listesi
                val videoResources = listOf(
                    VideoResource("spinalrotation", "Sırt üstü yat, dizlerini bük. Ağrın varsa bacaklarını düz şekilde uzatıp duvara yaslayabilirsin.\n" +
                            "\n" +
                            "Kollarını yanlara aç, avuç içlerin yere bakmalı.\n" +
                            "\n" +
                            "Dizlerini yavaşça sağa doğru indir, omurganın dönmesini sağla.\n" +
                            "\n" +
                            "\uD83D\uDD52 5 saniye bekle, sonra dizlerini tekrar merkeze getir.\n" +
                            "\n" +
                            "Aynı hareketi sola doğru yap.\n" +
                            "\n" +
                            "\uD83D\uDD01 Her iki yönde 10 tekrar yap.\n" +
                            "\n"),
                    VideoResource("spinalflexion", "Ayakta dur, kollarını aşağı uzatarak yavaşça belini hissederek aşağı eğilebildiğin kadar eğil.\n" +
                            "\n" +
                    " Tekrardan yavaş yavaş başlangıç pozisyonuna gel.\n" +
                            "\uD83D\uDD01 Her iki yönde 10 tekrar yap.\n"),
                    VideoResource("spinalextansion", "Ayakta dur, ellerini belinden tut ve vücudunu arkaya doğru esnet. \n" +
                            "\n" +
                    "Tekrardan yavaş yavaş başlangıç pozisyonuna gel.\n"+
                            "\uD83D\uDD01 Her iki yönde 10 tekrar yap.\n"),
                    VideoResource("bridge", "Sırt üstü yat, dizlerini bük ve ayaklarını yere bas.\n" +
                            "\n" +
                            "Ellerini yanlara yerleştir, kollar düz olmalı.\n" +
                            "\n" +
                            "Kalçanı yukarı doğru kaldır, vücudun düz bir çizgi oluşturmalı.\n" +
                            "\n" +
                            "\uD83D\uDD52 5 saniye bekle, sonra kalçanı yavaşça yere indir.\n" +
                            "\n" +
                            "\uD83D\uDD01 10 tekrar yap."),
                    VideoResource("kneestochest", "Sırt üstü yat, dizlerini karnına doğru çek.\n" +
                            "\n" +
                            "Ellerini dizlerinin etrafına sar.\n" +
                            "\n" +
                            "Dizlerini göğsüne doğru çekerek boynunu da hafifçe eğ.\n" +
                            "\n" +
                            "\uD83D\uDD52 5 saniye bekle, sonra yavaşça bırak.\n" +
                            "\n" +
                            "\uD83D\uDD01 10 tekrar yap."),
                    VideoResource("singlelegtochest", "Sırt üstü yat, bir bacağınızı düz tutun.\n" +
                            "\n" +
                            "Diğer bacağınızı dizden bükerek göğsünüze doğru çek.\n" +
                            "\n" +
                            "\uD83D\uDD52 Ellerinizle dizinizi kucaklayın ve 5 saniye bekleyin.\n" +
                            "\n" +
                            "Yavaşça bacağınızı indirip diğer bacakla tekrarlayın.\n" +
                            "\n" +
                            "\uD83D\uDD01 10 tekrar yap."),
                    VideoResource("lyinghamstringstretch", "Sırt üstü yat, bir bacağınızı düz tutun.\n" +
                            "\n" +
                            "Diğer bacağınızı diziniz kırık şekilde kendinize çekin, ellerinizle arka kısmını tutun ve kırık dizinizi açın.\n" +
                            "\n" +
                            "\uD83D\uDD52 10 saniye bekleyin, sonra bacağınızı indirin.\n" +
                            "\n" +
                            "Diğer bacakla aynı hareketi yapın.\n" +
                            "\n" +
                            "\uD83D\uDD01 10 tekrar yap.\n" +
                            "\n"),
                    VideoResource("catcow", "Dört ayak pozisyonuna geç (eller-dizler yerde).\n" +
                            "\n" +
                            "Nefes verirken sırtını yukarı doğru kamburlaştır (kedi pozisyonu).\n" +
                            "\n" +
                            "Nefes alırken sırtını çukurlaştır, başını yukarı kaldır (deve pozisyonu).\n" +
                            "\n" +
                            "Hareketi yavaşça ve kontrollü yap.\n" +
                            "\n" +
                            "\uD83D\uDD01 10 tekrar yap.\n" +
                            "\n"),
                    VideoResource("childpose", "Diz çökün, kalçalarınızı topuklara yaklaştırın. Kollarınızı öne uzatın ve alnınızı yere koyun. Derin nefes alarak rahatlayın.\n" +
                            "\uD83D\uDD52 30 saniye boyunca pozisyonda kalın.")
                    )

                // Videoları hazırla
                val videoEntities = videoResources.mapNotNull { videoResource ->
                    val resourceId = context.resources.getIdentifier(
                        videoResource.name, "raw", context.packageName
                    )

                    if (resourceId != 0) {
                        val uri = "android.resource://${context.packageName}/$resourceId"
                        ExamplesOfExercisesEntity(
                            uri = uri,
                            description = videoResource.description,
                            category = CATEGORY
                        )
                    } else {
                        null
                    }
                }

                // Toplu olarak videoları ekle
                if (videoEntities.isNotEmpty()) {
                    repository.insertVideos(videoEntities)
                }
                repository.getVideosByCategory(CATEGORY).collect { videos ->
                    _videoList.value = videos
                }


            } catch (e: Exception) {
                Log.e("VideoViewModel", "Error loading videos: ${e.message}")
            }
        }


    }

    private data class VideoResource(
        val name: String,
        val description: String
    )
}