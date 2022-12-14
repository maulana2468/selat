package com.ppm.selat.core.data.source.local

import android.content.SharedPreferences
import com.ppm.selat.core.data.Resource
import com.ppm.selat.core.domain.model.UserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.Exception

class UserLocalDataSource(private val sharedPreferences: SharedPreferences) {

    companion object {
        private const val DATA_USER = "DATA_USER"
        private const val PASSWD = "PASSWD"
    }

    private val userData = UserData(
        id = "NULL",
        name = "NULL",
        email = "NULL",
        photoUrl = "NULL",
        phone = "String",
        placeDateOfBirth = "NULL",
        address = "NULL",
        job = "NULL"
    )

    private val userDataStream = MutableStateFlow(userData)

    init {
        val data = sharedPreferences.getString(DATA_USER, null)
        if (data != null) {
            userDataStream.value = Json.decodeFromString(UserData.serializer(), data)
        } else {
            userDataStream.value = userData
        }
    }

    fun getDataStream(): MutableStateFlow<UserData> = userDataStream

    fun getSingleUserData(): UserData {
        return userDataStream.value
    }

    suspend fun getPassword(): Flow<Resource<String>> {
        return flow {
            try {
                val passwd = sharedPreferences.getString(PASSWD, null)!!
                emit(Resource.Success(passwd))
            } catch (e: Exception) {
                emit(Resource.Error(e.message.toString()))
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun savePassword(string: String) : Flow<Resource<Boolean>> {
        return flow {
            try {
                val editor = sharedPreferences.edit()
                editor.putString(PASSWD, string)
                editor.apply()
                emit(Resource.Success(true))
            } catch (e: Exception) {
                emit(Resource.Error(e.message.toString()))
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun saveUserData(newUserData: UserData): Flow<Resource<Boolean>> {
        return flow {
            try {
                val editor = sharedPreferences.edit()
                editor.putString(DATA_USER, Json.encodeToString(UserData.serializer(), newUserData))
                editor.apply()
                userDataStream.value = newUserData
                emit(Resource.Success(true))
            } catch (e: Exception) {
                emit(Resource.Error(e.message.toString()))
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun removeUserData(): Flow<Resource<Boolean>> {
        return flow {
            try {
                val editor = sharedPreferences.edit()
                editor.clear()
                editor.apply()
                emit(Resource.Success(true))
            } catch (e: Exception) {
                emit(Resource.Error(e.message.toString()))
            }
        }.flowOn(Dispatchers.IO)
    }
}