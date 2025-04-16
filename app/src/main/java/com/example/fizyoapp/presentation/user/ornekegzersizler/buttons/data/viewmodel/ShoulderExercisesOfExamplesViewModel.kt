package com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.data

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
class ExercisesOfExamplesViewModel @Inject constructor(
    private val repository: ExamplesOfExerciseRepository,
    @ApplicationContext private val context: Context
): ViewModel() {
    private val _videoList = MutableStateFlow<List<ExamplesOfExercisesEntity>>(emptyList())
    val videoList: StateFlow<List<ExamplesOfExercisesEntity>> = _videoList

    // Uygulama başladığında videoları yükle
    init {
        loadVideos()
    }

    fun loadVideos() {
        viewModelScope.launch {
            try {
                // Veritabanını temizle - artık suspend func olarak çağrılabilir
                repository.deleteAllVideos()

                // Video listesi
                val videoResources = listOf(
                    VideoResource("shouldercicle", "Omuz Egzersizi 1"),
                    VideoResource("shoulderflexion", "Omuz Egzersizi 2"),
                    VideoResource("shoulderabduction", "Sırt Egzersizi 3"),
                    VideoResource("shoulderexternalrotation", "Omuz Egzersizi 4"),
                    VideoResource("ninetydegreeer", "Omuz Egzersizi 5"),
                    VideoResource("ninetydegreeir", "Sırt Egzersizi 6"),
                    VideoResource("shoulderpress", "Omuz Egzersizi 7"),
                    VideoResource("dynamicstretch", "Omuz Egzersizi 8"),
                    VideoResource("cheststretch", "Sırt Egzersizi 9"),
                    VideoResource("childpose", "Sırt Egzersizi 10"),

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
                            description = videoResource.description
                        )
                    } else {
                        null
                    }
                }

                // Toplu olarak videoları ekle
                if (videoEntities.isNotEmpty()) {
                    repository.insertVideos(videoEntities)
                }

            } catch (e: Exception) {
                Log.e("VideoViewModel", "Error loading videos: ${e.message}")
            }
        }

        // Videoları dinle ve state'i güncelle
        viewModelScope.launch {
            repository.getAllVideo().collect { videos ->
                _videoList.value = videos
            }
        }
    }

    private data class VideoResource(
        val name: String,
        val description: String
    )
}