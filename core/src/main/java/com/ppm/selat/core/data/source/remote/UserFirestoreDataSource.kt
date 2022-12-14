package com.ppm.selat.core.data.source.remote

import com.google.firebase.firestore.*
import com.ppm.selat.core.data.source.remote.network.FirebaseResponse
import com.ppm.selat.core.domain.model.LoginData
import com.ppm.selat.core.domain.model.RegisterData
import com.ppm.selat.core.domain.model.UserData
import com.ppm.selat.core.utils.TypeDataEdit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await


class UserFirestoreDataSource(private val firestore: FirebaseFirestore) {
    private lateinit var userDb: CollectionReference
    private lateinit var pinDb: CollectionReference
    private lateinit var historyLoginDb: CollectionReference

    private fun initFirestore() {
        userDb = firestore.collection("users")
        pinDb = firestore.collection("PIN")
        historyLoginDb = firestore.collection("history_login")
    }

    suspend fun getUserDataFromFirestore(
        uid: String, loginData: LoginData
    ): Flow<FirebaseResponse<DocumentSnapshot>> {
        return flow {
            try {
                val loginDataUser = mapOf(
                    System.currentTimeMillis().toString() to listOf(
                        loginData.lastLogin, loginData.deviceData
                    )
                )
                val historyLoginUser = historyLoginDb.document(uid)
                val user = userDb.document(uid).get().await()
                if (historyLoginUser.get().await().exists()) {
                    historyLoginUser.update(loginDataUser).await()
                } else {
                    historyLoginUser.set(loginDataUser).await()
                }
                emit(FirebaseResponse.Success(user))
            } catch (e: FirebaseFirestoreException) {
                emit(FirebaseResponse.Error(e.message.toString()))
            }
        }
    }

    suspend fun createUserDataToFirestore(
        user: UserData, registerData: RegisterData,
    ): Flow<FirebaseResponse<Boolean>> {
        return flow {
            val id = user.id!!
            try {
                val userData = mapOf(
                    "name" to user.name,
                    "email" to user.email,
                    "photoUrl" to user.photoUrl,
                    "phone" to user.phone,
                    "pdob" to user.placeDateOfBirth,
                    "job" to user.job,
                    "address" to user.address
                )
                userDb.document(id).set(userData).await()
                pinDb.document(id).set(mapOf("PIN" to registerData.PIN)).await()
                emit(FirebaseResponse.Success(true))
            } catch (e: FirebaseFirestoreException) {
                emit(FirebaseResponse.Error(e.message.toString()))
            }
        }
    }

    suspend fun updateProfile(
        typeDataEdit: TypeDataEdit, dataChange: String, uid: String
    ): Flow<FirebaseResponse<Boolean>> {
        return flow {
            try {
                lateinit var dataMap: Map<String, String>
                when (typeDataEdit) {
                    TypeDataEdit.NAME -> {
                        dataMap = mapOf(
                            "name" to dataChange,
                        )
                    }
                    TypeDataEdit.PDOB -> {
                        dataMap = mapOf(
                            "pdob" to dataChange,
                        )
                    }
                    TypeDataEdit.EMAIL -> {
                        dataMap = mapOf(
                            "email" to dataChange,
                        )
                    }
                    TypeDataEdit.PHONE -> {
                        dataMap = mapOf(
                            "phone" to dataChange,
                        )
                    }
                    TypeDataEdit.JOB -> {
                        dataMap = mapOf(
                            "job" to dataChange,
                        )
                    }
                    TypeDataEdit.ADDRESS -> {
                        dataMap = mapOf(
                            "address" to dataChange,
                        )
                    }
                }
                if (typeDataEdit == TypeDataEdit.EMAIL) {

                }
                userDb.document(uid).update(dataMap).await()
                emit(FirebaseResponse.Success(true))
            } catch (e: FirebaseFirestoreException) {
                emit(FirebaseResponse.Error(e.message.toString()))
            }
        }
    }

    suspend fun updatePhoto(photoUrl: String, uid: String): Flow<FirebaseResponse<Boolean>> {
        return flow {
            try {
                val nameData = mapOf(
                    "photoUrl" to photoUrl,
                )
                userDb.document(uid).update(nameData).await()
                emit(FirebaseResponse.Success(true))
            } catch (e: FirebaseFirestoreException) {
                emit(FirebaseResponse.Error(e.message.toString()))
            }
        }
    }

    suspend fun getPIN(uid: String): Flow<FirebaseResponse<String>> {
        return flow {
            try {
                val result = pinDb.document(uid).get().await()
                emit(FirebaseResponse.Success(result["PIN"].toString()))
            } catch (e: FirebaseFirestoreException) {
                emit(FirebaseResponse.Error(e.message.toString()))
            }
        }
    }

    suspend fun setPIN(PIN: String, uid: String): Flow<FirebaseResponse<Boolean>> {
        return flow {
            try {
                pinDb.document(uid).set(mapOf("PIN" to PIN)).await()
                emit(FirebaseResponse.Success(true))
            } catch (e: FirebaseFirestoreException) {
                emit(FirebaseResponse.Error(e.message.toString()))
            }
        }
    }

    suspend fun getHistoryLogin(uid: String): Flow<FirebaseResponse<DocumentSnapshot>> {
        return flow {
            try {
                val result = historyLoginDb.document(uid).get().await()
                emit(FirebaseResponse.Success(result))
            } catch (e: FirebaseFirestoreException) {
                emit(FirebaseResponse.Error(e.message.toString()))
            }
        }
    }

    fun disablePersistence(): Boolean {
        val settings = FirebaseFirestoreSettings.Builder().setPersistenceEnabled(false).build()
        firestore.firestoreSettings = settings
        initFirestore()
        return true
    }
}