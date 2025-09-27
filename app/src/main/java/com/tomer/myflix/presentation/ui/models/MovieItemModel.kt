package com.tomer.myflix.presentation.ui.models

data class MovieItemModel(
    val id: Int,
    val flickId: String,
    val imdbId: String,
    val posterHori: String,
    val posterVerti: String,
    val title: String,
    val isHd: Boolean = false,
    val isShimmer: Boolean = true
)