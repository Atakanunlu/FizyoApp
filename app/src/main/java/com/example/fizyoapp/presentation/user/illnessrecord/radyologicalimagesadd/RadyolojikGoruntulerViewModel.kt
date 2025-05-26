package com.example.fizyoapp.presentation.user.illnessrecord.radyologicalimagesadd

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.repository.auth.AuthRepository
import com.example.fizyoapp.data.repository.illnessrecordscreen.radiologicalimagesscreen.RadyolojikGoruntuRepository
import com.example.fizyoapp.data.repository.messagesscreen.MessagesRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.messagesscreen.ChatThread
import com.example.fizyoapp.domain.model.messagesscreen.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class RadyolojikGoruntulerViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val radyolojikGoruntuRepository: RadyolojikGoruntuRepository,
    private val messagesRepository: MessagesRepository,
    private val storage: FirebaseStorage
) : ViewModel() {
    private val _state = MutableStateFlow(RadyolojikGoruntulerState())
    val state: StateFlow<RadyolojikGoruntulerState> = _state.asStateFlow()

    private var selectedFileUri: Uri? = null
    private var selectedFileType: String = "image"

    init {
        loadData()
    }

    fun onEvent(event: RadyolojikGoruntulerEvent) {
        when (event) {
            is RadyolojikGoruntulerEvent.RefreshData -> {
                loadData()
            }
            is RadyolojikGoruntulerEvent.DismissError -> {
                _state.update { it.copy(actionError = null) }
            }
            is RadyolojikGoruntulerEvent.FileSelected -> {
                selectedFileUri = event.uri
                val mimeType = event.uri.path?.let {
                    if (it.endsWith(".pdf", ignoreCase = true)) "application/pdf" else "image/*"
                } ?: "image/*"

                selectedFileType = if (mimeType.contains("pdf")) "pdf" else "image"
            }
            is RadyolojikGoruntulerEvent.AddImage -> {
                addFile(event.title, event.description, "image")
            }
            is RadyolojikGoruntulerEvent.AddPdf -> {
                addFile(event.title, event.description, "pdf")
            }
            is RadyolojikGoruntulerEvent.ShareImage -> {
                shareImage(event.imageId, event.userId)
            }
            is RadyolojikGoruntulerEvent.DeleteImage -> {
                deleteImage(event.fileUrl)
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Oturum açmanız gerekiyor"
                    )
                }
                return@launch
            }
            val userId = currentUser.uid
            _state.update { it.copy(currentUserId = userId) }
            loadRadyolojikGoruntuler(userId)
            loadRecentThreads(userId)
        }
    }

    private suspend fun loadRadyolojikGoruntuler(userId: String) {
        radyolojikGoruntuRepository.getRadyolojikGoruntuler(userId).collect { result ->
            when (result) {
                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            goruntular = result.data ?: emptyList(),
                            isLoading = false,
                            error = null
                        )
                    }
                }
                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Radyolojik görüntüler yüklenemedi"
                        )
                    }
                }
                is Resource.Loading -> {
                    _state.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    private suspend fun loadRecentThreads(userId: String) {
        messagesRepository.getChatTreadsForUser(userId).collect { result ->
            when (result) {
                is Resource.Success -> {
                    _state.update {
                        it.copy(recentThreads = result.data ?: emptyList())
                    }
                }
                is Resource.Error -> {
                }
                is Resource.Loading -> {
                }
            }
        }
    }

    private fun addFile(title: String, description: String, fileType: String) {
        if (selectedFileUri == null) {
            _state.update {
                it.copy(actionError = "Geçerli bir dosya seçilmedi")
            }
            return
        }

        val userId = state.value.currentUserId
        if (userId.isBlank()) {
            _state.update {
                it.copy(actionError = "Kullanıcı bilgileriniz alınamadı")
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            radyolojikGoruntuRepository.uploadRadyolojikGoruntu(
                fileUri = selectedFileUri!!,
                title = title,
                description = description,
                userId = userId,
                fileType = fileType
            ).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                successMessage = if (fileType == "pdf")
                                    "Radyolojik PDF başarıyla kaydedildi"
                                else
                                    "Radyolojik görüntü başarıyla kaydedildi"
                            )
                        }
                        selectedFileUri = null
                        delay(500)
                        loadRadyolojikGoruntuler(userId)
                        delay(3000)
                        _state.update { it.copy(successMessage = null) }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                actionError = result.message ?: "Dosya yüklenirken bir hata oluştu"
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

    private fun shareImage(imageId: String, receiverId: String) {
        val userId = state.value.currentUserId
        if (userId.isBlank()) {
            _state.update {
                it.copy(actionError = "Kullanıcı bilgileriniz alınamadı")
            }
            return
        }

        val imageToShare = state.value.goruntular.find { it.id == imageId }
        if (imageToShare == null) {
            _state.update {
                it.copy(actionError = "Paylaşılacak görüntü bulunamadı")
            }
            return
        }

        viewModelScope.launch {
            try {
                val imageData = JSONObject().apply {
                    put("title", imageToShare.title)
                    put("description", imageToShare.description)
                    put("url", imageToShare.fileUrl)
                    put("timestamp", imageToShare.timestamp.time.toString())
                    put("fileType", imageToShare.fileType) // Dosya tipini ekliyoruz
                }
                val messageContent = "[RADIOLOGICAL_IMAGE]\n${imageData}"
                val message = Message(
                    id = "",
                    senderId = userId,
                    receiverId = receiverId,
                    content = messageContent,
                    timestamp = Date(),
                    isRead = false,
                    threadId = ""
                )
                messagesRepository.sendMessage(message).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _state.update {
                                it.copy(
                                    successMessage = "Radyolojik görüntü başarıyla paylaşıldı"
                                )
                            }
                            delay(3000)
                            _state.update { it.copy(successMessage = null) }
                        }
                        is Resource.Error -> {
                            _state.update {
                                it.copy(
                                    actionError = result.message ?: "Görüntü paylaşılırken bir hata oluştu"
                                )
                            }
                        }
                        is Resource.Loading -> {
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(actionError = "Görüntü paylaşımı sırasında bir hata oluştu: ${e.message}")
                }
            }
        }
    }

    private fun deleteImage(imageUrl: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            radyolojikGoruntuRepository.deleteRadyolojikGoruntu(imageUrl).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "Radyolojik görüntü başarıyla silindi"
                            )
                        }
                        delay(500)
                        loadRadyolojikGoruntuler(state.value.currentUserId)
                        delay(3000)
                        _state.update { it.copy(successMessage = null) }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                actionError = result.message ?: "Görüntü silinirken bir hata oluştu"
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
}