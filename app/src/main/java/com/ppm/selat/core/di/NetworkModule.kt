package com.ppm.selat.core.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides

@Module
class NetworkModule {
    @Provides
    fun provideAuthInstance(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
}