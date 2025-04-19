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
class CoreExercisesOfExamplesViewModel@Inject constructor(
    private val repository: ExamplesOfExerciseRepository,
    @ApplicationContext private val context: Context
): ViewModel() {
    private val _videoList = MutableStateFlow<List<ExamplesOfExercisesEntity>>(emptyList())
    val videoList: StateFlow<List<ExamplesOfExercisesEntity>> = _videoList

    private val CATEGORY = "core"
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