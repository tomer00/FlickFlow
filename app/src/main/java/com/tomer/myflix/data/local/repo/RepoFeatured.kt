package com.tomer.myflix.data.local.repo

import com.tomer.myflix.data.local.models.ModelFeatured
import com.tomer.myflix.data.local.models.ModelLastPlayed
import com.tomer.myflix.data.local.room.DaoFeaturedCollection
import com.tomer.myflix.data.remote.repo.RepoRemote
import javax.inject.Inject

interface RepoFeatured {
    suspend fun getAllFeatured(): List<ModelFeatured>
    suspend fun getFeaturedByType(name: String): List<ModelFeatured>
    suspend fun resetFeatured()
    suspend fun getRecentlyPlayed(): List<ModelLastPlayed>
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

    override suspend fun getRecentlyPlayed(): List<ModelLastPlayed> {
        val grouped = daoFea.getLastPlayedItems().groupBy { it.title.substring(0, 6) }
        val list = mutableListOf<ModelLastPlayed>()
        grouped.forEach { (_, v) -> list.add(v.first()) }
        return list.map { it.copy(title = it.title.substring(6)) }.toList()
    }
}