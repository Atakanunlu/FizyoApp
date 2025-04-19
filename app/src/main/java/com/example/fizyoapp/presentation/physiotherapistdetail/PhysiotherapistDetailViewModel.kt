
package com.example.fizyoapp.presentation.physiotherapistdetail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.usecase.physiotherapist_profile.GetPhysiotherapistByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhysiotherapistDetailViewModel @Inject constructor(
    private val getPhysiotherapistByIdUseCase: GetPhysiotherapistByIdUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(PhysiotherapistDetailState())
    val state: StateFlow<PhysiotherapistDetailState> = _state.asStateFlow()

    init {
        savedStateHandle.get<String>("physiotherapistId")?.let { physiotherapistId ->
            loadPhysiotherapist(physiotherapistId)
        }
    }

    private fun loadPhysiotherapist(physiotherapistId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            getPhysiotherapistByIdUseCase(physiotherapistId).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }

                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            physiotherapist = result.data,
                            error = null
                        )
                    }

                    is Resource.Error -> {
                        Log.e("DetailVM", "Fizyoterapist detayı yükleme hatası", result.exception)
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }

                }
            }
        }
    }
}