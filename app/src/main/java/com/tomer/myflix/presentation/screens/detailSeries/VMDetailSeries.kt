package com.tomer.myflix.presentation.screens.detailSeries

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomer.myflix.common.gson
import com.tomer.myflix.data.local.models.ModelEpisode
import com.tomer.myflix.data.local.models.ModelSeries
import com.tomer.myflix.data.local.repo.RepoEpisode
import com.tomer.myflix.data.local.repo.RepoSeries
import com.tomer.myflix.data.local.repo.RepoSuggestions
import com.tomer.myflix.player.PlayerActivity
import com.tomer.myflix.presentation.ui.models.DtoPlayerView
import com.tomer.myflix.presentation.ui.models.PlayableItemModel
import com.tomer.myflix.presentation.ui.models.PlayingType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class VMDetailSeries @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repoSeries: RepoSeries,
    private val repoEpisode: RepoEpisode,
    private val repoSuggestions: RepoSuggestions
) : ViewModel() {

    val mod = MutableStateFlow<ModelSeries?>(null)
    val col = MutableStateFlow(Color.Red)
    val scroll = ScrollState(0)

    val listSuggestions = MutableStateFlow<List<MutableStateFlow<PlayableItemModel>>>(emptyList())
    val episodes = MutableStateFlow<List<List<ModelEpisode>>>(emptyList())
    val audioTracks = MutableStateFlow<List<String>>(emptyList())

    //region PLAY BUTTON

    val showProg = MutableStateFlow(false)
    val textPlayButton = MutableStateFlow("Watch Now")

    fun getCanPlayNow(con: Context,flickId: String) {
        viewModelScope.launch {
            showProg.emit(true)
            val res = repoEpisode.canPlay(flickId)
            if (res) {
                val intent = Intent(con, PlayerActivity::class.java)
                    .apply {
                        putExtra(
                            "data",
                            gson.toJson(DtoPlayerView(flickId, "", PlayingType.EPISODE))
                        )
                    }
                con.startActivity(intent)
            } else textPlayButton.emit("Can't play now")
            showProg.emit(false)
        }
    }

    //endregion PLAY BUTTON

    private var flickId = ""

    init {
        val data = savedStateHandle.get<String>("id") ?: "qwertyui"
        Log.d("TAG--", "init: $data")
        val flickId = data.substring(0, 6)
        this.flickId = flickId
        val imdbId = data.substring(6)
        viewModelScope.launch {
            val result = repoSeries.getSeriesModel(flickId, imdbId)
            mod.emit(result.getOrNull())
            col.emit(Color(result.getOrNull()?.accentCol ?: android.graphics.Color.RED).copy(.8f))
        }
        viewModelScope.launch {
            val episodesOfSeason1 = repoSeries.getAllEpisodesOfSeries(flickId, 1)
            val list = mutableListOf(episodesOfSeason1)
            repeat((mod.value?.seasonCount ?: 1) - 1) {
                list.add(emptyList())
            }
            episodes.emit(list)
        }

        viewModelScope.launch {
            val suggestedItems = repoSuggestions.getSuggestionListForSeries(flickId).map {
                MutableStateFlow(
                    PlayableItemModel(
                        Random.nextInt(),
                        it.flickId, it.imdbId,
                        "", "", "", true, true
                    )
                )
            }
//            listSuggestions.emit(suggestedItems)
//
//            suggestedItems.forEach {
//                viewModelScope.launch {
//                    val mod = it.value
//                    val curResult = repoSeries.getSeriesModel(mod.flickId, mod.imdbId)
//                    val curItem = curResult.getOrNull()
//                    if (curResult.isSuccess && curItem != null) {
//                        it.emit(
//                            PlayableItemModel(
//                                mod.id,
//                                mod.flickId, mod.imdbId,
//                                curItem.posterHorizontal,
//                                curItem.posterVertical,
//                                curItem.title,
//                                true, true, false
//                            )
//                        )
//                    }
//                }
//            }
        }
    }
}