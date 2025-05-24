package com.example.fizyoapp.data.repository.advertisement

import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdvertisementDataRepository @Inject constructor() {
    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri: StateFlow<Uri?> = _imageUri.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    fun setImageUri(uri: Uri?) {
        _imageUri.value = uri
    }

    fun setDescription(description: String) {
        _description.value = description
    }

    fun clear() {
        _imageUri.value = null
        _description.value = ""
    }
}