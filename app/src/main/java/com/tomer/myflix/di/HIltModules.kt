package com.tomer.myflix.di


import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class HIltModules {


    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()


}