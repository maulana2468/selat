package com.ppm.selat.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.ppm.selat.core.domain.usecase.AuthUseCase
import com.ppm.selat.core.domain.usecase.CarUseCase

class HomeViewModel(private val authUseCase: AuthUseCase, private val carUseCase: CarUseCase) :
    ViewModel() {

    val userDataStream = authUseCase.getUserStream().asLiveData()
    fun getAllCars() = carUseCase.getAllCars().asLiveData()
}