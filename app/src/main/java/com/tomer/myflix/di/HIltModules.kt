package com.tomer.myflix.di


import com.google.gson.Gson
import com.tomer.myflix.data.remote.retro.Api
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
object HIltModules {
    @Provides
    @Singleton
    fun provideBaseUrl() = "http://34.56.236.111:9696/flick/"


    private fun provideRetrofitClient(baseUrl: String): Retrofit {
        return Retrofit.Builder().baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiClient(baseUrl: String): Api = provideRetrofitClient(baseUrl).create(Api::class.java)

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

}