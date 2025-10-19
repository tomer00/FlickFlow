package com.tomer.myflix.presentation.ui.models

data class TrackInfo(
    val groupIndex: Int,
    val trackIndex: Int,
    val language: String,
    var isSelected: Boolean = false
)