package com.example.fizyoapp.presentation.advertisement.banner

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.advertisement.Advertisement
import com.example.fizyoapp.domain.usecase.advertisement.GetActiveAdvertisementsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdvertisementBannerViewModel @Inject constructor(
    private val getActiveAdvertisementsUseCase: GetActiveAdvertisementsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AdvertisementBannerState())
    val state: StateFlow<AdvertisementBannerState> = _state.asStateFlow()

    private var refreshJob: Job? = null
    private var autoScrollJob: Job? = null

    init {
        loadActiveAdvertisements()
        startAutoRefresh()
    }

    fun loadActiveAdvertisements() {
        viewModelScope.launch {
            Log.d("AdvertisementBanner", "Reklamlar yükleniyor...")

            getActiveAdvertisementsUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val advertisements = result.data
                        Log.d("AdvertisementBanner", "Reklamlar başarıyla yüklendi: ${advertisements.size}")

                        if (advertisements.isNotEmpty()) {
                            // Tüm reklamları kaydediyoruz
                            val adList = advertisements.take(10) // En fazla 10 reklam gösteriyoruz
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    advertisements = adList,
                                    currentIndex = 0,
                                    error = null
                                )
                            }
                            startAutoScroll() // Otomatik kaydırmayı başlat
                        } else {
                            Log.d("AdvertisementBanner", "Aktif reklam bulunamadı")
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    advertisements = emptyList()
                                )
                            }
                            stopAutoScroll()
                        }
                    }
                    is Resource.Error -> {
                        Log.e("AdvertisementBanner", "Reklam yükleme hatası: ${result.message}")
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    private fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (true) {
                delay(60 * 1000L) // 1 dakikada bir reklamları yenile
                Log.d("AdvertisementBanner", "Reklamlar yenileniyor...")
                loadActiveAdvertisements()
            }
        }
    }

    private fun startAutoScroll() {
        autoScrollJob?.cancel()
        autoScrollJob = viewModelScope.launch {
            while (true) {
                delay(3000L) // 3 saniyede bir sonraki reklama geç
                val currentAds = _state.value.advertisements
                if (currentAds.isNotEmpty()) {
                    val currentIndex = _state.value.currentIndex
                    val nextIndex = (currentIndex + 1) % currentAds.size

                    Log.d("AdvertisementBanner", "Otomatik kaydırma: $currentIndex -> $nextIndex")

                    _state.update {
                        it.copy(currentIndex = nextIndex)
                    }
                }
            }
        }
    }

    fun stopAutoScroll() {
        autoScrollJob?.cancel()
        autoScrollJob = null
    }

    // Kullanıcı manuel olarak bir reklama geçiş yaparsa
    fun moveToNextAd() {
        val currentAds = _state.value.advertisements
        if (currentAds.isNotEmpty()) {
            val currentIndex = _state.value.currentIndex
            val nextIndex = (currentIndex + 1) % currentAds.size
            _state.update {
                it.copy(currentIndex = nextIndex)
            }
        }
    }

    fun moveToPreviousAd() {
        val currentAds = _state.value.advertisements
        if (currentAds.isNotEmpty()) {
            val currentIndex = _state.value.currentIndex
            val previousIndex = if (currentIndex > 0) currentIndex - 1 else currentAds.size - 1
            _state.update {
                it.copy(currentIndex = previousIndex)
            }
        }
    }

    // Kullanıcı belirli bir reklama geçiş yaparsa
    fun moveToAd(index: Int) {
        val currentAds = _state.value.advertisements
        if (currentAds.isNotEmpty() && index in currentAds.indices) {
            _state.update {
                it.copy(currentIndex = index)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
        autoScrollJob?.cancel()
        Log.d("AdvertisementBanner", "ViewModel temizlendi, tüm job'lar iptal edildi")
    }
}

data class AdvertisementBannerState(
    val isLoading: Boolean = true,
    val advertisements: List<Advertisement> = emptyList(),
    val currentIndex: Int = 0,
    val error: String? = null
) {
    val currentAdvertisement: Advertisement?
        get() = advertisements.getOrNull(currentIndex)
}