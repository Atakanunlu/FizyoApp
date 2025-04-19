package com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.hip

import android.content.Context
import android.util.Log
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

@HiltViewModel
class HipExercisesOfExamplesViewModel @Inject constructor(
    private val repository: ExamplesOfExerciseRepository,
    @ApplicationContext private val context: Context
): ViewModel() {
    private val _videoList = MutableStateFlow<List<ExamplesOfExercisesEntity>>(emptyList())
    val videoList: StateFlow<List<ExamplesOfExercisesEntity>> = _videoList

    private val CATEGORY="hip"
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
                    VideoResource("hipmobility", "Ayakta ya da yere oturarak, dizlerinizi bükün ve bacaklarınızı birbirine yakın tutun.\n" +
                            "\n" +
                            "Kalçanızı dairesel hareketlerle döndürerek hareketliliği artırın.\n" +
                            "\n" +
                            "\uD83D\uDD01 Her iki yönde 10 tekrarla yapın.\n" +
                            "\n"),
                    VideoResource("leftlegraise", "Sırt üstü yatın veya ayakta durun, bacaklarınızı düz tutun.\n" +
                            "\n" +
                            "Sol bacağınızı yavaşça yukarı kaldırın, paralel tutun.\n" +
                            "\n" +
                            "\uD83D\uDD52 5 saniye tutun, sonra bacağınızı yavaşça indirin.\n" +
                            "\n" +
                            "\uD83D\uDD01 10 tekrar yapın.\n" +
                            "\n"),
                    VideoResource("rightlegraise", "Sırt üstü yatın veya ayakta durun, bacaklarınızı düz tutun.\n" +
                            "\n" +
                            "Sağ bacağınızı yavaşça yukarı kaldırın, paralel tutun.\n" +
                            "\n" +
                            "\uD83D\uDD52 5 saniye tutun, sonra bacağınızı yavaşça indirin.\n" +
                            "\n" +
                            "\uD83D\uDD01 10 tekrar yapın.\n" +
                            "\n"),
                    VideoResource("hipflexion", "Ayakta dur, bir bacağını yukarı doğru kaldırarak dizini bük.\n" +
                            "\n" +
                            "Dizini göğsüne doğru çek.Dilersen direnç bandı ile de yapabilirsin.\n" +
                            "\n" +
                            "\uD83D\uDD52 5 saniye tut, sonra bacağını indir.\n" +
                            "\n" +
                            "Diğer bacakla tekrarlayın.\n" +
                            "\n" +
                            "\uD83D\uDD01 10 tekrar yap.\n" +
                            "\n"),
                    VideoResource("hipextansion", "Ayakta durun, bacaklarınızı omuz genişliğinde açın.\n" +
                            "\n" +
                            "Bir bacağınızı geriye doğru kaldırın.\n" +
                            "\n" +
                            "\uD83D\uDD52 5 saniye tutun, sonra bacağınızı indirin.\n" +
                            "\n" +
                            "Diğer bacakla tekrarlayın.\n" +
                            "\n" +
                            "\uD83D\uDD01 10 tekrar yapın.\n" +
                            "\n"),
                    VideoResource("bridge", "Sırt üstü yatın, dizlerinizi bükün, ayaklarınızı yere basın.\n" +
                            "\n" +
                            "Kalçanızı yukarı kaldırarak köprü pozisyonuna gelin.\n" +
                            "\n" +
                            "\uD83D\uDD52 5 saniye tutun, sonra kalçanızı yavaşça yere indirin.\n" +
                            "\n" +
                            "\uD83D\uDD01 10 tekrar yapın.\n" +
                            "\n"),
                    VideoResource("legcurl", "Yüzüstü yatın, bacaklarınız düz olsun.\n" +
                            "\n" +
                            "Bir bacağınızı dizinden bükerek topuğunuzu kalçanıza doğru çekin.\n" +
                            "\n" +
                            "\uD83D\uDD52 5 saniye tutun, sonra bacağınızı indirin.\n" +
                            "\n" +
                            "Diğer bacakla aynı hareketi yapın.\n" +
                            "\n" +
                            "\uD83D\uDD01 10 tekrar yapın.\n" +
                            "\n"),
                    VideoResource("wallsquad", "Ayaklarınızı omuz genişliğinde açarak bir duvara yaslanın.\n" +
                            "\n" +
                            "Dizlerinizi bükerek aşağıya doğru inin.\n" +
                            "\n" +
                            "\uD83D\uDD52 5 saniye bekleyin, sonra yukarı çıkın.\n" +
                            "\n" +
                            "\uD83D\uDD01 10 tekrar yapın.\n" +
                            "\n"),
                    VideoResource("hipstretch", "Yere oturun veya ayakta durun, bir bacağınızı düz tutun, diğerini dizinizden bükerek göğsünüze doğru çekin.\n" +
                            "\n" +
                            "\uD83D\uDD52 Kalçalarınızı esneterek 15 saniye tutun.\n" +
                            "\n" +
                            "Diğer bacakla aynı hareketi yapın.\n" +
                            "\n" +
                            "\uD83D\uDD01 2 tekrar yapın."),
                    VideoResource("quadricepsstretch", "Ayakta durun, bir bacağınızı dizinden bükerek topuğunu kalçanıza doğru çekin.\n" +
                            "\n" +
                            "\uD83D\uDD52 Ellerinizle ayak bileğinizi tutarak 15 saniye esnetin.\n" +
                            "\n" +
                            "Diğer bacakla aynı hareketi yapın.\n" +
                            "\n" +
                            "\uD83D\uDD01 2 tekrar yapın.\n" +
                            "\n"),

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

}