package com.tomer.myflix.di

import com.tomer.myflix.data.local.repo.RepoEpisode
import com.tomer.myflix.data.local.repo.RepoEpisodeImpl
import com.tomer.myflix.data.local.repo.RepoFeatured
import com.tomer.myflix.data.local.repo.RepoFeaturedImpl
import com.tomer.myflix.data.local.repo.RepoMovies
import com.tomer.myflix.data.local.repo.RepoMoviesImpl
import com.tomer.myflix.data.local.repo.RepoSeries
import com.tomer.myflix.data.local.repo.RepoSeriesImpl
import com.tomer.myflix.data.local.repo.RepoSettings
import com.tomer.myflix.data.local.repo.RepoSettingsRoom
import com.tomer.myflix.data.local.repo.RepoSuggestions
import com.tomer.myflix.data.local.repo.RepoSuggestionsImpl
import com.tomer.myflix.data.remote.repo.RepoRemote
import com.tomer.myflix.data.remote.repo.RepoRemoteRetro
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
abstract class RepoModule {

    @Binds
    abstract fun provideRepoMovies(repoMoviesImpl: RepoMoviesImpl): RepoMovies

    @Binds
    abstract fun provideRepoEpisode(repoEpisodeImpl: RepoEpisodeImpl): RepoEpisode

    @Binds
    abstract fun provideRepoSeries(repoSeriesImpl: RepoSeriesImpl): RepoSeries

    @Binds
    abstract fun provideRepoRemote(repoRemoteImpl: RepoRemoteRetro): RepoRemote

    @Binds
    abstract fun provideRepoSettings(repoSettingsImpl: RepoSettingsRoom): RepoSettings

    @Binds
    abstract fun provideRepoFeatured(repoFeaturedImpl: RepoFeaturedImpl): RepoFeatured

    @Binds
    abstract fun provideRepoSuggestion(repoSuggestions: RepoSuggestionsImpl): RepoSuggestions

}