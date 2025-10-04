package com.tomer.myflix.common

import android.content.Context
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tomer.myflix.data.local.room.DBMovies
import com.tomer.myflix.data.remote.repo.RepoRemoteRetro
import com.tomer.myflix.data.remote.retro.ApiFlickServer
import com.tomer.myflix.data.remote.retro.ApiOmDBServer
import com.tomer.myflix.data.remote.retro.utils.RetryInterceptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val db = Room.databaseBuilder(
        appContext,
        DBMovies::class.java,
        "MOVIES_DB"
    ).allowMainThreadQueries()
        .build()

    private val remote = Retrofit.Builder().baseUrl(linkServer)
        .client(
            OkHttpClient.Builder()
                .addInterceptor(RetryInterceptor(maxRetries = 3))
                .build()
        )
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val repoRemote = RepoRemoteRetro(
        appContext,
        remote.create(ApiFlickServer::class.java),
        remote.create(ApiOmDBServer::class.java)
    )

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val remo = repoRemote.getFeaturedList()
        if (remo.isNotEmpty()) {
            db.featuredDao().deleteAllFeatured()
            db.featuredDao().insertAllFeatured(remo)
            Result.success()
        } else Result.retry()
    }
}
