package com.tomer.myflix.data.local.repo

import com.tomer.myflix.common.getDefaultHoriPoster
import com.tomer.myflix.data.local.models.ModelMovie
import com.tomer.myflix.data.local.room.DaoPlaying
import com.tomer.myflix.data.local.room.DaoSettings
import com.tomer.myflix.data.models.TimePair
import com.tomer.myflix.data.remote.repo.RepoRemote
import com.tomer.myflix.presentation.ui.models.BuilderMoviePresentation
import com.tomer.myflix.presentation.ui.models.ModelPLayerUI
import javax.inject.Inject

interface RepoMovies {
    suspend fun getMovieModelPresentation(id: String): ModelPLayerUI
    suspend fun getMovieModel(id: String, imdbId: String = ""): Result<ModelMovie>
    suspend fun canPlay(flickId: String): Boolean
}


class RepoMoviesImpl @Inject constructor(
    private val daoSettings: DaoSettings,
    private val daoPlaying: DaoPlaying,
    private val repoRemote: RepoRemote
) : RepoMovies {
    override suspend fun getMovieModelPresentation(id: String): ModelPLayerUI {
        val modUI = daoSettings.getPlayerUi(id)
        if (modUI != null) return modUI

        //try if it was a movie
        val movieModel = daoPlaying.getMovieFromId(id)
        if (movieModel != null) {
            return BuilderMoviePresentation(
                id, movieModel.title, movieModel.introTime,
                movieModel.posterHorizontal,
                colorAccent = movieModel.accentCol
            ).build().also { daoSettings.savePlayerUI(it) }
        }

        return BuilderMoviePresentation(
            id, "Unknown", TimePair(0L, 0L), getDefaultHoriPoster()
        ).build()
    }

    override suspend fun getMovieModel(id: String, imdbId: String): Result<ModelMovie> {
        val movieMod = daoPlaying.getMovieFromId(id)
        if (movieMod != null) return Result.success(movieMod)

        val result = repoRemote.getMovieModel(id, imdbId)
        if (result.isSuccess)
            result.getOrNull()?.let { daoPlaying.insertMovie(it) }
        return result
    }

    override suspend fun canPlay(flickId: String) = repoRemote.getCanPlay(flickId, true)
}