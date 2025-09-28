package com.tomer.myflix.data.local.repo

import com.tomer.myflix.common.getDefaultHoriPoster
import com.tomer.myflix.data.local.models.ModelEpisode
import com.tomer.myflix.data.local.room.DaoPlaying
import com.tomer.myflix.data.local.room.DaoSettings
import com.tomer.myflix.data.models.TimePair
import com.tomer.myflix.data.remote.repo.RepoRemote
import com.tomer.myflix.presentation.ui.models.BuilderMoviePresentation
import com.tomer.myflix.presentation.ui.models.ModelPLayerUI
import javax.inject.Inject

interface RepoEpisode {
    suspend fun getEpisodeModelPresentation(id: String): ModelPLayerUI
    suspend fun getEpisodeModel(id: String, imdbId: String = ""): Result<ModelEpisode>
    suspend fun canPlay(flickId: String): Boolean
}

class RepoEpisodeImpl @Inject constructor(
    private val daoSettings: DaoSettings,
    private val daoPlaying: DaoPlaying,
    private val repoRemote: RepoRemote
) : RepoEpisode {
    override suspend fun getEpisodeModelPresentation(id: String): ModelPLayerUI {
        val modUI = daoSettings.getPlayerUi(id)
        if (modUI != null) return modUI

        val episodeModel = daoPlaying.getEpisodeFromId(id)
        if (episodeModel != null) {
            val seriesPoster =
                daoPlaying.getHoriPosterOfSeries(episodeModel.seriesFlickId)
                    ?: getDefaultHoriPoster()
            return BuilderMoviePresentation(
                id, episodeModel.title, episodeModel.introTime, seriesPoster
            ).build().also { daoSettings.savePlayerUI(it) }
        }

        return BuilderMoviePresentation(
            id, "Unknown", TimePair(0L, 0L), getDefaultHoriPoster()
        ).build()
    }

    override suspend fun getEpisodeModel(id: String, imdbId: String): Result<ModelEpisode> {
        val episodeMod = daoPlaying.getEpisodeFromId(id)
        if (episodeMod != null) return Result.success(episodeMod)

        val result = repoRemote.getEpisodeModel(id, imdbId)
        if (result.isSuccess)
            result.getOrNull()?.let { daoPlaying.insertEpisode(it) }
        return result
    }

    override suspend fun canPlay(flickId: String) = repoRemote.getCanPlay(flickId, false)

}