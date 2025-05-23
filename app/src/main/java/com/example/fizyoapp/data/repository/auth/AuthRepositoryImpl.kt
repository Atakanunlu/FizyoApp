package com.example.fizyoapp.data.repository.auth

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
    ): Flow<Resource<AuthResult.SignInResult>> = flow {
        try {
            emit(Resource.Loading())

            val result = auth.signInWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: throw Exception("Kullanıcı ID bulunamadı.")

            val collection = when (role) {
                UserRole.PHYSIOTHERAPIST -> physiotherapistCollection
                UserRole.USER -> userCollection
            }

            val userDoc = collection.document(userId).get().await()
            if (!userDoc.exists()) {
                auth.signOut()
                throw Exception("Yanlış rol! Lütfen doğru rolde giriş yapınız.")
            }

            val user = User(
                id = userId,
                email = email,
                role = role
            )

            emit(Resource.Success(AuthResult.SignInResult(user)))
        } catch (e: Exception) {
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
                "profileCompleted" to false,
                "createdAt" to FieldValue.serverTimestamp()
            )

            try {
                when (role) {
                    UserRole.PHYSIOTHERAPIST -> physiotherapistCollection.document(userId).set(userData).await()
                    UserRole.USER -> userCollection.document(userId).set(userData).await()
                }

                emit(Resource.Success(AuthResult.SignUpResult(user)))
            } catch (e: Exception) {
                try {
                    auth.currentUser?.delete()?.await()
                } catch (deleteEx: Exception) {
                }
                throw e
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Kayıt başarısız oldu", e))
        }
    }

    override fun signOut(): Flow<Resource<AuthResult.SignOutResult>> = flow {
        try {
            emit(Resource.Loading())
            auth.signOut()
            emit(Resource.Success(AuthResult.SignOutResult))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Çıkış yapılırken hata oluştu", e))
        }
    }

    override fun getUserRole(userId: String): Flow<Resource<AuthResult.UserRoleResult>> = flow {
        try {
            emit(Resource.Loading())
            val physiotherapistDoc = physiotherapistCollection.document(userId).get().await()
            if (physiotherapistDoc.exists()) {
                emit(Resource.Success(AuthResult.UserRoleResult(UserRole.PHYSIOTHERAPIST)))
                return@flow
            }

            val userDoc = userCollection.document(userId).get().await()
            if (userDoc.exists()) {
                emit(Resource.Success(AuthResult.UserRoleResult(UserRole.USER)))
                return@flow
            }

            emit(Resource.Success(AuthResult.UserRoleResult(null)))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Kullanıcı rolü bulunamadı", e))
        }
    }

    override fun getCurrentUser(): Flow<Resource<AuthResult.CurrentUserResult>> = flow {
        try {
            emit(Resource.Loading())
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                val roleFlow = getUserRole(firebaseUser.uid)
                roleFlow.collect { resource ->
                    when (resource) {
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
            } else {
                emit(Resource.Success(AuthResult.CurrentUserResult(null)))
            }
        } catch (e: Exception) {
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
                        trySend(Resource.Error("Kullanıcı rolü alınamadı: ${e.message}", e))
                    }
                } else {
                    trySend(Resource.Success(AuthResult.CurrentUserResult(null)))
                }
            }
        }

        auth.addAuthStateListener(authStateListener)

        awaitClose {
            auth.removeAuthStateListener(authStateListener)
        }
    }

    override fun sendEmailVerification(): Flow<Resource<AuthResult.EmailVerificationResult>> = flow {
        try {
            emit(Resource.Loading())
            val user = auth.currentUser ?: throw Exception("Kullanıcı bulunamadı.")
            user.sendEmailVerification().await()
            emit(Resource.Success(AuthResult.EmailVerificationResult(true)))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "E-posta doğrulama e-postası gönderilemedi", e))
        }
    }

    override fun checkEmailVerified(): Flow<Resource<AuthResult.EmailVerifiedResult>> = flow {
        try {
            emit(Resource.Loading())
            auth.currentUser?.reload()?.await()
            val user = auth.currentUser ?: throw Exception("Kullanıcı bulunamadı.")
            emit(Resource.Success(AuthResult.EmailVerifiedResult(user.isEmailVerified)))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "E-posta doğrulama durumu kontrol edilemedi", e))
        }
    }

    override fun sendPasswordResetEmail(email: String): Flow<Resource<AuthResult.PasswordResetEmailResult>> = flow {
        try {
            emit(Resource.Loading())
            auth.sendPasswordResetEmail(email).await()
            emit(Resource.Success(AuthResult.PasswordResetEmailResult(true)))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Şifre sıfırlama e-postası gönderilemedi", e))
        }
    }

    override fun verifyPasswordResetCode(code: String): Flow<Resource<AuthResult.VerifyResetCodeResult>> = flow {
        try {
            emit(Resource.Loading())
            auth.verifyPasswordResetCode(code).await()
            emit(Resource.Success(AuthResult.VerifyResetCodeResult(true)))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Şifre sıfırlama kodu doğrulanamadı", e))
        }
    }

    override fun resetPassword(code: String, newPassword: String): Flow<Resource<AuthResult.ResetPasswordResult>> = flow {
        try {
            emit(Resource.Loading())
            auth.confirmPasswordReset(code, newPassword).await()
            emit(Resource.Success(AuthResult.ResetPasswordResult(true)))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Şifre sıfırlanamadı", e))
        }
    }
}