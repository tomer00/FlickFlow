package com.tomer.myflix.data.local.repo

import com.tomer.myflix.data.local.models.ModelSuggestions
import com.tomer.myflix.data.local.room.DaoSuggestions
import com.tomer.myflix.data.remote.repo.RepoRemote
import com.tomer.myflix.data.remote.retro.modals.DtoSuggest
import javax.inject.Inject

interface RepoSuggestions {
    suspend fun getSuggestionListForMovie(flickId: String): List<DtoSuggest>
    suspend fun getSuggestionListForSeries(flickId: String): List<DtoSuggest>
}


class RepoSuggestionsImpl @Inject constructor(
    private val repoRemote: RepoRemote,
    private val daoSuggest: DaoSuggestions
) : RepoSuggestions {
    override suspend fun getSuggestionListForMovie(flickId: String) = getCombined(flickId, true)

    override suspend fun getSuggestionListForSeries(flickId: String) = getCombined(flickId, false)

    private suspend fun getCombined(flickId: String, isMovie: Boolean): List<DtoSuggest> {
        val formRoom = daoSuggest.getSuggestionsFromId(flickId)
        if (formRoom != null) return formRoom.suggestedItems.map {
            DtoSuggest(it.first, it.second, if (isMovie) "MOVIE" else "SERIES")
        }
        val fromServer = repoRemote.getSuggestionList(flickId)
        if (fromServer.isEmpty()) return emptyList()
        daoSuggest.insertSuggestions(
            ModelSuggestions(
                flickId, fromServer.map { it.flickId to it.imdbId }
            )
        )
        return fromServer
    }
}