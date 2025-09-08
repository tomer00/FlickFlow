package com.tomer.myflix.data.remote.repo

import com.tomer.myflix.data.local.models.ModelEpisode
import com.tomer.myflix.data.local.models.ModelFeatured
import com.tomer.myflix.data.local.models.ModelMovie
import com.tomer.myflix.data.local.models.ModelSeries
import com.tomer.myflix.data.remote.retro.modals.DtoSuggest

interface RepoRemote {

    suspend fun getMovieModel(flickId: String, imdbId: String): Result<ModelMovie>
    suspend fun getEpisodeModel(flickId: String, imdbId: String): Result<ModelEpisode>
    suspend fun getSeriesModel(flickId: String, imdbId: String): Result<ModelSeries>
    suspend fun getSuggestionList(flickId: String): List<DtoSuggest>
    suspend fun getFeaturedList(): List<ModelFeatured>
    suspend fun getCanPlay(flickId: String, isMovie: Boolean): Boolean
}