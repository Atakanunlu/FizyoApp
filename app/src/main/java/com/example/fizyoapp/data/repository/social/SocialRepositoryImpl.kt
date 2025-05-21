package com.example.fizyoapp.data.repository.social

import android.net.Uri
import android.util.Log
import com.example.fizyoapp.data.repository.auth.AuthRepository
import com.example.fizyoapp.data.repository.physiotherapist_profile.PhysiotherapistProfileRepository
import com.example.fizyoapp.data.repository.user_profile.UserProfileRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.auth.User
import com.example.fizyoapp.domain.model.auth.UserRole
import com.example.fizyoapp.domain.model.social.Comment
import com.example.fizyoapp.domain.model.social.Post
import com.example.fizyoapp.domain.model.social.SocialProfile
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class SocialRepositoryImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val physiotherapistProfileRepository: PhysiotherapistProfileRepository,
    private val userProfileRepository: UserProfileRepository
) : SocialRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val postsCollection = firestore.collection("posts")
    private val commentsCollection = firestore.collection("comments")
    private val followsCollection = firestore.collection("follows")
    private val likesCollection = firestore.collection("likes")

    override fun getGeneralPosts(): Flow<Resource<List<Post>>> = callbackFlow {
        trySend(Resource.Loading())

        try {
            // Önce mevcut kullanıcıyı al, yoksa erken dön
            val currentUser = getCurrentUser()
            if (currentUser == null) {
                trySend(Resource.Error("Kullanıcı oturumu bulunamadı"))
                close()
                return@callbackFlow
            }

            val listener = postsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        if (!isClosedForSend) {
                            trySend(Resource.Error("Gönderiler yüklenirken hata oluştu: ${error.message}"))
                        }
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val posts = snapshot.documents.mapNotNull { document ->
                            try {
                                document.toObject(Post::class.java)?.copy(id = document.id)
                            } catch (e: Exception) {
                                Log.e("SocialRepository", "Post dönüştürme hatası", e)
                                null
                            }
                        }

                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                if (isClosedForSend) return@launch

                                val postsWithLikeStatus = mutableListOf<Post>()

                                for (post in posts) {
                                    try {
                                        val isLiked = isPostLikedByUser(currentUser.id, post.id)
                                        postsWithLikeStatus.add(post.copy(isLikedByCurrentUser = isLiked))
                                    } catch (e: Exception) {
                                        Log.e("SocialRepository", "Like durumu kontrolü hatası", e)
                                        postsWithLikeStatus.add(post)
                                    }
                                }

                                if (!isClosedForSend) {
                                    trySend(Resource.Success(postsWithLikeStatus))
                                }
                            } catch (e: Exception) {
                                Log.e("SocialRepository", "Post işleme hatası", e)
                                if (!isClosedForSend) {
                                    trySend(Resource.Success(posts))
                                }
                            }
                        }
                    }
                }

            awaitClose {
                Log.d("SocialRepository", "General posts listener removing")
                listener.remove()
            }

        } catch (e: Exception) {
            Log.e("SocialRepository", "Genel akış alınırken hata", e)
            if (!isClosedForSend) {
                trySend(Resource.Error("Gönderiler alınamadı: ${e.message}"))
            }
            close(e)
        }
    }

    override fun getFollowingPosts(): Flow<Resource<List<Post>>> = callbackFlow {
        try {
            trySend(Resource.Loading())

            val userResult = try {
                getCurrentUser()
            } catch (e: Exception) {
                Log.e("SocialRepository", "Error getting current user for following posts", e)
                null
            }

            if (userResult == null) {
                trySend(Resource.Error("Kullanıcı oturumu bulunamadı veya hata oluştu"))
                close()
                return@callbackFlow
            }

            try {
                val followingSnapshot = followsCollection
                    .whereEqualTo("followerId", userResult.id)
                    .get()
                    .await()

                val followingIds = followingSnapshot.documents.mapNotNull { it.getString("followingId") }

                if (followingIds.isEmpty()) {
                    trySend(Resource.Success(emptyList()))
                    close()
                    return@callbackFlow
                }

                val listener = postsCollection
                    .whereIn("authorId", followingIds)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("SocialRepository", "Error getting following posts", error)
                            if (!isClosedForSend) {
                                trySend(Resource.Error("Takip edilen gönderiler yüklenirken hata: ${error.message}"))
                            }
                            return@addSnapshotListener
                        }

                        if (snapshot != null) {
                            val posts = snapshot.documents.mapNotNull { document ->
                                try {
                                    document.toObject(Post::class.java)?.copy(id = document.id)
                                } catch (e: Exception) {
                                    Log.e("SocialRepository", "Following post dönüştürme hatası", e)
                                    null
                                }
                            }

                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    if (isClosedForSend) return@launch

                                    val postsWithLikeStatus = mutableListOf<Post>()

                                    for (post in posts) {
                                        try {
                                            val isLiked = isPostLikedByUser(userResult.id, post.id)
                                            postsWithLikeStatus.add(post.copy(isLikedByCurrentUser = isLiked))
                                        } catch (e: Exception) {
                                            postsWithLikeStatus.add(post)
                                        }
                                    }

                                    if (!isClosedForSend) {
                                        trySend(Resource.Success(postsWithLikeStatus))
                                    }
                                } catch (e: Exception) {
                                    Log.e("SocialRepository", "Following posts like check error", e)
                                    if (!isClosedForSend) {
                                        trySend(Resource.Success(posts))
                                    }
                                }
                            }
                        }
                    }

                awaitClose {
                    Log.d("SocialRepository", "Removing following posts listener")
                    listener.remove()
                }

            } catch (e: Exception) {
                Log.e("SocialRepository", "Error processing following posts", e)
                if (!isClosedForSend) {
                    trySend(Resource.Error("Takip edilen gönderiler işlenirken hata: ${e.message}"))
                }
                close(e)
            }

        } catch (e: Exception) {
            Log.e("SocialRepository", "Overall error in getFollowingPosts", e)
            if (!isClosedForSend) {
                trySend(Resource.Error("Takip edilen gönderiler alınamadı: ${e.message}"))
            }
            close(e)
        }
    }

    override fun getUserPosts(userId: String): Flow<Resource<List<Post>>> = callbackFlow {
        try {
            trySend(Resource.Loading())

            val userResult = try {
                getCurrentUser()
            } catch (e: Exception) {
                Log.e("SocialRepository", "Error getting current user for user posts", e)
                null
            }

            if (userResult == null) {
                trySend(Resource.Error("Kullanıcı oturumu bulunamadı veya hata oluştu"))
                close()
                return@callbackFlow
            }

            val listener = postsCollection
                .whereEqualTo("authorId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("SocialRepository", "Error getting user posts", error)
                        if (!isClosedForSend) {
                            trySend(Resource.Error("Kullanıcı gönderileri yüklenirken hata: ${error.message}"))
                        }
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val posts = snapshot.documents.mapNotNull { document ->
                            try {
                                document.toObject(Post::class.java)?.copy(id = document.id)
                            } catch (e: Exception) {
                                Log.e("SocialRepository", "User post conversion error", e)
                                null
                            }
                        }

                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                if (isClosedForSend) return@launch

                                val postsWithLikeStatus = mutableListOf<Post>()

                                for (post in posts) {
                                    try {
                                        val isLiked = isPostLikedByUser(userResult.id, post.id)
                                        postsWithLikeStatus.add(post.copy(isLikedByCurrentUser = isLiked))
                                    } catch (e: Exception) {
                                        postsWithLikeStatus.add(post)
                                    }
                                }

                                if (!isClosedForSend) {
                                    trySend(Resource.Success(postsWithLikeStatus))
                                }
                            } catch (e: Exception) {
                                Log.e("SocialRepository", "User posts like check error", e)
                                if (!isClosedForSend) {
                                    trySend(Resource.Success(posts))
                                }
                            }
                        }
                    }
                }

            awaitClose {
                Log.d("SocialRepository", "Removing user posts listener")
                listener.remove()
            }

        } catch (e: Exception) {
            Log.e("SocialRepository", "Overall error in getUserPosts", e)
            if (!isClosedForSend) {
                trySend(Resource.Error("Kullanıcı gönderileri alınamadı: ${e.message}"))
            }
            close(e)
        }
    }

    override fun createPost(content: String, mediaUris: List<String>): Flow<Resource<Post>> = flow {
        emit(Resource.Loading())

        try {
            // Kullanıcıyı almayı dene, yoksa erken dön
            val currentUser = getCurrentUser()
            if (currentUser == null) {
                emit(Resource.Error("Kullanıcı oturumu bulunamadı"))
                return@flow
            }

            // Rol kontrolü yap
            if (currentUser.role != UserRole.PHYSIOTHERAPIST) {
                emit(Resource.Error("Sadece fizyoterapistler gönderi paylaşabilir"))
                return@flow
            }

            // Medya var mı diye kontrol et
            val mediaUrls = if (mediaUris.isNotEmpty()) {
                uploadMedia(mediaUris)
            } else {
                emptyList()
            }

            // Post verilerini oluştur
            val postData = hashMapOf(
                "authorId" to currentUser.id,
                "content" to content,
                "mediaUrls" to mediaUrls,
                "likeCount" to 0,
                "commentCount" to 0,
                "createdAt" to FieldValue.serverTimestamp()
            )

            // Firestore'a kaydet
            val postRef = postsCollection.add(postData).await()

            val createdPost = Post(
                id = postRef.id,
                authorId = currentUser.id,
                content = content,
                mediaUrls = mediaUrls,
                likeCount = 0,
                commentCount = 0,
                createdAt = Date()
            )

            emit(Resource.Success(createdPost))
        } catch (e: Exception) {
            Log.e("SocialRepository", "Gönderi oluşturma hatası", e)
            emit(Resource.Error("Gönderi oluşturulamadı: ${e.message}"))
        }
    }

    override fun likePost(postId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())

        try {
            val currentUser = getCurrentUser()

            if (currentUser == null) {
                emit(Resource.Error("Kullanıcı oturumu bulunamadı veya hata oluştu"))
                return@flow
            }

            val likeData = hashMapOf(
                "userId" to currentUser.id,
                "postId" to postId,
                "createdAt" to FieldValue.serverTimestamp()
            )

            // Check if already liked
            val existingLike = likesCollection
                .whereEqualTo("userId", currentUser.id)
                .whereEqualTo("postId", postId)
                .get()
                .await()

            if (!existingLike.isEmpty) {
                emit(Resource.Success(true)) // Already liked
                return@flow
            }

            // Add like document
            likesCollection.add(likeData).await()

            // Update post like count
            postsCollection.document(postId)
                .update("likeCount", FieldValue.increment(1))
                .await()

            emit(Resource.Success(true))
        } catch (e: Exception) {
            Log.e("SocialRepository", "Like post error", e)
            emit(Resource.Error("Gönderi beğenilemedi: ${e.message}"))
        }
    }.catch { e ->
        Log.e("SocialRepository", "Uncaught exception in likePost", e)
        emit(Resource.Error("Beklenmeyen bir hata oluştu: ${e.message}"))
    }

    override fun unlikePost(postId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())

        try {
            val currentUser = getCurrentUser()

            if (currentUser == null) {
                emit(Resource.Error("Kullanıcı oturumu bulunamadı veya hata oluştu"))
                return@flow
            }

            // Find the like document
            val likeSnapshot = likesCollection
                .whereEqualTo("userId", currentUser.id)
                .whereEqualTo("postId", postId)
                .get()
                .await()

            if (likeSnapshot.isEmpty) {
                emit(Resource.Success(true)) // Already not liked
                return@flow
            }

            // Delete the like document
            val likeDoc = likeSnapshot.documents.first()
            likesCollection.document(likeDoc.id).delete().await()

            // Update post like count
            postsCollection.document(postId)
                .update("likeCount", FieldValue.increment(-1))
                .await()

            emit(Resource.Success(true))
        } catch (e: Exception) {
            Log.e("SocialRepository", "Unlike post error", e)
            emit(Resource.Error("Gönderi beğenisi kaldırılamadı: ${e.message}"))
        }
    }.catch { e ->
        Log.e("SocialRepository", "Uncaught exception in unlikePost", e)
        emit(Resource.Error("Beklenmeyen bir hata oluştu: ${e.message}"))
    }

    override fun getComments(postId: String): Flow<Resource<List<Comment>>> = callbackFlow {
        try {
            trySend(Resource.Loading())

            val listener = commentsCollection
                .whereEqualTo("postId", postId)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("SocialRepository", "Error getting comments", error)
                        if (!isClosedForSend) {
                            trySend(Resource.Error("Yorumlar yüklenirken hata oluştu: ${error.message}"))
                        }
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val comments = snapshot.documents.mapNotNull { document ->
                            try {
                                document.toObject(Comment::class.java)?.copy(id = document.id)
                            } catch (e: Exception) {
                                Log.e("SocialRepository", "Comment conversion error", e)
                                null
                            }
                        }

                        if (!isClosedForSend) {
                            trySend(Resource.Success(comments))
                        }
                    }
                }

            awaitClose {
                Log.d("SocialRepository", "Removing comments listener")
                listener.remove()
            }

        } catch (e: Exception) {
            Log.e("SocialRepository", "Overall error in getComments", e)
            if (!isClosedForSend) {
                trySend(Resource.Error("Yorumlar alınamadı: ${e.message}"))
            }
            close(e)
        }
    }

    override fun addComment(postId: String, content: String): Flow<Resource<Comment>> = flow {
        emit(Resource.Loading())

        try {
            val currentUser = getCurrentUser()

            if (currentUser == null) {
                emit(Resource.Error("Kullanıcı oturumu bulunamadı veya hata oluştu"))
                return@flow
            }

            // Get user name and photo
            val userInfo = getUserInfo(currentUser)
            val authorName = userInfo.first
            val authorPhotoUrl = userInfo.second

            val commentData = hashMapOf(
                "postId" to postId,
                "authorId" to currentUser.id,
                "authorName" to authorName,
                "authorPhotoUrl" to authorPhotoUrl,
                "content" to content,
                "createdAt" to FieldValue.serverTimestamp()
            )

            val commentRef = commentsCollection.add(commentData).await()

            // Update post comment count
            postsCollection.document(postId)
                .update("commentCount", FieldValue.increment(1))
                .await()

            val createdComment = Comment(
                id = commentRef.id,
                postId = postId,
                authorId = currentUser.id,
                authorName = authorName,
                authorPhotoUrl = authorPhotoUrl,
                content = content,
                createdAt = Date()
            )

            emit(Resource.Success(createdComment))
        } catch (e: Exception) {
            Log.e("SocialRepository", "Add comment error", e)
            emit(Resource.Error("Yorum eklenemedi: ${e.message}"))
        }
    }.catch { e ->
        Log.e("SocialRepository", "Uncaught exception in addComment", e)
        emit(Resource.Error("Beklenmeyen bir hata oluştu: ${e.message}"))
    }

    override fun getSocialProfile(userId: String): Flow<Resource<SocialProfile>> = flow {
        emit(Resource.Loading())

        try {
            val currentUser = getCurrentUser()

            if (currentUser == null) {
                emit(Resource.Error("Kullanıcı oturumu bulunamadı veya hata oluştu"))
                return@flow
            }

            // Get user role and profile info
            val userRole = getUserRole(userId)

            val userInfo = if (userRole == UserRole.PHYSIOTHERAPIST) {
                val profile = physiotherapistProfileRepository.getPhysiotherapistProfile(userId).first()
                when (profile) {
                    is Resource.Success -> {
                        val p = profile.data
                        "${p.firstName} ${p.lastName}" to p.profilePhotoUrl
                    }
                    else -> "Fizyoterapist" to ""
                }
            } else {
                val profile = userProfileRepository.getUserProfile(userId).first()
                when (profile) {
                    is Resource.Success -> {
                        val p = profile.data
                        "${p.firstName} ${p.lastName}" to p.profilePhotoUrl
                    }
                    else -> "Kullanıcı" to ""
                }
            }

            // Get follower count
            val followersSnapshot = followsCollection
                .whereEqualTo("followingId", userId)
                .get()
                .await()

            val followersCount = followersSnapshot.size()

            // Get following count
            val followingSnapshot = followsCollection
                .whereEqualTo("followerId", userId)
                .get()
                .await()

            val followingCount = followingSnapshot.size()

            // Check if current user follows this profile
            val isFollowed = followsCollection
                .whereEqualTo("followerId", currentUser.id)
                .whereEqualTo("followingId", userId)
                .get()
                .await()
                .size() > 0

            val socialProfile = SocialProfile(
                userId = userId,
                name = userInfo.first,
                photoUrl = userInfo.second,
                followersCount = followersCount,
                followingCount = followingCount,
                isFollowedByCurrentUser = isFollowed
            )

            emit(Resource.Success(socialProfile))
        } catch (e: Exception) {
            Log.e("SocialRepository", "Get social profile error", e)
            emit(Resource.Error("Profil bilgisi alınamadı: ${e.message}"))
        }
    }.catch { e ->
        Log.e("SocialRepository", "Uncaught exception in getSocialProfile", e)
        emit(Resource.Error("Beklenmeyen bir hata oluştu: ${e.message}"))
    }

    override fun followUser(userId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())

        try {
            val currentUser = getCurrentUser()

            if (currentUser == null) {
                emit(Resource.Error("Kullanıcı oturumu bulunamadı veya hata oluştu"))
                return@flow
            }

            if (userId == currentUser.id) {
                emit(Resource.Error("Kendinizi takip edemezsiniz"))
                return@flow
            }

            // Check if already following
            val existingFollow = followsCollection
                .whereEqualTo("followerId", currentUser.id)
                .whereEqualTo("followingId", userId)
                .get()
                .await()

            if (!existingFollow.isEmpty) {
                emit(Resource.Success(true)) // Already following
                return@flow
            }

            val followData = hashMapOf(
                "followerId" to currentUser.id,
                "followingId" to userId,
                "createdAt" to FieldValue.serverTimestamp()
            )

            followsCollection.add(followData).await()

            emit(Resource.Success(true))
        } catch (e: Exception) {
            Log.e("SocialRepository", "Follow user error", e)
            emit(Resource.Error("Takip edilemedi: ${e.message}"))
        }
    }.catch { e ->
        Log.e("SocialRepository", "Uncaught exception in followUser", e)
        emit(Resource.Error("Beklenmeyen bir hata oluştu: ${e.message}"))
    }

    override fun unfollowUser(userId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())

        try {
            val currentUser = getCurrentUser()

            if (currentUser == null) {
                emit(Resource.Error("Kullanıcı oturumu bulunamadı veya hata oluştu"))
                return@flow
            }

            // Find the follow document
            val followSnapshot = followsCollection
                .whereEqualTo("followerId", currentUser.id)
                .whereEqualTo("followingId", userId)
                .get()
                .await()

            if (followSnapshot.isEmpty) {
                emit(Resource.Success(true)) // Already not following
                return@flow
            }

            // Delete the follow document
            val followDoc = followSnapshot.documents.first()
            followsCollection.document(followDoc.id).delete().await()

            emit(Resource.Success(true))
        } catch (e: Exception) {
            Log.e("SocialRepository", "Unfollow user error", e)
            emit(Resource.Error("Takip bırakılamadı: ${e.message}"))
        }
    }.catch { e ->
        Log.e("SocialRepository", "Uncaught exception in unfollowUser", e)
        emit(Resource.Error("Beklenmeyen bir hata oluştu: ${e.message}"))
    }

    override fun getFollowers(userId: String): Flow<Resource<List<SocialProfile>>> = flow {
        emit(Resource.Loading())

        try {
            // Get all users following this user
            val followersSnapshot = followsCollection
                .whereEqualTo("followingId", userId)
                .get()
                .await()

            val followerIds = followersSnapshot.documents.mapNotNull { it.getString("followerId") }

            if (followerIds.isEmpty()) {
                emit(Resource.Success(emptyList()))
                return@flow
            }

            val followers = followerIds.mapNotNull { followerId ->
                try {
                    val socialProfile = getSocialProfile(followerId).first()
                    when (socialProfile) {
                        is Resource.Success -> socialProfile.data
                        else -> null
                    }
                } catch (e: Exception) {
                    Log.e("SocialRepository", "Follower profile error", e)
                    null
                }
            }

            emit(Resource.Success(followers))
        } catch (e: Exception) {
            Log.e("SocialRepository", "Get followers error", e)
            emit(Resource.Error("Takipçiler alınamadı: ${e.message}"))
        }
    }.catch { e ->
        Log.e("SocialRepository", "Uncaught exception in getFollowers", e)
        emit(Resource.Error("Beklenmeyen bir hata oluştu: ${e.message}"))
    }

    override fun getFollowing(userId: String): Flow<Resource<List<SocialProfile>>> = flow {
        emit(Resource.Loading())

        try {
            // Get all users this user is following
            val followingSnapshot = followsCollection
                .whereEqualTo("followerId", userId)
                .get()
                .await()

            val followingIds = followingSnapshot.documents.mapNotNull { it.getString("followingId") }

            if (followingIds.isEmpty()) {
                emit(Resource.Success(emptyList()))
                return@flow
            }

            val following = followingIds.mapNotNull { followingId ->
                try {
                    val socialProfile = getSocialProfile(followingId).first()
                    when (socialProfile) {
                        is Resource.Success -> socialProfile.data
                        else -> null
                    }
                } catch (e: Exception) {
                    Log.e("SocialRepository", "Following profile error", e)
                    null
                }
            }

            emit(Resource.Success(following))
        } catch (e: Exception) {
            Log.e("SocialRepository", "Get following error", e)
            emit(Resource.Error("Takip edilenler alınamadı: ${e.message}"))
        }
    }.catch { e ->
        Log.e("SocialRepository", "Uncaught exception in getFollowing", e)
        emit(Resource.Error("Beklenmeyen bir hata oluştu: ${e.message}"))
    }

    // Helper methods
    private suspend fun getCurrentUser(): User? {
        return try {
            withContext(Dispatchers.IO) {
                val currentUserResource = authRepository.getCurrentUser().first()
                when (currentUserResource) {
                    is Resource.Success -> currentUserResource.data.user
                    else -> null
                }
            }
        } catch (e: Exception) {
            Log.e("SocialRepository", "Mevcut kullanıcı alınırken hata oluştu", e)
            null
        }
    }

    private suspend fun getUserRole(userId: String): UserRole? {
        return try {
            val roleResource = authRepository.getUserRole(userId).first()
            when (roleResource) {
                is Resource.Success -> roleResource.data.role
                else -> null
            }
        } catch (e: Exception) {
            Log.e("SocialRepository", "Get user role error", e)
            null
        }
    }

    private suspend fun getUserInfo(user: User): Pair<String, String> {
        return try {
            if (user.role == UserRole.PHYSIOTHERAPIST) {
                val profileResource = physiotherapistProfileRepository.getPhysiotherapistProfile(user.id).first()
                when (profileResource) {
                    is Resource.Success -> {
                        val profile = profileResource.data
                        "${profile.firstName} ${profile.lastName}" to (profile.profilePhotoUrl ?: "")
                    }
                    else -> "Fizyoterapist" to ""
                }
            } else {
                val profileResource = userProfileRepository.getUserProfile(user.id).first()
                when (profileResource) {
                    is Resource.Success -> {
                        val profile = profileResource.data
                        "${profile.firstName} ${profile.lastName}" to (profile.profilePhotoUrl ?: "")
                    }
                    else -> "Kullanıcı" to ""
                }
            }
        } catch (e: Exception) {
            Log.e("SocialRepository", "Get user info error", e)
            "Kullanıcı" to ""
        }
    }

    private suspend fun uploadMedia(mediaUris: List<String>): List<String> {
        val mediaUrls = mutableListOf<String>()

        for (uriString in mediaUris) {
            try {
                val uri = Uri.parse(uriString)
                val fileName = "posts/${UUID.randomUUID()}_${System.currentTimeMillis()}"
                val fileRef = storage.reference.child(fileName)

                fileRef.putFile(uri).await()
                val downloadUrl = fileRef.downloadUrl.await().toString()
                mediaUrls.add(downloadUrl)
            } catch (e: Exception) {
                Log.e("SocialRepository", "Media upload error", e)
            }
        }

        return mediaUrls
    }

    private suspend fun isPostLikedByUser(userId: String, postId: String): Boolean {
        return try {
            val likeSnapshot = likesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("postId", postId)
                .get()
                .await()

            likeSnapshot.size() > 0
        } catch (e: Exception) {
            Log.e("SocialRepository", "Post beğenilme durumu kontrol hatası", e)
            false
        }
    }
}