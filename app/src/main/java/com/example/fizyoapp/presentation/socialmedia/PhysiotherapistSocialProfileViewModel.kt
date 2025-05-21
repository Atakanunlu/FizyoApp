// presentation/socialmedia/PhysiotherapistSocialProfileViewModel.kt
package com.example.fizyoapp.presentation.socialmedia

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.auth.User
import com.example.fizyoapp.domain.model.socialmedia.Post
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.physiotherapist_profile.GetPhysiotherapistByIdUseCase
import com.example.fizyoapp.domain.usecase.physiotherapist_profile.GetPhysiotherapistProfileUseCase
import com.example.fizyoapp.domain.usecase.socialmedia.GetAllPostsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhysiotherapistSocialProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUseCase,
    private val getPhysiotherapistProfileUseCase: GetPhysiotherapistProfileUseCase,
    private val getPhysiotherapistByIdUseCase: GetPhysiotherapistByIdUseCase,
    private val getAllPostsUseCase: GetAllPostsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _state = MutableStateFlow(PhysiotherapistSocialProfileState(isLoading = true))
    val state: StateFlow<PhysiotherapistSocialProfileState> = _state.asStateFlow()

    // URL'den fizyoterapist ID'sini al
    private val physiotherapistId: String? = savedStateHandle.get<String>("physiotherapistId")

    init {
        if (physiotherapistId != null && physiotherapistId.isNotEmpty()) {
            // URL'den gelen ID ile profilin yüklenmesi
            loadSpecificPhysiotherapistProfile(physiotherapistId)
        } else {
            // Mevcut giriş yapmış kullanıcının kendi profili
            loadCurrentUserProfile()
        }
    }

    private fun loadCurrentUserProfile() {
        viewModelScope.launch {
            try {
                getCurrentUserUseCase().collect { userResult ->
                    when (userResult) {
                        is Resource.Success -> {
                            val user = userResult.data
                            if (user != null) {
                                // Fizyoterapist profilini yükle
                                loadPhysiotherapistProfileAndPosts(user.id)
                            } else {
                                _state.value = _state.value.copy(
                                    error = "Kullanıcı bulunamadı",
                                    isLoading = false
                                )
                            }
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                error = userResult.message ?: "Kullanıcı bilgisi alınamadı",
                                isLoading = false
                            )
                        }
                        is Resource.Loading -> {
                            // Zaten yükleniyor
                        }
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Profil yüklenirken bir hata oluştu: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun loadSpecificPhysiotherapistProfile(physiotherapistId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            try {
                getPhysiotherapistByIdUseCase(physiotherapistId).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val profile = result.data
                            _state.value = _state.value.copy(
                                profile = profile,
                                isLoading = false
                            )
                            // Fizyoterapistin paylaşımlarını yükle
                            loadPosts(physiotherapistId)
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                error = result.message ?: "Fizyoterapist profili yüklenemedi",
                                isLoading = false
                            )
                        }
                        is Resource.Loading -> {
                            // Zaten yükleniyor
                        }
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Profil yüklenirken bir hata oluştu: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun loadPhysiotherapistProfileAndPosts(userId: String) {
        viewModelScope.launch {
            try {
                getPhysiotherapistProfileUseCase(userId).collect { profileResult ->
                    when (profileResult) {
                        is Resource.Success -> {
                            val profile = profileResult.data
                            _state.value = _state.value.copy(
                                profile = profile,
                                isLoading = false
                            )
                            // Paylaşımları yükle
                            loadPosts(userId)
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                error = profileResult.message ?: "Profil yüklenemedi",
                                isLoading = false
                            )
                        }
                        is Resource.Loading -> {
                            // Zaten yükleniyor
                        }
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Profil yüklenirken bir hata oluştu: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun loadPosts(userId: String) {
        viewModelScope.launch {
            try {
                getAllPostsUseCase().collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            // Belirli fizyoterapistin paylaşımlarını filtrele
                            val physiotherapistPosts = result.data.filter { it.userId == userId }

                            // Toplam beğeni ve yorum sayılarını hesapla
                            val totalLikes = physiotherapistPosts.sumOf { it.likeCount }
                            val totalComments = physiotherapistPosts.sumOf { it.commentCount }

                            _state.value = _state.value.copy(
                                posts = physiotherapistPosts,
                                totalLikes = totalLikes,
                                totalComments = totalComments,
                                isLoading = false
                            )
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                error = result.message ?: "Paylaşımlar yüklenemedi",
                                isLoading = false
                            )
                        }
                        is Resource.Loading -> {
                            // Zaten yükleniyor
                        }
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Paylaşımlar yüklenirken bir hata oluştu: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun refreshData() {
        _state.value = _state.value.copy(isLoading = true)

        if (physiotherapistId != null && physiotherapistId.isNotEmpty()) {
            loadSpecificPhysiotherapistProfile(physiotherapistId)
        } else {
            loadCurrentUserProfile()
        }
    }
}