package com.example.fizyoapp.presentation.advertisement.banner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
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
            getActiveAdvertisementsUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val advertisements = result.data
                        if (advertisements.isNotEmpty()) {
                            val adList = advertisements.take(10)
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    advertisements = adList,
                                    currentIndex = 0,
                                    error = null
                                )
                            }
                            startAutoScroll()
                        } else {
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
                // 30 saniyede bir reklamları kontrol edelim (süre dolmuş olabilir)
                delay(30 * 1000L)
                loadActiveAdvertisements()
            }
        }
    }

    private fun startAutoScroll() {
        autoScrollJob?.cancel()
        autoScrollJob = viewModelScope.launch {
            while (true) {
                delay(3000L)
                val currentAds = _state.value.advertisements
                if (currentAds.isNotEmpty()) {
                    val currentIndex = _state.value.currentIndex
                    val nextIndex = (currentIndex + 1) % currentAds.size
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
    }
}