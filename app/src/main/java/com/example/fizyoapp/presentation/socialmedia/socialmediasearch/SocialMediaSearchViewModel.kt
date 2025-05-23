package com.example.fizyoapp.presentation.socialmedia.socialmediasearch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.auth.User
import com.example.fizyoapp.domain.model.physiotherapist_profile.PhysiotherapistProfile
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.physiotherapist_profile.GetAllPhysiotherapistsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SocialMediaSearchViewModel @Inject constructor(
    private val getAllPhysiotherapistsUseCase: GetAllPhysiotherapistsUseCase,
    private val getCurrentUserUseCase: GetCurrentUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(SocialMediaSearchState())
    val state: StateFlow<SocialMediaSearchState> = _state.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private var allPhysiotherapists: List<PhysiotherapistProfile> = emptyList()
    private val searchHistory = mutableSetOf<PhysiotherapistProfile>()

    init {
        loadCurrentUser()
        loadAllPhysiotherapists()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                getCurrentUserUseCase().collect { result ->
                    if (result is Resource.Success) {
                        _currentUser.value = result.data
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Kullanıcı bilgisi alınırken hata oluştu"
                )
            }
        }
    }

    private fun loadAllPhysiotherapists() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            try {
                getAllPhysiotherapistsUseCase().collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            allPhysiotherapists = result.data
                            _state.value = _state.value.copy(
                                searchResults = emptyList(),
                                isLoading = false
                            )
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                error = result.message ?: "Fizyoterapistler yüklenemedi",
                                isLoading = false
                            )
                        }
                        is Resource.Loading -> {
                            _state.value = _state.value.copy(isLoading = true)
                        }
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Fizyoterapistler yüklenirken hata oluştu: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.value = _state.value.copy(searchQuery = query)

        if (query.isBlank()) {
            _state.value = _state.value.copy(
                searchResults = emptyList(),
                hasSearched = false
            )
            return
        }

        _state.value = _state.value.copy(hasSearched = true)

        val filteredResults = allPhysiotherapists.filter { physiotherapist ->
            val fullName = "${physiotherapist.firstName} ${physiotherapist.lastName}".lowercase()
            fullName.contains(query.lowercase())
        }

        _state.value = _state.value.copy(searchResults = filteredResults)
    }

    fun addToSearchHistory(physiotherapist: PhysiotherapistProfile) {
        searchHistory.remove(physiotherapist)
        searchHistory.add(physiotherapist)
        updateSearchHistory()
    }

    fun clearSearchHistory() {
        searchHistory.clear()
        updateSearchHistory()
    }

    private fun updateSearchHistory() {
        _state.value = _state.value.copy(
            searchHistory = searchHistory.toList().reversed()
        )
    }
}