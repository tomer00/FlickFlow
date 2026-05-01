package com.tomer.myflix.presentation.screens.homescreen

import android.content.Context
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.pager.PagerState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomer.myflix.data.local.models.ModelFeatured
import com.tomer.myflix.data.local.models.ModelLastPlayed
import com.tomer.myflix.data.local.repo.RepoFeatured
import com.tomer.myflix.data.local.repo.RepoMovies
import com.tomer.myflix.data.local.repo.RepoSeries
import com.tomer.myflix.data.remote.retro.ApiFlickServer
import com.tomer.myflix.presentation.ui.models.PlayableItemModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @param:ApplicationContext private val appCon: Context,
    private val api: ApiFlickServer,
    private val repoMovies: RepoMovies,
    private val repoSeries: RepoSeries,
    private val repoFeatured: RepoFeatured
) : ViewModel() {

    val titlePagerState = PagerState { featuredList.value.size }
    val posterPagerState = PagerState { featuredList.value.size }
    val mainContScrollState = ScrollState(0)
    val featuredList = MutableStateFlow<List<PlayableItemModel>>(emptyList())
    val lastPlayedList = MutableStateFlow<List<ModelLastPlayed>>(emptyList())
    val featuredMap = MutableStateFlow<Map<String, List<PlayableItemModel>>>(emptyMap())

    init {
        viewModelScope.launch {
            val nowPlaying = async {
                repoFeatured.getFeaturedByType("NOW_PLAYING")
                    .map { async { it.toPlayableItem() } }.awaitAll()
            }
            val lastPlayed = async {
                repoFeatured.getRecentlyPlayed()
            }

            val allFea = async {
                val map = LinkedHashMap<String, List<ModelFeatured>>()
                repoFeatured.getAllFeatured()
                    .reversed()
                    .filter { it.displayName != "NOW_PLAYING" }
                    .forEach {
                        if (map.containsKey(it.displayName).not())
                            map.put(it.displayName, mutableListOf())
                        map.put(it.displayName, map[it.displayName]!! + listOf(it))
                    }

                map.mapValues { entry ->
                    entry.value.map {
                        async { it.toPlayableItem() }
                    }.awaitAll()
                }
            }
            featuredList.emit(nowPlaying.await())
            featuredMap.emit(allFea.await())
            lastPlayedList.emit(lastPlayed.await())
        }
    }

    private suspend fun ModelFeatured.toPlayableItem(): PlayableItemModel {
        if (this.isMovie) {
            val mod = repoMovies.getMovieModel(this.flickId, this.imdbId)
            if (mod.isSuccess)
                return PlayableItemModel(
                    this.id, this.flickId, this.imdbId,
                    mod.getOrNull()!!.posterHorizontal,
                    mod.getOrNull()!!.posterVertical,
                    mod.getOrNull()?.title ?: "",
                    true, mod.getOrNull()!!.isHd, false
                )
        } else {
            val mod = repoSeries.getSeriesModel(this.flickId, this.imdbId)
            if (mod.isSuccess)
                return PlayableItemModel(
                    this.id, this.flickId, this.imdbId,
                    mod.getOrNull()!!.posterHorizontal,
                    mod.getOrNull()!!.posterVertical,
                    mod.getOrNull()?.title ?: "",
                    isMovie = false, isHd = true, isShimmer = false
                )
        }


        return PlayableItemModel(
            this.id,
            this.flickId,
            this.imdbId,
            "",
            "",
            "",
            true
        )
    }
}