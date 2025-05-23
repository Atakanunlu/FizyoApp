package com.example.fizyoapp.presentation.socialmedia.physiotherapistsocialprofile

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.auth.User
import com.example.fizyoapp.domain.model.auth.UserRole
import com.example.fizyoapp.domain.model.follow.FollowRelation
import com.example.fizyoapp.domain.model.physiotherapist_profile.PhysiotherapistProfile
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.follow.*
import com.example.fizyoapp.domain.usecase.physiotherapist_profile.GetPhysiotherapistByIdUseCase
import com.example.fizyoapp.domain.usecase.physiotherapist_profile.GetPhysiotherapistProfileUseCase
import com.example.fizyoapp.domain.usecase.socialmedia.GetAllPostsUseCase
import com.example.fizyoapp.domain.usecase.user_profile.GetUserProfileUseCase
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
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getAllPostsUseCase: GetAllPostsUseCase,
    private val followPhysiotherapistUseCase: FollowPhysiotherapistUseCase,
    private val unfollowPhysiotherapistUseCase: UnfollowPhysiotherapistUseCase,
    private val isFollowingUseCase: IsFollowingUseCase,
    private val getFollowersCountUseCase: GetFollowersCountUseCase,
    private val getFollowingCountUseCase: GetFollowingCountUseCase,
    private val getFollowersUseCase: GetFollowersUseCase,
    private val getFollowingUseCase: GetFollowingUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _state = MutableStateFlow(PhysiotherapistSocialProfileState(isLoading = true))
    val state: StateFlow<PhysiotherapistSocialProfileState> = _state.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()


    private val physiotherapistId: String? = savedStateHandle.get<String>("physiotherapistId")

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val user = result.data
                        if (user != null) {
                            _currentUser.value = user

                            if (physiotherapistId != null && physiotherapistId.isNotEmpty()) {
                                loadSpecificPhysiotherapistProfile(physiotherapistId)
                            } else {
                                if (user.role == UserRole.PHYSIOTHERAPIST) {
                                    loadPhysiotherapistProfileAndData(user.id)
                                } else {
                                    _state.value = _state.value.copy(
                                        error = "Sadece fizyoterapistlerin profili görüntülenebilir",
                                        isLoading = false
                                    )
                                }
                            }
                        } else {
                            _state.value = _state.value.copy(
                                error = "Kullanıcı bulunamadı",
                                isLoading = false
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = result.message ?: "Kullanıcı bilgisi alınamadı",
                            isLoading = false
                        )
                    }
                    is Resource.Loading -> {
                    }
                }
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

                            loadAllProfileData(physiotherapistId)
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                error = result.message ?: "Fizyoterapist profili yüklenemedi",
                                isLoading = false
                            )
                        }
                        is Resource.Loading -> {
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

    private fun loadPhysiotherapistProfileAndData(userId: String) {
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

                            loadAllProfileData(userId)
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                error = profileResult.message ?: "Profil yüklenemedi",
                                isLoading = false
                            )
                        }
                        is Resource.Loading -> {
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

    private fun loadAllProfileData(userId: String) {
        loadPosts(userId)
        loadFollowersCount(userId)
        loadFollowingCount(userId)

        val currentUserId = _currentUser.value?.id
        if (currentUserId != null && userId != currentUserId) {
            checkFollowState(userId)
        }
    }

    private fun loadPosts(userId: String) {
        viewModelScope.launch {
            try {
                getAllPostsUseCase().collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val physiotherapistPosts = result.data.filter { it.userId == userId }

                            val totalLikes = physiotherapistPosts.sumOf { it.likeCount }
                            val totalComments = physiotherapistPosts.sumOf { it.commentCount }

                            _state.value = _state.value.copy(
                                posts = physiotherapistPosts,
                                totalLikes = totalLikes,
                                totalComments = totalComments
                            )
                        }
                        is Resource.Error -> {
                            Log.e("PhysiotherapistProfileVM", "Error loading posts: ${result.message}")
                        }
                        is Resource.Loading -> {
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("PhysiotherapistProfileVM", "Exception loading posts", e)
            }
        }
    }

    private fun checkFollowState(physiotherapistId: String) {
        val currentUserId = _currentUser.value?.id ?: return

        viewModelScope.launch {
            isFollowingUseCase(currentUserId, physiotherapistId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            isFollowing = result.data
                        )
                    }
                    is Resource.Error -> {
                        Log.e("PhysiotherapistProfileVM", "Error checking follow state: ${result.message}")
                    }
                    is Resource.Loading -> {
                    }
                }
            }
        }
    }

    fun toggleFollow(physiotherapistId: String) {
        val currentUser = _currentUser.value ?: return
        val isCurrentlyFollowing = _state.value.isFollowing

        _state.value = _state.value.copy(isFollowLoading = true)

        viewModelScope.launch {
            try {
                if (isCurrentlyFollowing) {
                    unfollowPhysiotherapistUseCase(currentUser.id, physiotherapistId).collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                _state.value = _state.value.copy(
                                    isFollowing = false,
                                    isFollowLoading = false
                                )

                                loadFollowersCount(physiotherapistId)
                            }
                            is Resource.Error -> {
                                _state.value = _state.value.copy(
                                    error = result.message,
                                    isFollowLoading = false
                                )
                            }
                            is Resource.Loading -> {

                            }
                        }
                    }
                } else {

                    followPhysiotherapistUseCase(
                        currentUser.id,
                        currentUser.role.toString(),
                        physiotherapistId
                    ).collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                _state.value = _state.value.copy(
                                    isFollowing = true,
                                    isFollowLoading = false
                                )
                                loadFollowersCount(physiotherapistId)
                            }
                            is Resource.Error -> {
                                _state.value = _state.value.copy(
                                    error = result.message,
                                    isFollowLoading = false
                                )
                            }
                            is Resource.Loading -> {
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "İşlem sırasında bir hata oluştu: ${e.message}",
                    isFollowLoading = false
                )
            }
        }
    }

    private fun loadFollowersCount(physiotherapistId: String) {
        viewModelScope.launch {
            getFollowersCountUseCase(physiotherapistId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            followersCount = result.data
                        )
                    }
                    is Resource.Error -> {
                        Log.e("PhysiotherapistProfileVM", "Error loading followers count: ${result.message}")
                    }
                    is Resource.Loading -> {
                    }
                }
            }
        }
    }

    private fun loadFollowingCount(physiotherapistId: String) {
        viewModelScope.launch {
            getFollowingCountUseCase(physiotherapistId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            followingCount = result.data
                        )
                    }
                    is Resource.Error -> {
                        Log.e("PhysiotherapistProfileVM", "Error loading following count: ${result.message}")
                    }
                    is Resource.Loading -> {
                    }
                }
            }
        }
    }

    fun loadFollowers(physiotherapistId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            getFollowersUseCase(physiotherapistId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            followers = result.data,
                            isLoading = false
                        )
                        loadFollowerProfiles(result.data)
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = result.message,
                            isLoading = false
                        )
                    }
                    is Resource.Loading -> {
                    }
                }
            }
        }
    }

    fun loadFollowing(physiotherapistId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            getFollowingUseCase(physiotherapistId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            following = result.data,
                            isLoading = false
                        )

                        loadFollowingProfiles(result.data)
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = result.message,
                            isLoading = false
                        )
                    }
                    is Resource.Loading -> {

                    }
                }
            }
        }
    }


    private fun loadFollowerProfiles(followers: List<FollowRelation>) {
        viewModelScope.launch {
            val profiles = mutableMapOf<String, Any>()

            followers.forEach { followRelation ->
                try {
                    if (followRelation.followerRole == "PHYSIOTHERAPIST") {
                        getPhysiotherapistByIdUseCase(followRelation.followerId).collect { result ->
                            if (result is Resource.Success && result.data != null) {
                                profiles[followRelation.followerId] = result.data
                                _state.value = _state.value.copy(
                                    followerProfiles = profiles.toMap()
                                )
                            }
                        }
                    } else {
                        getUserProfileUseCase(followRelation.followerId).collect { result ->
                            if (result is Resource.Success && result.data != null) {
                                profiles[followRelation.followerId] = result.data
                                _state.value = _state.value.copy(
                                    followerProfiles = profiles.toMap()
                                )
                            }
                        }
                    }
                } catch (e: Exception) {

                }
            }
        }
    }


    private fun loadFollowingProfiles(following: List<FollowRelation>) {
        viewModelScope.launch {
            val profiles = mutableMapOf<String, PhysiotherapistProfile>()

            following.forEach { followRelation ->
                try {

                    getPhysiotherapistByIdUseCase(followRelation.followedId).collect { result ->
                        if (result is Resource.Success && result.data != null) {
                            profiles[followRelation.followedId] = result.data
                            _state.value = _state.value.copy(
                                followingProfiles = profiles.toMap()
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e("PhysiotherapistProfileVM", "Error loading following profile", e)
                }
            }
        }
    }

    fun toggleShowFollowers() {
        _state.value = _state.value.copy(
            showFollowers = !_state.value.showFollowers,
            showFollowing = false
        )

        if (_state.value.showFollowers) {
            val profileId = physiotherapistId ?: _currentUser.value?.id
            profileId?.let { loadFollowers(it) }
        }
    }

    fun toggleShowFollowing() {
        _state.value = _state.value.copy(
            showFollowing = !_state.value.showFollowing,
            showFollowers = false
        )

        if (_state.value.showFollowing) {
            val profileId = physiotherapistId ?: _currentUser.value?.id
            profileId?.let { loadFollowing(it) }
        }
    }

    fun refreshData() {
        _state.value = _state.value.copy(isLoading = true)

        if (physiotherapistId != null && physiotherapistId.isNotEmpty()) {
            loadSpecificPhysiotherapistProfile(physiotherapistId)
        } else {
            val currentUser = _currentUser.value
            if (currentUser != null && currentUser.role == UserRole.PHYSIOTHERAPIST) {
                loadPhysiotherapistProfileAndData(currentUser.id)
            }
        }
    }
}