// data/repository/socialmedia/SocialMediaRepositoryImpl.kt
package com.example.fizyoapp.data.repository.socialmedia

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.socialmedia.Comment
import com.example.fizyoapp.domain.model.socialmedia.Post
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class SocialMediaRepositoryImpl @Inject constructor(
    private val contentResolver: ContentResolver
) : SocialMediaRepository {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = Firebase.storage
    private val postsCollection = firestore.collection("posts")
    private val commentsCollection = firestore.collection("comments")

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
                            val post = doc.toObject(Post::class.java)?.copy(id = doc.id)
                            post
                        } catch (e: Exception) {
                            Log.e("SocialMediaRepo", "Post dönüştürme hatası", e)
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
            Log.d("SocialMediaRepo", "Gönderi oluşturma başlıyor: userId=${post.userId}, mediaCount=${mediaUris.size}")

            // Medya dosyalarını yükle
            val mediaUrls = mutableListOf<String>()
            val mediaTypes = mutableListOf<String>()

            for (uriString in mediaUris) {
                try {
                    val uri = Uri.parse(uriString)
                    val mimeType = getMimeType(uri)
                    val isVideo = mimeType?.startsWith("video/") ?: false

                    // Dosya uzantısını belirle
                    val fileExtension = if (isVideo) ".mp4" else ".jpg"

                    // Dosya adını rastgele oluştur
                    val fileName = "post_${post.userId}_${UUID.randomUUID()}$fileExtension"
                    val fileRef = storage.reference.child("post_media/$fileName")

                    Log.d("SocialMediaRepo", "Medya yükleniyor: $fileName (${if (isVideo) "video" else "image"})")

                    // Yükleme işlemi
                    fileRef.putFile(uri).await()
                    val downloadUrl = fileRef.downloadUrl.await().toString()
                    mediaUrls.add(downloadUrl)
                    mediaTypes.add(if (isVideo) "video" else "image")

                    Log.d("SocialMediaRepo", "Medya yüklendi: $downloadUrl")
                } catch (e: Exception) {
                    Log.e("SocialMediaRepo", "Medya yükleme hatası: ${e.message}", e)
                    throw Exception("Medya yüklenirken hata oluştu: ${e.message}")
                }
            }

            // Post objesi oluştur
            val postWithMedia = post.copy(
                mediaUrls = mediaUrls,
                mediaTypes = mediaTypes
            )

            // Firestore'a kaydet
            val docRef = postsCollection.add(postWithMedia).await()

            // Başarılı sonucu döndür
            Log.d("SocialMediaRepo", "Gönderi başarıyla oluşturuldu: ${docRef.id}")
            emit(Resource.Success(postWithMedia.copy(id = docRef.id)))

        } catch (e: Exception) {
            Log.e("SocialMediaRepo", "Post oluşturma hatası", e)
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
            Log.e("SocialMediaRepo", "Post getirme hatası", e)
            emit(Resource.Error(e.message ?: "Gönderi alınamadı"))
        }
    }

    override fun likePost(postId: String, userId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            Log.d("SocialMediaRepo", "Beğeni işlemi başlatılıyor: postId=$postId, userId=$userId")
            val postRef = postsCollection.document(postId)

            // Transaction ile beğeni işlemini gerçekleştir
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                val post = snapshot.toObject(Post::class.java)

                if (post != null) {
                    // Beğeni listesini al
                    val likedBy = post.likedBy.toMutableList()

                    // Kullanıcı zaten beğenmiş mi kontrol et
                    if (!likedBy.contains(userId)) {
                        // Beğeni ekle
                        likedBy.add(userId)
                        transaction.update(postRef, "likedBy", likedBy)
                        transaction.update(postRef, "likeCount", post.likeCount + 1)
                        Log.d("SocialMediaRepo", "Beğeni eklendi")
                    } else {
                        Log.d("SocialMediaRepo", "Kullanıcı zaten gönderiyi beğenmiş")
                    }
                } else {
                    Log.e("SocialMediaRepo", "Post bulunamadı")
                    throw Exception("Gönderi bulunamadı")
                }
            }.await()

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            Log.e("SocialMediaRepo", "Beğeni hatası: ${e.message}", e)
            emit(Resource.Error(e.message ?: "Gönderi beğenilemedi", e))
        }
    }

    override fun unlikePost(postId: String, userId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            Log.d("SocialMediaRepo", "Beğeni kaldırma işlemi başlatılıyor: postId=$postId, userId=$userId")
            val postRef = postsCollection.document(postId)

            // Transaction ile beğeni kaldırma işlemini gerçekleştir
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                val post = snapshot.toObject(Post::class.java)

                if (post != null) {
                    // Beğeni listesini al
                    val likedBy = post.likedBy.toMutableList()

                    // Kullanıcının beğenisi var mı kontrol et
                    if (likedBy.contains(userId)) {
                        // Beğeniyi kaldır
                        likedBy.remove(userId)
                        transaction.update(postRef, "likedBy", likedBy)
                        transaction.update(postRef, "likeCount", post.likeCount - 1)
                        Log.d("SocialMediaRepo", "Beğeni kaldırıldı")
                    } else {
                        Log.d("SocialMediaRepo", "Kullanıcı gönderiyi beğenmemiş")
                    }
                } else {
                    Log.e("SocialMediaRepo", "Post bulunamadı")
                    throw Exception("Gönderi bulunamadı")
                }
            }.await()

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            Log.e("SocialMediaRepo", "Beğeni kaldırma hatası: ${e.message}", e)
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
                            val comment = doc.toObject(Comment::class.java)?.copy(id = doc.id)
                            comment
                        } catch (e: Exception) {
                            Log.e("SocialMediaRepo", "Yorum dönüştürme hatası", e)
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
            Log.d("SocialMediaRepo", "Yorum ekleme başlıyor: postId=${comment.postId}, userId=${comment.userId}")

            // Yorumu ekle
            val commentRef = commentsCollection.add(comment).await()

            // Post'un yorum sayısını güncelle
            val postRef = postsCollection.document(comment.postId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                val currentCommentCount = snapshot.getLong("commentCount") ?: 0
                transaction.update(postRef, "commentCount", currentCommentCount + 1)
            }.await()

            Log.d("SocialMediaRepo", "Yorum eklendi ve gönderi yorum sayısı güncellendi")
            emit(Resource.Success(comment.copy(id = commentRef.id)))
        } catch (e: Exception) {
            Log.e("SocialMediaRepo", "Yorum ekleme hatası: ${e.message}", e)
            emit(Resource.Error(e.message ?: "Yorum eklenemedi", e))
        }
    }

    override fun deletePost(postId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            Log.d("SocialMediaRepo", "Gönderi silme başlatıldı: postId=$postId")

            // Gönderiyi al
            val postDoc = postsCollection.document(postId).get().await()
            val post = postDoc.toObject(Post::class.java)

            if (post != null) {
                // Medya dosyalarını silme
                for (mediaUrl in post.mediaUrls) {
                    try {
                        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(mediaUrl)
                        storageRef.delete().await()
                        Log.d("SocialMediaRepo", "Medya dosyası silindi: $mediaUrl")
                    } catch (e: Exception) {
                        Log.e("SocialMediaRepo", "Medya dosyası silinemedi: $mediaUrl", e)
                        // Bu hatayı yakala ama silme işlemine devam et
                    }
                }

                // Gönderiye ait yorumları silme
                val commentsQuery = commentsCollection.whereEqualTo("postId", postId).get().await()
                val batch = firestore.batch()

                for (commentDoc in commentsQuery.documents) {
                    batch.delete(commentDoc.reference)
                }

                // Gönderiyi silme
                batch.delete(postsCollection.document(postId))

                // Batch işlemini uygula
                batch.commit().await()

                Log.d("SocialMediaRepo", "Gönderi ve ilişkili veriler başarıyla silindi")
                emit(Resource.Success(Unit))
            } else {
                throw Exception("Gönderi bulunamadı")
            }
        } catch (e: Exception) {
            Log.e("SocialMediaRepo", "Gönderi silme hatası: ${e.message}", e)
            emit(Resource.Error(e.message ?: "Gönderi silinemedi", e))
        }
    }

    override fun deleteComment(commentId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            // Yorumun bağlı olduğu post ID'sini al
            val commentDoc = commentsCollection.document(commentId).get().await()
            val postId = commentDoc.getString("postId")
            if (postId != null) {
                // Yorumu sil
                commentsCollection.document(commentId).delete().await()
                // Post'un yorum sayısını güncelle
                val postRef = postsCollection.document(postId)
                firestore.runTransaction { transaction ->
                    val snapshot = transaction.get(postRef)
                    val currentCommentCount = snapshot.getLong("commentCount") ?: 0
                    if (currentCommentCount > 0) {
                        transaction.update(postRef, "commentCount", currentCommentCount - 1)
                    }
                }.await()
                Log.d("SocialMediaRepo", "Yorum silindi ve gönderi yorum sayısı güncellendi")
            } else {
                commentsCollection.document(commentId).delete().await()
                Log.d("SocialMediaRepo", "Yorum silindi, bağlı gönderi bulunamadı")
            }
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            Log.e("SocialMediaRepo", "Yorum silme hatası", e)
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
            Log.d("SocialMediaRepo", "Gönderi güncelleme başlatıldı: postId=$postId")

            // Mevcut gönderiyi al
            val postDoc = postsCollection.document(postId).get().await()
            val existingPost = postDoc.toObject(Post::class.java)
                ?: throw Exception("Güncellenecek gönderi bulunamadı")

            // Yeni medya dosyalarını yükle
            val newMediaUrls = mutableListOf<String>()
            val newMediaTypes = mutableListOf<String>()

            // Yeni eklenen medya dosyalarını işle
            for (mediaUri in newMediaUris) {
                try {
                    val uri = Uri.parse(mediaUri)
                    val contentResolver = FirebaseApp.getInstance().applicationContext.contentResolver

                    // MIME tipini belirle (varsayılan image/jpeg)
                    val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
                    val isVideo = mimeType.startsWith("video/")

                    // Uygun uzantıyı seç
                    val extension = if (isVideo) ".mp4" else ".jpg"

                    // Dosya adı oluştur
                    val fileName = "post_${existingPost.userId}_${UUID.randomUUID()}$extension"
                    val fileRef = storage.reference.child("post_media/$fileName")

                    // Dosyayı yükle
                    fileRef.putFile(uri).await()
                    val downloadUrl = fileRef.downloadUrl.await().toString()

                    // Listeye ekle
                    newMediaUrls.add(downloadUrl)
                    newMediaTypes.add(if (isVideo) "video" else "image")

                    Log.d("SocialMediaRepo", "Yeni medya yüklendi: $downloadUrl, Tip: ${if (isVideo) "video" else "image"}")
                } catch (e: Exception) {
                    Log.e("SocialMediaRepo", "Medya yükleme hatası: ${e.message}", e)
                    throw Exception("Medya yüklenirken hata oluştu: ${e.message}")
                }
            }

            // Silinen medya dosyalarını bul ve storage'dan kaldır
            val originalMediaUrls = existingPost.mediaUrls
            val deletedMediaUrls = originalMediaUrls.filter { !existingMediaUrls.contains(it) }

            for (mediaUrl in deletedMediaUrls) {
                try {
                    val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(mediaUrl)
                    storageRef.delete().await()
                    Log.d("SocialMediaRepo", "Silinen medya: $mediaUrl")
                } catch (e: Exception) {
                    Log.e("SocialMediaRepo", "Medya silinirken hata: $mediaUrl", e)
                    // Hata olsa bile devam et
                }
            }

            // Tüm medya URL ve tiplerini birleştir
            val allMediaUrls = existingMediaUrls + newMediaUrls
            val allMediaTypes = existingMediaTypes + newMediaTypes

            // Gönderiyi güncelle
            val updatedPost = existingPost.copy(
                content = content,
                mediaUrls = allMediaUrls,
                mediaTypes = allMediaTypes
            )

            // Firestore'a kaydet
            postsCollection.document(postId).set(updatedPost).await()

            Log.d("SocialMediaRepo", "Gönderi başarıyla güncellendi")
            emit(Resource.Success(updatedPost))
        } catch (e: Exception) {
            Log.e("SocialMediaRepo", "Gönderi güncelleme hatası: ${e.message}", e)
            emit(Resource.Error(e.message ?: "Gönderi güncellenemedi", e))
        }
    }

    // MIME tipini belirlemek için yardımcı fonksiyon
    private fun getMimeType(uri: Uri): String? {
        return if (uri.scheme == "content") {
            contentResolver.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.lowercase())
        }
    }


}