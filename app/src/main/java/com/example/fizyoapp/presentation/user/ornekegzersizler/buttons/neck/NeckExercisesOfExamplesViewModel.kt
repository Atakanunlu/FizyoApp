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
class NeckExercisesOfExamplesViewModel @Inject constructor(
    private val repository: ExamplesOfExerciseRepository,
    @ApplicationContext private val context: Context
): ViewModel() {
    private val _videoList = MutableStateFlow<List<ExamplesOfExercisesEntity>>(emptyList())
    val videoList: StateFlow<List<ExamplesOfExercisesEntity>> = _videoList

    private val CATEGORY = "neck"
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
                    VideoResource("neckrotation", "Dik bir şekilde otur ya da ayakta dur.\n" +
                            "\n" +
                            "Başını yavaşça sağa çevir, çenen omzuna yaklaşsın.\n" +
                            "\n" +
                            "\uD83D\uDD52 Son noktada 2-3 saniye bekle.\n" +
                            "\n" +
                            "Yavaşça başını ortaya getir.\n" +
                            "\n" +
                            "Aynı hareketi sola doğru yap.\n" +
                            "\n" +
                            "\uD83D\uDD01 Sağ ve sol yönlerde toplam 10 tekrar yap"),
                    VideoResource("neckflexion", "Başını yavaşça öne doğru eğ.\n" +
                            "\n" +
                            "Çenen, göğsüne yaklaşmalı.\n" +
                            "\n" +
                            "Boynunun arka kısmında esneme hissetmelisin.\n" +
                            "\n" +
                            "\uD83D\uDD52 5 saniye bekle, sonra yavaşça başını dik konuma getir.\n" +
                            "\n" +
                            "\uD83D\uDD01 10 tekrar yap."),
                    VideoResource("neckextension", "Başını geriye doğru yavaşça eğ.\n" +
                            "\n" +
                            "Gözlerin yukarıya bakmalı.\n" +
                            "\n" +
                            "Boynunun ön kısmı gerilmeli.\n" +
                            "\n" +
                            "\uD83D\uDD52 5 saniye bekle.\n" +
                            "\n" +
                            "Başını yavaşça başlangıç konumuna getir.\n" +
                            "\n" +
                            "\uD83D\uDD01 10 tekrar yap."),
                    VideoResource("necklateralflexion", "Başını sağa doğru eğ, sağ kulağın omzuna yaklaşsın.\n" +
                            "\n" +
                            "Omzunu kaldırma.\n" +
                            "\n" +
                            "\uD83D\uDD52 3 saniye bekle, sonra dik konuma dön.\n" +
                            "\n" +
                            "Aynı hareketi sola uygula.\n" +
                            "\n" +
                            "\uD83D\uDD01 Sağ ve sol olmak üzere 10 tekrar yap.\n" +
                            "\n"),
                    VideoResource("necktrapeziusstrenghtening", "Omuzlarını yukarı doğru kaldır.\n" +
                            "\n" +
                            "Kulaklarına yaklaşacak kadar kaldır.\n" +
                            "\n" +
                            "\uD83D\uDD52 2 saniye tut, sonra yavaşça bırak.\n" +
                            "\n" +
                            "Hareketi yavaş ve kontrollü yap. Ağrın yoksa ağırlık kullanabilirsin.\n" +
                            "\n" +
                            "\uD83D\uDD01 10 tekrar yap.\n" +
                            "\n"),
                    VideoResource("neckandtrapeziusstretch", "Ayakta ya da sandalyede dik dur.\n" +
                            "\n" +
                            "Sağ kolunu vücudunun arkasına uzat.\n" +
                            "\n" +
                            "Sol elinle sağ elini bileğinden tut.\n" +
                            "\n" +
                            "Sol kulağını omzuna değdirmeye çalış.Boynun sağ tarafında ve trapez kaslarında bir esneme hisset.\n" +
                            "\n" +
                            "\uD83D\uDD52 Bu pozisyonda 10–15 saniye kal.\n" +
                            "\n" +
                            "Başlangıç pozisyonuna dön.\n" +
                            "\n" +
                            "Aynı hareketi diğer taraf için tekrarla.\n" +
                            "\n" +
                            "\uD83D\uDD01 Her iki taraf için 2 tekrar yap.\n" +
                            "\n"),
                    VideoResource("cobra", "Yüzüstü yere uzan, eller omuz hizasında.\n" +
                            "\n" +
                            "Dirseklerini düzleştirerek üst gövdeni yukarı kaldır.\n" +
                            "\n" +
                            "Kalçan yere temas etmeli.\n" +
                            "\n" +
                            "Göğsünü öne doğru aç, başın hafifçe yukarıda.\n" +
                            "\n" +
                            "\uD83D\uDD52 15 saniye bekle, yavaşça yere dön.\n" +
                            "\n" +
                            "\uD83D\uDD01 3 tekrar yap."),
                    VideoResource("catcamel", "Dört ayak pozisyonuna geç (eller-dizler yerde).\n" +
                            "\n" +
                            "Nefes verirken sırtını yukarı doğru kamburlaştır (kedi pozisyonu).\n" +
                            "\n" +
                            "Nefes alırken sırtını çukurlaştır, başını yukarı kaldır (deve pozisyonu).\n" +
                            "\n" +
                            "Hareketi yavaşça ve kontrollü yap.\n" +
                            "\n" +
                            "\uD83D\uDD01 10 tekrar yap.")
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
                    Log.d("NeckViewModel", "Loaded ${videos.size} neck videos")
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