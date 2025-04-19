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
class LegExercisesOfExamplesViewModel @Inject constructor(
    private val repository: ExamplesOfExerciseRepository,
    @ApplicationContext private val context: Context
): ViewModel() {
    private val _videoList = MutableStateFlow<List<ExamplesOfExercisesEntity>>(emptyList())
    val videoList: StateFlow<List<ExamplesOfExercisesEntity>> = _videoList

    private val CATEGORY="legs"
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
                    VideoResource("calfraises", "Ayakta dur, bacakların omuz genişliğinde açık.\n" +
                            "\n" +
                            "Yavaşça topuklarını kaldırarak parmak uçlarında yüksel.\n" +
                            "\n" +
                            "\uD83D\uDD52 2 saniye tut, sonra topuklarını yavaşça yere indir.\n" +
                            "\n" +
                            "\uD83D\uDD01 15 tekrar yap.\n" +
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
                    VideoResource("steuup", "Bir basamağa ya da sağlam bir kutuya adım at.\n" +
                            "\n" +
                            "Diğer bacağınla da basamağa adım at.\n" +
                            "\n" +
                            "\uD83D\uDD52 5 saniye bekle, sonra geri in ve diğer bacakla tekrarla.\n" +
                            "\n" +
                            "\uD83D\uDD01 10 tekrar yap."),
                    VideoResource("legabduction", "Ayakta dur, bir bacağını yana doğru kaldır.\n" +
                            "\n" +
                            "Bacak düz olmalı, hareketi yavaşça yap.\n" +
                            "\n" +
                            "\uD83D\uDD52 5 saniye tut, sonra bacağını indir.\n" +
                            "\n" +
                            "Diğer bacakla aynı hareketi yap.\n" +
                            "\n" +
                            "\uD83D\uDD01 10 tekrar yap."),
                    VideoResource("legraises", "Sırt üstü yat, bacaklarını düz tut.\n" +
                            "\n" +
                            "Bir bacağını yukarı doğru kaldır.\n" +
                            "\n" +
                            "Bacağın tamamen düz olmalı.\n" +
                            "\n" +
                            "\uD83D\uDD52 5 saniye bekle, sonra bacağını indir.\n" +
                            "\n" +
                            "Diğer bacakla aynı hareketi yap.\n" +
                            "\n" +
                            "\uD83D\uDD01 10 tekrar yap."),
                    VideoResource("hipadd", "Ayakta dur, bir bacağını yan tarafa doğru aç.\n" +
                            "\n" +
                            "Diğer bacağına doğru çek, hareketi yavaşça yap.\n" +
                            "\n" +
                            "\uD83D\uDD52 5 saniye tut, sonra bacağını geri getir.\n" +
                            "\n" +
                            "Diğer bacakla aynı hareketi yap.\n" +
                            "\n" +
                            "\uD83D\uDD01 10 tekrar yap."),
                    VideoResource("legcurl", "Yüzüstü yat, bacaklarınızı düz tutun.\n" +
                            "\n" +
                            "Bir bacağınızı dizinizden bükerek topuğunuzu kalçanıza doğru çekin.\n" +
                            "\n" +
                            "\uD83D\uDD52 5 saniye tut, sonra bacağını indirin.\n" +
                            "\n" +
                            "Diğer bacakla aynı hareketi yap.\n" +
                            "\n" +
                            "\uD83D\uDD01 10 tekrar yap.\n" +
                            "\n"),
                    VideoResource("kick", "Emekleme pozisyonuna gelin, bir diziniz kırık şekilde yukarı doğru tekme at.\n" +
                            "\n" +
                            "\uD83D\uDD52 5 saniye tut, sonra bacağını indir.\n" +
                            "\n" +
                            "Diğer bacakla aynı hareketi yap.\n" +
                            "\n" +
                            "\uD83D\uDD01 10 tekrar yap."),
                    VideoResource("clamshells", "Yan yat, dizlerinizi bükerek ayaklarınızı birleştirin.\n" +
                            "\n" +
                            "Üst dizinizi yukarıya doğru kaldırın, hareketi kontrollü yapın.\n" +
                            "\n" +
                            "\uD83D\uDD52 5 saniye tut, sonra dizinizi indirin.\n" +
                            "\n" +
                            "Diğer tarafla aynı hareketi yap.\n" +
                            "\n" +
                            "\uD83D\uDD01 10 tekrar yap.\n" +
                            "\n"),
                    VideoResource("bridge", "Sırt üstü yat, dizlerinizi bükerek ayaklarınızı yere basın.\n" +
                            "\n" +
                            "Kalçanızı yukarıya doğru kaldırarak vücudunuzu düz tutun.\n" +
                            "\n" +
                            "\uD83D\uDD52 5 saniye bekle, sonra kalçanızı yavaşça yere indirin.\n" +
                            "\n" +
                            "\uD83D\uDD01 10 tekrar yap.\n" +
                            "\n"),
                    VideoResource("wallsquad", "Ayaklarınızı omuz genişliğinde açarak duvara yaslanın.\n" +
                            "\n" +
                            "Dizlerinizi bükerek sırtınızı duvara yaslayarak aşağıya doğru inin.\n" +
                            "\n" +
                            "\uD83D\uDD52 5 saniye bu pozisyonda kalın, sonra yukarı doğru çıkın.\n" +
                            "\n" +
                            "\uD83D\uDD01 10 tekrar yap."),
                    VideoResource("quadstretch", "Ayakta dur, bir bacağını dizinden bükerek topuğunu kalçana doğru çek.\n" +
                            "\n" +
                            "Ellerinizle ayak bileğinizi tutarak gerilme hissedene kadar esnetin.\n" +
                            "\n" +
                            "\uD83D\uDD52 15 saniye bekleyin, sonra bacağınızı indirin.\n" +
                            "\n" +
                            "Diğer bacakla aynı hareketi yap.\n" +
                            "\n" +
                            "\uD83D\uDD01 2 tekrar yap.\n" +
                            "\n"),
                    VideoResource("hamstringstretch", "Oturun , bacaklarınızı düz uzatın.\n" +
                            "\n" +
                            "Ellerinizle ayak parmaklarınıza doğru uzanarak bacak arkasındaki kasları esnetin.\n" +
                            "\n" +
                            "\uD83D\uDD52 15 saniye bekleyin, sonra bacağı indirin.\n" +
                            "\n" +
                            "Diğer bacakla aynı hareketi yap.\n" +
                            "\n" +
                            "\uD83D\uDD01 2 tekrar yap.\n" +
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