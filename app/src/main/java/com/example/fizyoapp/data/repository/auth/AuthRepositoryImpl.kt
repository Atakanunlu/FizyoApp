package com.example.fizyoapp.data.repository.auth

import android.util.Log
import com.example.fizyoapp.data.local.entity.auth.AuthResult
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.auth.User
import com.example.fizyoapp.domain.model.auth.UserRole
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor() : AuthRepository {
    private val auth = Firebase.auth
    private val firestore = Firebase.firestore
    private val physiotherapistCollection = firestore.collection("physiotherapist")
    private val userCollection = firestore.collection("user")

    override fun signIn(
        email: String,
        password: String,
        role: UserRole
    ): Flow<Resource<AuthResult.SignInResult>> = flow{
        try {
            emit(Resource.Loading())

            // Kullanıcı kontrolü için gerekli sorgular
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: throw Exception("Kullanıcı ID bulunamadı.")

            val collection = when (role){
                UserRole.PHYSIOTHERAPIST -> physiotherapistCollection
                UserRole.USER -> userCollection
            }

            val userDoc = collection.document(userId).get().await()
            if (!userDoc.exists()){
                auth.signOut()
                throw Exception("Yanlış rol! Lütfen doğru rolde giriş yapınız.")
            }

            val user = User(
                id = userId,
                email = email,
                role = role
            )
            // Başarılı olduğunda giriş yapacak
            emit(Resource.Success(AuthResult.SignInResult(user)))
        } catch (e: Exception){
            Log.e("FirebaseRepository","Giriş hatası", e)
            emit(Resource.Error(e.message ?: "Giriş başarısız oldu", e))
        }
    }

    override fun signUp(
        email: String,
        password: String,
        role: UserRole
    ): Flow<Resource<AuthResult.SignUpResult>> = flow {
        try {
            emit(Resource.Loading())
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: throw Exception("Kullanıcı Oluşturulamadı")

            val user = User(
                id = userId,
                email = email,
                role = role
            )

            val userData = hashMapOf(
                "email" to email,
                "role" to role.name,
                "createdAt" to FieldValue.serverTimestamp()
            )

            try {
                when(role){
                    UserRole.PHYSIOTHERAPIST -> physiotherapistCollection.document(userId).set(userData).await()
                    UserRole.USER -> userCollection.document(userId).set(userData).await()
                }
                //Kayıt olduktan sonra oturumu kapattım ki kullanıcı kendisi login ekrandan girsin
                auth.signOut()
                emit(Resource.Success(AuthResult.SignUpResult(user)))
            } catch (e: Exception){
                try {
                    auth.currentUser?.delete()?.await()
                } catch (deleteEx: Exception){
                    Log.e("FirebaseRepository", "Kullanıcı silme hatası", deleteEx)
                }
                throw e
            }
        } catch (e: Exception){
            Log.e("FirebaseRepository", "Kayıt Hatası", e)
            emit(Resource.Error(e.message ?: "Kayıt başarısız oldu", e))
        }
    }

    override fun signOut(): Flow<Resource<AuthResult.SignOutResult>> = flow{
        try {
            emit(Resource.Loading())
            auth.signOut()
            emit(Resource.Success(AuthResult.SignOutResult))
        } catch (e: Exception){
            Log.e("FirebaseRepository","Çıkış hatası", e)
            emit(Resource.Error(e.message ?: "Çıkış yapılırken hata oluştu", e))
        }
    }

    override fun getUserRole(userId: String): Flow<Resource<AuthResult.UserRoleResult>> = flow{
        try {
            emit(Resource.Loading())
            val physiotherapistDoc = physiotherapistCollection.document(userId).get().await()
            if (physiotherapistDoc.exists()){
                emit(Resource.Success(AuthResult.UserRoleResult(UserRole.PHYSIOTHERAPIST)))
                return@flow
            }

            val userDoc = userCollection.document(userId).get().await()
            if (userDoc.exists()){
                emit(Resource.Success(AuthResult.UserRoleResult(UserRole.USER)))
                return@flow
            }

            emit(Resource.Success(AuthResult.UserRoleResult(null)))
        } catch (e: Exception){
            Log.e("FirebaseRepository","Rol sorgulama hatası", e)
            emit(Resource.Error(e.message ?: "Kullanıcı rolü bulunamadı", e))
        }
    }

    override fun getCurrentUser(): Flow<Resource<AuthResult.CurrentUserResult>> = flow{
        try {
            emit(Resource.Loading())
            val firebaseUser = auth.currentUser
            if (firebaseUser != null){
                val roleFlow = getUserRole(firebaseUser.uid)
                roleFlow.collect{ resource ->
                    when(resource){
                        is Resource.Success -> {
                            val role = resource.data.role
                            if (role != null) {
                                val user = User(
                                    id = firebaseUser.uid,
                                    email = firebaseUser.email ?: "",
                                    role = role
                                )
                                emit(Resource.Success(AuthResult.CurrentUserResult(user)))
                            } else {
                                emit(Resource.Success(AuthResult.CurrentUserResult(null)))
                            }
                        }
                        is Resource.Error -> {
                            emit(Resource.Error(resource.message, resource.exception))
                        }
                        is Resource.Loading -> {}
                    }
                }
            } else{
                emit(Resource.Success(AuthResult.CurrentUserResult(null)))
            }
        } catch (e: Exception){
            Log.e("FirebaseRepository","Mevcut kullanıcı hatası", e)
            emit(Resource.Error(e.message ?: "Kullanıcı bilgileri alınamadı", e))
        }
    }

    override fun observeAuthState(): Flow<Resource<AuthResult.CurrentUserResult>> = callbackFlow {
        trySend(Resource.Loading())
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            CoroutineScope(Dispatchers.IO).launch {
                if (user != null) {
                    try {
                        val physiotherapistDoc = physiotherapistCollection.document(user.uid).get().await()
                        if (physiotherapistDoc.exists()) {
                            val userObj = User(
                                id = user.uid,
                                email = user.email ?: "",
                                role = UserRole.PHYSIOTHERAPIST
                            )
                            trySend(Resource.Success(AuthResult.CurrentUserResult(userObj)))
                        } else {
                            val userDoc = userCollection.document(user.uid).get().await()
                            if (userDoc.exists()) {
                                val userObj = User(
                                    id = user.uid,
                                    email = user.email ?: "",
                                    role = UserRole.USER
                                )
                                trySend(Resource.Success(AuthResult.CurrentUserResult(userObj)))
                            } else {
                                trySend(Resource.Success(AuthResult.CurrentUserResult(null)))
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("FirebaseRepository", "Auth state listener hatası", e)
                        trySend(Resource.Error("Kullanıcı rolü alınamadı: ${e.message}", e))
                    }
                } else {
                    // Kullanıcı giriş yapmamış
                    trySend(Resource.Success(AuthResult.CurrentUserResult(null)))
                }
            }
        }

        auth.addAuthStateListener(authStateListener)
        // Flow kapatıldığında listener'ı temizle
        awaitClose {
            auth.removeAuthStateListener(authStateListener)
        }
    }
}