package com.tomer.myflix.data.remote.retro.modals


import com.google.gson.annotations.SerializedName

data class DtoIds(
    @SerializedName("content")
    val content: List<Content>,
    @SerializedName("isLast")
    val isLast: Boolean,
    @SerializedName("size")
    val size: Int
)

data class Content(
    @SerializedName("flickId")
    val flickId: String,
    @SerializedName("imdbId")
    val imdbId: String
)

data class DtoCanPlay(
    @SerializedName("canPlay")
    val canPlay: Boolean,
    @SerializedName("flickId")
    val flickId: String,
)

data class DtoSuggest(
    @SerializedName("flickId")
    val flickId: String,
    @SerializedName("imdbId")
    val imdbId: String,
    @SerializedName("type")
    val type: String
)