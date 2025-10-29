package com.tomer.myflix.di

import android.app.Application
import androidx.media3.common.util.UnstableApi
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.tomer.myflix.common.ApiWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

@UnstableApi
@HiltAndroidApp
class HiltApp : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(.2)
                    .build()
            }.diskCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        setupPeriodicApiWork()
    }

    private fun setupPeriodicApiWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<ApiWorker>(
            24, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .addTag("fetch_featured")
            .build()

        WorkManager.getInstance(applicationContext)
            .enqueueUniquePeriodicWork(
                "refresh_featured",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
    }
}