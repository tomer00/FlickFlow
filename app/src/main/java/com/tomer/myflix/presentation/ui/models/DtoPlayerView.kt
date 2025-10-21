package com.tomer.myflix.presentation.ui.models

enum class PlayingType {
    MOVIE, EPISODE, LINK
}

data class DtoPlayerView(
    val id: String,
    val link: String,
    val type: PlayingType
)
