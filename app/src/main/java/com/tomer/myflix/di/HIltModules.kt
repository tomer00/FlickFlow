package com.tomer.myflix.di


import android.content.Context
import androidx.room.Room
import com.tomer.myflix.common.linkOmDb
import com.tomer.myflix.common.linkServer
import com.tomer.myflix.data.local.room.DBMovies
import com.tomer.myflix.data.local.room.DaoFeaturedCollection
import com.tomer.myflix.data.local.room.DaoPlaying
import com.tomer.myflix.data.local.room.DaoSeries
import com.tomer.myflix.data.local.room.DaoSettings
import com.tomer.myflix.data.local.room.DaoSuggestions
import com.tomer.myflix.data.remote.retro.ApiFlickServer
import com.tomer.myflix.data.remote.retro.ApiOmDBServer
import com.tomer.myflix.data.remote.retro.utils.RetryInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
object HIltModules {

    private fun provideRetrofitClient(link: String): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(RetryInterceptor(maxRetries = 3))
            .build()
        return Retrofit.Builder().baseUrl(link)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiClient(): ApiFlickServer =
        provideRetrofitClient(linkServer).create(ApiFlickServer::class.java)

    @Provides
    @Singleton
    fun provideOMDBClient(): ApiOmDBServer =
        provideRetrofitClient(linkOmDb).create(ApiOmDBServer::class.java)

    @Provides
    @Singleton
    fun providesMoviesDB(@ApplicationContext appContext: Context): DBMovies {
        return Room.databaseBuilder(
            appContext,
            DBMovies::class.java,
            "MOVIES_DB"
        ).allowMainThreadQueries()
            .build()
    }

    @Provides
    @Singleton
    fun provideDaoSeries(db: DBMovies): DaoSeries = db.seriesDao()

    @Provides
    @Singleton
    fun provideDaoPlaying(db: DBMovies): DaoPlaying = db.playingDao()

    @Provides
    @Singleton
    fun provideDaoSettings(db: DBMovies): DaoSettings = db.settingsDao()

    @Provides
    @Singleton
    fun provideDaoSuggestions(db: DBMovies): DaoSuggestions = db.suggestionDao()

    @Provides
    @Singleton
    fun provideDaoFeatured(db: DBMovies): DaoFeaturedCollection = db.featuredDao()

}