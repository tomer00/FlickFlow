package com.tomer.myflix.ui.models

import com.tomer.myflix.data.models.TimePair
import com.tomer.myflix.data.models.LinkPair

data class MoviePlayerModalUi(
    val name:String,
    val links:List<LinkPair>,
    val introTime: TimePair,
    val poster : String
)
