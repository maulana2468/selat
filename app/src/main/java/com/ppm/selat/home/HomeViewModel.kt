package com.ppm.selat.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.ppm.selat.core.domain.usecase.AuthUseCase

class HomeViewModel(private val authUseCase: AuthUseCase) : ViewModel() {
    val userDataStream = authUseCase.getUserStream().asLiveData()
}