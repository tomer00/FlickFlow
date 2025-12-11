package com.tomer.myflix.data.local.repo

import com.tomer.myflix.data.local.models.ModelEpisode
import com.tomer.myflix.data.local.models.ModelSeries
import com.tomer.myflix.data.local.room.DaoSeries
import com.tomer.myflix.data.remote.repo.RepoRemote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface RepoSeries {
    suspend fun getSeriesModel(id: String, imdbId: String = ""): Result<ModelSeries>
    suspend fun getAllEpisodesOfSeries(seriesFlickId: String, season: Int): List<ModelEpisode>
}

class RepoSeriesImpl @Inject constructor(
    private val daoSeries: DaoSeries,
    private val repoRemote: RepoRemote
) : RepoSeries {

    override suspend fun getSeriesModel(id: String, imdbId: String): Result<ModelSeries> {
        val seriesMod = daoSeries.getSeriesFromId(id)
        if (seriesMod != null) return Result.success(seriesMod)

        val result = repoRemote.getSeriesModel(id, imdbId)
        if (result.isSuccess)
            result.getOrNull()?.let { daoSeries.insertSeries(it) }
        return result
    }

    override suspend fun getAllEpisodesOfSeries(
        seriesFlickId: String,
        season: Int
    ): List<ModelEpisode> {
        val allEpisodesLocal = daoSeries.getAllEpisodesOfSeries(seriesFlickId, season)
        if (allEpisodesLocal.isNotEmpty()) return allEpisodesLocal

        val allEpisodesRemoteModel = repoRemote.getAllEpisodesOfSeries(seriesFlickId, season)
        if (allEpisodesRemoteModel.isEmpty()) return emptyList()
        val finalList = mutableListOf<ModelEpisode>()
        val finalRes = runCatching {
            withContext(Dispatchers.IO) {
                allEpisodesRemoteModel.map {
                    async {
                        val remRes = repoRemote.getEpisodeModel(it.flickId, it.imdbId)
                        if (remRes.isSuccess) {
                            finalList.add(remRes.getOrThrow())
                            daoSeries.insertEpisode(remRes.getOrThrow())
                        }
                    }
                }.joinAll()
            }
        }
        if (finalRes.isFailure) return emptyList()
        return finalList
    }

}