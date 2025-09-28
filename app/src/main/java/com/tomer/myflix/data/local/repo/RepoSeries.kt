package com.tomer.myflix.data.local.repo

import com.tomer.myflix.data.local.models.ModelSeries
import com.tomer.myflix.data.local.room.DaoSeries
import com.tomer.myflix.data.remote.repo.RepoRemote
import javax.inject.Inject

interface RepoSeries {
    suspend fun getSeriesModel(id: String, imdbId: String = ""): Result<ModelSeries>
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

}