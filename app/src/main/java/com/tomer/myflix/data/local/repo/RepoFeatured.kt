package com.tomer.myflix.data.local.repo

import com.tomer.myflix.data.local.models.ModelFeatured
import com.tomer.myflix.data.local.room.DaoFeaturedCollection
import com.tomer.myflix.data.remote.repo.RepoRemote
import javax.inject.Inject

interface RepoFeatured {
    suspend fun getAllFeatured(): List<ModelFeatured>
    suspend fun getFeaturedByType(name: String): List<ModelFeatured>
    suspend fun resetFeatured()
}

class RepoFeaturedImpl @Inject constructor(
    private val daoFea: DaoFeaturedCollection,
    private val repoRemote: RepoRemote
) : RepoFeatured {
    override suspend fun getAllFeatured(): List<ModelFeatured> {
        val roomRes = daoFea.getAllFeatured()
        if (roomRes.isNotEmpty()) return roomRes
        val remoteRes = repoRemote.getFeaturedList()
        daoFea.insertAllFeatured(remoteRes)
        return remoteRes
    }

    override suspend fun getFeaturedByType(name: String): List<ModelFeatured> {
        val roomRes = daoFea.getAllFeatured().filter { it.displayName == name }
        if (roomRes.isNotEmpty()) return roomRes
        val remoteRes = repoRemote.getFeaturedList()
        daoFea.insertAllFeatured(remoteRes)
        return remoteRes.filter { it.displayName == name }
    }

    override suspend fun resetFeatured() {
        daoFea.deleteAllFeatured()
        getAllFeatured()
    }

}