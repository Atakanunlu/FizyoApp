package com.example.fizyoapp.data.repository.socialmedia

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.notification.NotificationType
import com.example.fizyoapp.domain.model.notification.SocialMediaNotification
import com.example.fizyoapp.domain.model.socialmedia.Comment
import com.example.fizyoapp.domain.model.socialmedia.Post
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class SocialMediaRepositoryImpl @Inject constructor(
    private val contentResolver: ContentResolver
) : SocialMediaRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = Firebase.storage
    private val postsCollection = firestore.collection("posts")
    private val commentsCollection = firestore.collection("comments")
    private val notificationsCollection = firestore.collection("notifications")

    override fun getAllPosts(): Flow<Resource<List<Post>>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = postsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Gönderiler alınamadı"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val posts = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(Post::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(Resource.Success(posts))
                }
            }
        awaitClose { listener.remove() }
    }

    override fun createPost(post: Post, mediaUris: List<String>): Flow<Resource<Post>> = flow {
        emit(Resource.Loading())
        try {
            val mediaUrls = mutableListOf<String>()
            val mediaTypes = mutableListOf<String>()

            for (uriString in mediaUris) {
                try {
                    val uri = Uri.parse(uriString)
                    val mimeType = getMimeType(uri)
                    val isVideo = mimeType?.startsWith("video/") ?: false
                    val fileExtension = if (isVideo) ".mp4" else ".jpg"
                    val fileName = "post_${post.userId}_${UUID.randomUUID()}$fileExtension"
                    val fileRef = storage.reference.child("post_media/$fileName")

                    fileRef.putFile(uri).await()
                    val downloadUrl = fileRef.downloadUrl.await().toString()
                    mediaUrls.add(downloadUrl)
                    mediaTypes.add(if (isVideo) "video" else "image")
                } catch (e: Exception) {
                    throw Exception("Medya yüklenirken hata oluştu: ${e.message}")
                }
            }

            val postWithMedia = post.copy(
                mediaUrls = mediaUrls,
                mediaTypes = mediaTypes
            )

            val docRef = postsCollection.add(postWithMedia).await()
            emit(Resource.Success(postWithMedia.copy(id = docRef.id)))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Gönderi paylaşılamadı", e))
        }
    }

    override fun getPostById(postId: String): Flow<Resource<Post>> = flow {
        emit(Resource.Loading())
        try {
            val postDoc = postsCollection.document(postId).get().await()
            if (postDoc.exists()) {
                val post = postDoc.toObject(Post::class.java)?.copy(id = postDoc.id)
                if (post != null) {
                    emit(Resource.Success(post))
                } else {
                    emit(Resource.Error("Gönderi dönüştürülemedi"))
                }
            } else {
                emit(Resource.Error("Gönderi bulunamadı"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Gönderi alınamadı"))
        }
    }

    override fun likePost(postId: String, userId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val postRef = postsCollection.document(postId)
            val postDoc = postRef.get().await()
            val post = postDoc.toObject(Post::class.java)

            if (post != null) {
                val likedBy = post.likedBy.toMutableList()
                if (!likedBy.contains(userId)) {
                    likedBy.add(userId)
                    postRef.update(
                        mapOf(
                            "likedBy" to likedBy,
                            "likeCount" to (post.likeCount + 1)
                        )
                    ).await()

                    if (post.userId != userId) {
                        try {
                            val userRoleDoc = firestore.collection("user_roles")
                                .document(userId)
                                .get()
                                .await()
                            val userRole = userRoleDoc.getString("role") ?: "USER"

                            val notification = SocialMediaNotification(
                                recipientId = post.userId,
                                senderId = userId,
                                senderRole = userRole,
                                type = NotificationType.LIKE,
                                contentId = postId,
                                contentText = post.content.take(50) + if (post.content.length > 50) "..." else "",
                                timestamp = Date()
                            )
                            notificationsCollection.add(notification).await()
                        } catch (e: Exception) {

                        }
                    }
                }
                emit(Resource.Success(Unit))
            } else {
                throw Exception("Gönderi bulunamadı")
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Gönderi beğenilemedi", e))
        }
    }

    override fun unlikePost(postId: String, userId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val postRef = postsCollection.document(postId)
            val postDoc = postRef.get().await()
            val post = postDoc.toObject(Post::class.java)

            if (post != null) {
                val likedBy = post.likedBy.toMutableList()
                if (likedBy.contains(userId)) {
                    likedBy.remove(userId)
                    postRef.update(
                        mapOf(
                            "likedBy" to likedBy,
                            "likeCount" to (post.likeCount - 1)
                        )
                    ).await()
                }
                emit(Resource.Success(Unit))
            } else {
                throw Exception("Gönderi bulunamadı")
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Gönderi beğenisi kaldırılamadı", e))
        }
    }

    override fun getCommentsByPostId(postId: String): Flow<Resource<List<Comment>>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = commentsCollection
            .whereEqualTo("postId", postId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Yorumlar alınamadı"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val comments = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(Comment::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(Resource.Success(comments))
                }
            }
        awaitClose { listener.remove() }
    }

    override fun addComment(comment: Comment): Flow<Resource<Comment>> = flow {
        emit(Resource.Loading())
        try {
            val commentRef = commentsCollection.add(comment).await()

            val postRef = postsCollection.document(comment.postId)
            val postDoc = postRef.get().await()
            val post = postDoc.toObject(Post::class.java)

            if (post != null) {
                postRef.update("commentCount", post.commentCount + 1).await()

                if (post.userId != comment.userId) {
                    try {
                        val notification = SocialMediaNotification(
                            recipientId = post.userId,
                            senderId = comment.userId,
                            senderRole = comment.userRole,
                            type = NotificationType.COMMENT,
                            contentId = comment.postId,
                            contentText = comment.content.take(50) + if (comment.content.length > 50) "..." else "",
                            timestamp = Date()
                        )
                        notificationsCollection.add(notification).await()
                    } catch (e: Exception) {
                    }
                }
            }

            emit(Resource.Success(comment.copy(id = commentRef.id)))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Yorum eklenemedi", e))
        }
    }

    override fun deletePost(postId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val postDoc = postsCollection.document(postId).get().await()
            val post = postDoc.toObject(Post::class.java)
            if (post != null) {
                Log.d("DeletePost", "Deleting post with ID: $postId")
                Log.d("DeletePost", "Media URLs to delete: ${post.mediaUrls}")

                for (mediaUrl in post.mediaUrls) {
                    try {
                        Log.d("DeletePost", "Attempting to delete media: $mediaUrl")

                        val storageRef = getStorageRefFromUrl(mediaUrl)
                        if (storageRef != null) {
                            try {
                                storageRef.delete().await()
                                Log.d("DeletePost", "Successfully deleted media: $mediaUrl")
                            } catch (e: Exception) {
                                Log.e("DeletePost", "Error deleting media: ${e.message}", e)
                            }
                        } else {
                            Log.w("DeletePost", "Could not get storage reference for: $mediaUrl")
                        }
                    } catch (e: Exception) {
                        Log.e("DeletePost", "Error processing media URL: ${e.message}", e)
                    }
                }

                Log.d("DeletePost", "Fetching related notifications and comments")
                val notificationsQuery = notificationsCollection
                    .whereEqualTo("contentId", postId)
                    .get()
                    .await()

                val commentsQuery = commentsCollection
                    .whereEqualTo("postId", postId)
                    .get()
                    .await()

                val batch = firestore.batch()

                for (notifDoc in notificationsQuery.documents) {
                    Log.d("DeletePost", "Adding notification to batch delete: ${notifDoc.id}")
                    batch.delete(notifDoc.reference)
                }

                for (commentDoc in commentsQuery.documents) {
                    Log.d("DeletePost", "Adding comment to batch delete: ${commentDoc.id}")
                    batch.delete(commentDoc.reference)
                }

                Log.d("DeletePost", "Adding post to batch delete: $postId")
                batch.delete(postsCollection.document(postId))

                Log.d("DeletePost", "Committing batch operation")
                batch.commit().await()
                Log.d("DeletePost", "Batch operation completed successfully")

                emit(Resource.Success(Unit))
            } else {
                throw Exception("Gönderi bulunamadı")
            }
        } catch (e: Exception) {
            Log.e("DeletePost", "Error in deletePost: ${e.message}", e)
            emit(Resource.Error(e.message ?: "Gönderi silinemedi", e))
        }
    }

    private fun getStorageRefFromUrl(url: String): StorageReference? {
        return try {
            if (url.contains("firebasestorage.googleapis.com")) {
                val storage = FirebaseStorage.getInstance()

                val startIndex = url.indexOf("/o/") + 3
                val endIndex = url.indexOf("?", startIndex)

                if (startIndex >= 3 && endIndex > startIndex) {
                    val pathPart = url.substring(startIndex, endIndex)
                    val decodedPath = java.net.URLDecoder.decode(pathPart, "UTF-8")
                    Log.d("StorageRef", "Extracted path: $decodedPath")
                    storage.reference.child(decodedPath)
                } else {
                    // Normal yöntemi dene
                    FirebaseStorage.getInstance().getReferenceFromUrl(url)
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("StorageRef", "Error getting storage reference: ${e.message}", e)
            null
        }
    }

    override fun deleteComment(commentId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val commentDoc = commentsCollection.document(commentId).get().await()
            val postId = commentDoc.getString("postId")

            if (postId != null) {
                commentsCollection.document(commentId).delete().await()

                val postRef = postsCollection.document(postId)
                val postDoc = postRef.get().await()
                val post = postDoc.toObject(Post::class.java)

                if (post != null && post.commentCount > 0) {
                    postRef.update("commentCount", post.commentCount - 1).await()
                }
            } else {
                commentsCollection.document(commentId).delete().await()
            }

            val notificationsQuery = notificationsCollection
                .whereEqualTo("contentId", commentId)
                .whereEqualTo("type", NotificationType.COMMENT.toString())
                .get()
                .await()

            for (notifDoc in notificationsQuery.documents) {
                notifDoc.reference.delete().await()
            }

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Yorum silinemedi", e))
        }
    }

    override fun updatePost(
        postId: String,
        content: String,
        existingMediaUrls: List<String>,
        existingMediaTypes: List<String>,
        newMediaUris: List<String>
    ): Flow<Resource<Post>> = flow {
        emit(Resource.Loading())
        try {
            val postDoc = postsCollection.document(postId).get().await()
            val existingPost = postDoc.toObject(Post::class.java)
                ?: throw Exception("Güncellenecek gönderi bulunamadı")

            val newMediaUrls = mutableListOf<String>()
            val newMediaTypes = mutableListOf<String>()

            for (mediaUri in newMediaUris) {
                try {
                    val uri = Uri.parse(mediaUri)
                    val mimeType = getMimeType(uri)
                    val isVideo = mimeType?.startsWith("video/") ?: false
                    val extension = if (isVideo) ".mp4" else ".jpg"
                    val fileName = "post_${existingPost.userId}_${UUID.randomUUID()}$extension"
                    val fileRef = storage.reference.child("post_media/$fileName")

                    fileRef.putFile(uri).await()
                    val downloadUrl = fileRef.downloadUrl.await().toString()

                    newMediaUrls.add(downloadUrl)
                    newMediaTypes.add(if (isVideo) "video" else "image")
                } catch (e: Exception) {
                    throw Exception("Medya yüklenirken hata oluştu: ${e.message}")
                }
            }

            val originalMediaUrls = existingPost.mediaUrls
            val deletedMediaUrls = originalMediaUrls.filter { !existingMediaUrls.contains(it) }

            for (mediaUrl in deletedMediaUrls) {
                try {
                    val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(mediaUrl)
                    storageRef.delete().await()
                } catch (e: Exception) {
                }
            }

            val allMediaUrls = existingMediaUrls + newMediaUrls
            val allMediaTypes = existingMediaTypes + newMediaTypes

            val updatedPost = existingPost.copy(
                content = content,
                mediaUrls = allMediaUrls,
                mediaTypes = allMediaTypes
            )

            postsCollection.document(postId).set(updatedPost).await()
            emit(Resource.Success(updatedPost))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Gönderi güncellenemedi", e))
        }
    }

    private fun getMimeType(uri: Uri): String? {
        return if (uri.scheme == "content") {
            contentResolver.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.lowercase())
        }
    }
}