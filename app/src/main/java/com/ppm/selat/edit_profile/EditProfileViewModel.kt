package com.ppm.selat.edit_profile

import android.net.Uri
import android.provider.ContactsContract.CommonDataKinds.Email
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.ppm.selat.core.data.Resource
import com.ppm.selat.core.domain.model.UserData
import com.ppm.selat.core.domain.usecase.AuthUseCase
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.*

class EditProfileViewModel(private val authUseCase: AuthUseCase) : ViewModel() {
    lateinit var nameInit: String
    var nameFlow = MutableStateFlow("")
    lateinit var emailInit: String
    var emailFlow = MutableStateFlow("")
    lateinit var phoneInit: String
    var phoneFlow = MutableStateFlow("")

    var photoIsChanged = MutableStateFlow(false)
    private var newPhotoUrl = MutableStateFlow("")

    fun checkNameIsChanged(): Boolean = nameInit != nameFlow.value
    fun checkEmailIsChanged(): Boolean = emailInit != emailFlow.value
    fun checkPhoneIsChanged(): Boolean = phoneInit != phoneFlow.value

    val userDataStream = authUseCase.getUserStream()
    fun saveNewName() = authUseCase.updateName(nameFlow.value).asLiveData()
    fun saveNewEmail() = authUseCase.updateEmail(emailFlow.value).asLiveData()

    fun saveNewPhone() = authUseCase.updatePhone(phoneFlow.value).asLiveData()
}
