package com.example.fizyoapp.presentation.bottomnavbar.items.searchscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.physiotherapist_profile.PhysiotherapistProfile
import com.example.fizyoapp.domain.usecase.physiotherapist_profile.GetAllPhysiotherapistsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchScreenViewModel @Inject constructor(
    private val getAllPhysiotherapistsUseCase: GetAllPhysiotherapistsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SearchScreenState())
    val state: StateFlow<SearchScreenState> = _state.asStateFlow()

    private val _uiEvent = Channel<SearchScreenUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadPhysiotherapists()
    }

    fun onEvent(event: SearchScreenEvent) {
        when (event) {
            is SearchScreenEvent.SearchQueryChanged -> {
                _state.value = _state.value.copy(
                    searchQuery = event.query,
                    filteredPhysiotherapists = filterPhysiotherapists(event.query)
                )
            }

            is SearchScreenEvent.RefreshPhysiotherapists -> {
                loadPhysiotherapists()
            }

            is SearchScreenEvent.NavigateToPhysiotherapistDetail -> {
                viewModelScope.launch {
                    _uiEvent.send(SearchScreenUiEvent.NavigateToPhysiotherapistDetail(event.physiotherapistId))
                }
            }
        }
    }

    private fun loadPhysiotherapists() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null
            )

            getAllPhysiotherapistsUseCase().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }

                    is Resource.Success -> {
                        val physioList = result.data

                        _state.value = _state.value.copy(
                            isLoading = false,
                            physiotherapists = physioList,
                            filteredPhysiotherapists = physioList,
                            error = if (physioList.isEmpty()) "Kayıtlı fizyoterapist bulunamadı" else null
                        )
                    }

                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.message

                        )
                    }
                }
            }
        }
    }

    private fun filterPhysiotherapists(query: String): List<PhysiotherapistProfile> {
        val physiotherapists = _state.value.physiotherapists

        return if (query.isBlank()) {
            physiotherapists
        } else {
            physiotherapists.filter { physio ->
                physio.firstName.contains(query, ignoreCase = true) ||
                        physio.lastName.contains(query, ignoreCase = true) ||
                        physio.city.contains(query, ignoreCase = true) ||
                        physio.district.contains(query, ignoreCase = true)
            }
        }
    }

    sealed class SearchScreenUiEvent {
        data class NavigateToPhysiotherapistDetail(val physiotherapistId: String) :
            SearchScreenUiEvent()
    }
}