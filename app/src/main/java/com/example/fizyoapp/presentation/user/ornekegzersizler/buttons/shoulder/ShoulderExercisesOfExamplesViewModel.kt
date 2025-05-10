package com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.shoulder
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.local.entity.exercisevideos.ExamplesOfExercisesEntity
import com.example.fizyoapp.data.repository.exercisevideos.ExamplesOfExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoulderExercisesOfExamplesViewModel @Inject constructor(
    private val repository: ExamplesOfExerciseRepository,
    @ApplicationContext private val context: Context
): ViewModel() {
    private val _videoList = MutableStateFlow<List<ExamplesOfExercisesEntity>>(emptyList())
    val videoList: StateFlow<List<ExamplesOfExercisesEntity>> = _videoList
    private val CATEGORY = "shoulder"

    init {
        loadVideos()
    }

    fun loadVideos() {
        viewModelScope.launch {
            try {
                repository.deleteVideosByCategory(CATEGORY)
                val videoResources = listOf(
                    VideoResource("shouldercicle", "Ayakta durun yada dik bir şekilde oturun. Kollarını iki yana aç, yere paralel şekilde(Ağrınız olursa kollarınızı yana açmayın videodaki gibi konumlandırın.)\n" +
                            "\n" +
                            "Kollarınla-omuzunla yavaşça küçük daireler çizmeye başla.\n" +
                            "\n" +
                            "\uD83D\uDD52 10 saniye ileri doğru, ardından \uD83D\uDD52  10 saniye geri doğru çevir.\n" +
                            "Tekrar: 3 set"),
                    VideoResource("shoulderflexion", "Ayakta durun. Kollarınızı düz şekilde öne doğru omuz hizasına kadar kaldırın ve yavaşça indirin. Avuç içleriniz yere bakmalı.\n" +
                            "\uD83D\uDD01 12 tekrar."),
                    VideoResource("shoulderabduction", "Ayakta, kollarınızı iki yana doğru açın, omuz hizasında durun ve tekrar indirin.\n" +
                            "\uD83D\uDD01 10 tekrar."),
                    VideoResource("shoulderexternalrotation", "Dirseklerinizi 90 derece bükerek yanlarınızda tutun. Avuç içleri yukarı baksın. Dirsek sabit kalacak şekilde ellerinizi dışa doğru açın.\n" +
                            "\uD83D\uDD01 10 tekrar."),
                    VideoResource("ninetydegreeer", "Kollarınızı omuz hizasında yana açın, dirsekler 90 derece bükülü. Ön kollar yere paralel olacak. Elinizi yukarı kaldırarak omzu dışa döndürün.\n" +
                            "\uD83D\uDD01 10 tekrar."),
                    VideoResource("ninetydegreeir", "Bir önceki pozisyonda eller bu kez baş hizasından aşağıya doğru döndürülür. Sanki tavana bakan eliniz yere döner gibi.\n" +
                            "\uD83D\uDD01 10 tekrar."),
                    VideoResource("shoulderpress", "Kollarınızı omuz hizasında iki yanda, dirsekler 90 derece bükülü şekilde tutun. Avuç içleriniz yukarı baksın. Kollarınızı yukarı doğru itin, sonra yavaşça indirin.\n Oturur pozisyonda veya ayakta yapabilirsiniz.Dilerseniz ağırlık kullanabilirsiniz." +
                            "\uD83D\uDD01 8 tekrar."),
                    VideoResource("dynamicstretch", "Ayakta durun.Ellerinizi kalça hizzasonda birleştirin ve kaldırabildiğiniz kadar yukarı kaldırın\n" +
                            "\uD83D\uDD01 12 tekrar "),
                    VideoResource("cheststretch", "Ayakta dik dur.\n" +
                            "\n" +
                            "Her iki kolunu dirseklerinden 90 derece bükerek yukarı kaldır.\n" +
                            "\n" +
                            "Kollarını duvarın iki yanına ya da bir kapı çerçevesine yerleştir.Avuç içlerin öne bakmalı\n" +
                            "\n" +
                            "Gövdeni yavaşça öne doğru it.\n" +
                            "\n" +
                            "\uD83D\uDD52 15–20 saniye boyunca bu pozisyonda kal.\uD83D\uDD01 2-3 set yapabilirsin. "),
                    VideoResource("childpose", "Diz çökün, kalçalarınızı topuklara yaklaştırın. Kollarınızı öne uzatın ve alnınızı yere koyun. Derin nefes alarak rahatlayın.\n" +
                            "\uD83D\uDD52 30 saniye boyunca pozisyonda kalın."),
                )

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

                if (videoEntities.isNotEmpty()) {
                    repository.insertVideos(videoEntities)
                }

                repository.getVideosByCategory(CATEGORY).collect { videos ->
                    _videoList.value = videos
                }
            } catch (e: Exception) {
                // Hata durumunda işlem yapılmıyor
            }
        }
    }

    private data class VideoResource(
        val name: String,
        val description: String
    )
}