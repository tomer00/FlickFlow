package com.tomer.myflix.presentation.screens.detailMovie

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.ScrollState
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomer.myflix.common.gson
import com.tomer.myflix.data.local.models.ModelMovie
import com.tomer.myflix.data.local.repo.RepoMovies
import com.tomer.myflix.data.local.repo.RepoSuggestions
import com.tomer.myflix.player.PlayerActivity
import com.tomer.myflix.presentation.ui.models.DtoPlayerView
import com.tomer.myflix.presentation.ui.models.PlayableItemModel
import com.tomer.myflix.presentation.ui.models.PlayingType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class VMDetailMovies @Inject constructor(
    @param:ApplicationContext private val con: Context,
    private val repoSuggestions: RepoSuggestions,
    savedStateHandle: SavedStateHandle,
    private val repoMovies: RepoMovies,
) : ViewModel() {

    val mod = MutableStateFlow<ModelMovie?>(null)
    val col = MutableStateFlow(Color.Red)
    val scroll = ScrollState(0)

    val listSuggestions = MutableStateFlow<List<MutableStateFlow<PlayableItemModel>>>(emptyList())

    //region PLAY BUTTON

    val showProg = MutableStateFlow(false)
    val textPlayButton = MutableStateFlow("Watch Now")

    fun getCanPlayNow(con: Context) {
        viewModelScope.launch {
            showProg.emit(true)
            val res = repoMovies.canPlay(flickId)
            if (res) {
                val intent = Intent(con, PlayerActivity::class.java).apply {
                    putExtra(
                        "data",
                        gson.toJson(DtoPlayerView(flickId, "", PlayingType.MOVIE))
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
        flickId = data.substring(0, 6)
        val imdbId = data.substring(6)
        viewModelScope.launch {
            val result = repoMovies.getMovieModel(flickId, imdbId)
            mod.emit(result.getOrNull())
            col.emit(Color(result.getOrNull()?.accentCol ?: android.graphics.Color.RED).copy(.8f))

            val suggestedItems = repoSuggestions.getSuggestionListForMovie(flickId).map {
                MutableStateFlow(
                    PlayableItemModel(
                        Random.nextInt(),
                        it.flickId, it.imdbId,
                        "", "", "", true, true
                    )
                )
            }
            listSuggestions.emit(suggestedItems)

            suggestedItems.forEach {
                viewModelScope.launch {
                    val mod = it.value
                    val curResult = repoMovies.getMovieModel(mod.flickId, mod.imdbId)
                    val curItem = curResult.getOrNull()
                    if (curResult.isSuccess && curItem != null) {
                        it.emit(
                            PlayableItemModel(
                                mod.id,
                                mod.flickId, mod.imdbId,
                                curItem.posterHorizontal,
                                curItem.posterVertical,
                                curItem.title,
                                true, curItem.isHd, false
                            )
                        )
                    }
                }
            }
        }
    }
}