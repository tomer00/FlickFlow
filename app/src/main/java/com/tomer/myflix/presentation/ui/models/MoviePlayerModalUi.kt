package com.tomer.myflix.ui.models

import com.tomer.myflix.data.models.LinkPair
import com.tomer.myflix.data.models.TimePair

data class MoviePlayerModalUi(
    val name: String,
    val links: List<LinkPair>,
    val introTime: TimePair,
    val poster: String,
    val prefQuality: Int = 0
)
