package com.example.fizyoapp.presentation.bottomnavbar.items.searchscreen

sealed class SearchScreenEvent {
    data class SearchQueryChanged(val query: String) : SearchScreenEvent()
    data object RefreshPhysiotherapists : SearchScreenEvent()
    data class NavigateToPhysiotherapistDetail(val physiotherapistId: String) : SearchScreenEvent()
}