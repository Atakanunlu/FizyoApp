package com.example.fizyoapp.presentation.socialmedia.socialmediamain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.auth.User
import com.example.fizyoapp.domain.model.auth.UserRole
import com.example.fizyoapp.domain.model.physiotherapist_profile.PhysiotherapistProfile
import com.example.fizyoapp.domain.model.user_profile.UserProfile
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.follow.FollowPhysiotherapistUseCase
import com.example.fizyoapp.domain.usecase.follow.IsFollowingUseCase
import com.example.fizyoapp.domain.usecase.follow.UnfollowPhysiotherapistUseCase
import com.example.fizyoapp.domain.usecase.physiotherapist_profile.GetPhysiotherapistProfileUseCase
import com.example.fizyoapp.domain.usecase.socialmedia.GetAllPostsUseCase
import com.example.fizyoapp.domain.usecase.socialmedia.LikePostUseCase
import com.example.fizyoapp.domain.usecase.socialmedia.UnlikePostUseCase
import com.example.fizyoapp.domain.usecase.user_profile.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import android.util.Log
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class SocialMediaViewModel @Inject constructor(
    private val getAllPostsUseCase: GetAllPostsUseCase,
    private val likePostUseCase: LikePostUseCase,
    private val unlikePostUseCase: UnlikePostUseCase,
    private val getCurrentUserUseCase: GetCurrentUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getPhysiotherapistProfileUseCase: GetPhysiotherapistProfileUseCase,
    private val followPhysiotherapistUseCase: FollowPhysiotherapistUseCase,
    private val unfollowPhysiotherapistUseCase: UnfollowPhysiotherapistUseCase,
    private val isFollowingUseCase: IsFollowingUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(SocialMediaState())
    val state: StateFlow<SocialMediaState> = _state.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _physiotherapistProfile = MutableStateFlow<PhysiotherapistProfile?>(null)
    val physiotherapistProfile: StateFlow<PhysiotherapistProfile?> = _physiotherapistProfile.asStateFlow()

    private val followStateMapInternal = mutableMapOf<String, Boolean>()
    private val _followStateMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val followStateMap: StateFlow<Map<String, Boolean>> = _followStateMap.asStateFlow()

    private val followLoadingMapInternal = mutableMapOf<String, Boolean>()
    private val _followLoadingMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val followLoadingMap: StateFlow<Map<String, Boolean>> = _followLoadingMap.asStateFlow()

    private val mutex = Mutex()
    private var hasInitializedData = false
    private var followCheckJobs = mutableMapOf<String, Job>()

    init {
        getCurrentUser()
    }

    fun initializeScreen() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            mutex.withLock {
                followLoadingMapInternal.clear()
                _followLoadingMap.value = emptyMap()
            }

            getCurrentUser()
            loadPosts()

            startPeriodicFollowChecks()

            hasInitializedData = true
        }
    }

    private fun startPeriodicFollowChecks() {
        viewModelScope.launch {
            while (true) {
                delay(30000) // 30 saniyede bir kontrol et
                if (_state.value.posts.isNotEmpty() && _currentUser.value != null) {
                    checkAllFollowStates()
                }
            }
        }
    }

    private fun getCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _currentUser.value = result.data
                        result.data?.let { loadUserInfo(it) }
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = result.message ?: "Kullanıcı bilgisi alınamadı"
                        )
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    fun loadUserInfo() {
        val user = _currentUser.value
        if (user != null) {
            loadUserInfo(user)
        }
    }

    private fun loadUserInfo(user: User) {
        viewModelScope.launch {
            when (user.role) {
                UserRole.USER -> {
                    getUserProfileUseCase(user.id).collect { result ->
                        if (result is Resource.Success) {
                            _userProfile.value = result.data
                        } else if (result is Resource.Error) {
                            _state.value = _state.value.copy(
                                error = result.message ?: "Kullanıcı profili alınamadı"
                            )
                        }
                    }
                }
                UserRole.PHYSIOTHERAPIST -> {
                    getPhysiotherapistProfileUseCase(user.id).collect { result ->
                        if (result is Resource.Success) {
                            _physiotherapistProfile.value = result.data
                        } else if (result is Resource.Error) {
                            _state.value = _state.value.copy(
                                error = result.message ?: "Fizyoterapist profili alınamadı"
                            )
                        }
                    }
                }
                else -> {}
            }
        }
    }

    fun checkAllFollowStates() {
        val posts = _state.value.posts
        val currentUserId = _currentUser.value?.id ?: return

        viewModelScope.launch {

            followCheckJobs.forEach { (_, job) ->
                if (job.isActive) job.cancel()
            }
            followCheckJobs.clear()

            mutex.withLock {
                followLoadingMapInternal.clear()
                val physiotherapistIds = posts
                    .filter { it.userRole == "PHYSIOTHERAPIST" && it.userId != currentUserId }
                    .map { it.userId }
                    .distinct()

                physiotherapistIds.forEach { id ->
                    followLoadingMapInternal[id] = true
                }

                _followLoadingMap.value = HashMap(followLoadingMapInternal)
            }

            val physiotherapistIds = posts
                .filter { it.userRole == "PHYSIOTHERAPIST" && it.userId != currentUserId }
                .map { it.userId }
                .distinct()

            Log.d("SocialMediaVM", "Checking follow states for ${physiotherapistIds.size} physiotherapists")

            physiotherapistIds.forEach { physiotherapistId ->
                val job = viewModelScope.launch {
                    try {
                        isFollowingUseCase(currentUserId, physiotherapistId).collect { result ->
                            when (result) {
                                is Resource.Success -> {
                                    mutex.withLock {
                                        followStateMapInternal[physiotherapistId] = result.data
                                        followLoadingMapInternal[physiotherapistId] = false
                                        _followStateMap.value = HashMap(followStateMapInternal)
                                        _followLoadingMap.value = HashMap(followLoadingMapInternal)

                                        Log.d("SocialMediaVM", "Follow state for $physiotherapistId: ${result.data}")
                                    }
                                }
                                is Resource.Error -> {
                                    mutex.withLock {
                                        followLoadingMapInternal[physiotherapistId] = false
                                        _followLoadingMap.value = HashMap(followLoadingMapInternal)
                                    }
                                    Log.e("SocialMediaVM", "Error checking follow state: ${result.message}")
                                }
                                is Resource.Loading -> {
                                    // Loading durumunda işlem yok
                                }
                            }
                        }
                    } catch (e: Exception) {
                        mutex.withLock {
                            followLoadingMapInternal[physiotherapistId] = false
                            _followLoadingMap.value = HashMap(followLoadingMapInternal)
                        }
                        Log.e("SocialMediaVM", "Exception checking follow state: ${e.message}", e)
                    }
                }

                followCheckJobs[physiotherapistId] = job
            }
        }
    }

    fun toggleFollow(physiotherapistId: String) {
        val currentUser = _currentUser.value ?: return

        viewModelScope.launch {
            mutex.withLock {
                val isCurrentlyFollowing = followStateMapInternal[physiotherapistId] ?: false
                followLoadingMapInternal[physiotherapistId] = true
                _followLoadingMap.value = HashMap(followLoadingMapInternal)
            }

            val isCurrentlyFollowing = mutex.withLock { followStateMapInternal[physiotherapistId] ?: false }

            try {
                if (isCurrentlyFollowing) {
                    Log.d("SocialMediaVM", "Attempting to unfollow $physiotherapistId")
                    unfollowPhysiotherapistUseCase(currentUser.id, physiotherapistId).collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                mutex.withLock {
                                    followStateMapInternal[physiotherapistId] = false
                                    followLoadingMapInternal[physiotherapistId] = false
                                    _followStateMap.value = HashMap(followStateMapInternal)
                                    _followLoadingMap.value = HashMap(followLoadingMapInternal)
                                    Log.d("SocialMediaVM", "Successfully unfollowed $physiotherapistId")
                                }
                            }
                            is Resource.Error -> {
                                mutex.withLock {
                                    followLoadingMapInternal[physiotherapistId] = false
                                    _followLoadingMap.value = HashMap(followLoadingMapInternal)
                                }
                                _state.value = _state.value.copy(
                                    error = result.message ?: "Takipten çıkma işlemi başarısız oldu"
                                )
                                Log.e("SocialMediaVM", "Error unfollowing: ${result.message}")
                            }
                            is Resource.Loading -> {}
                        }
                    }
                } else {
                    Log.d("SocialMediaVM", "Attempting to follow $physiotherapistId")
                    followPhysiotherapistUseCase(
                        currentUser.id,
                        currentUser.role.toString(),
                        physiotherapistId
                    ).collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                mutex.withLock {
                                    followStateMapInternal[physiotherapistId] = true
                                    followLoadingMapInternal[physiotherapistId] = false
                                    _followStateMap.value = HashMap(followStateMapInternal)
                                    _followLoadingMap.value = HashMap(followLoadingMapInternal)
                                    Log.d("SocialMediaVM", "Successfully followed $physiotherapistId")
                                }
                            }
                            is Resource.Error -> {
                                mutex.withLock {
                                    followLoadingMapInternal[physiotherapistId] = false
                                    _followLoadingMap.value = HashMap(followLoadingMapInternal)
                                }
                                _state.value = _state.value.copy(
                                    error = result.message ?: "Takip etme işlemi başarısız oldu"
                                )
                                Log.e("SocialMediaVM", "Error following: ${result.message}")
                            }
                            is Resource.Loading -> {}
                        }
                    }
                }
            } catch (e: Exception) {
                mutex.withLock {
                    followLoadingMapInternal[physiotherapistId] = false
                    _followLoadingMap.value = HashMap(followLoadingMapInternal)
                }
                _state.value = _state.value.copy(
                    error = "İşlem sırasında bir hata oluştu: ${e.message}"
                )
                Log.e("SocialMediaVM", "Exception in toggleFollow: ${e.message}", e)
            }
        }
    }

    fun loadPosts() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            getAllPostsUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            posts = result.data,
                            isLoading = false,
                            error = null
                        )
                        checkAllFollowStates()
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.message ?: "Gönderiler yüklenemedi"
                        )
                    }
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    fun onLikePost(postId: String) {
        val userId = _currentUser.value?.id ?: return
        val post = _state.value.posts.find { it.id == postId } ?: return

        viewModelScope.launch {
            if (post.likedBy.contains(userId)) {
                unlikePostUseCase(postId, userId).collect { result ->
                    if (result is Resource.Success) {
                        loadPosts()
                    }
                }
            } else {
                likePostUseCase(postId, userId).collect { result ->
                    if (result is Resource.Success) {
                        loadPosts()
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        followCheckJobs.forEach { (_, job) ->
            if (job.isActive) job.cancel()
        }
    }
}