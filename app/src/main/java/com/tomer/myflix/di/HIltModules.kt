package com.tomer.myflix.di

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class HIltModules {


    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()


}