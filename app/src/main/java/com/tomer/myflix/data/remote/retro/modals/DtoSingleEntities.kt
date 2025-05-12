package com.tomer.myflix.data.remote.retro.modals

import com.google.gson.annotations.SerializedName
import com.tomer.myflix.data.models.TimePair

data class DtoEpisode(
    @SerializedName("audioTracks")
    val audioTracks: List<String>,
    @SerializedName("episode")
    val episode: Int,
    @SerializedName("flickId")
    val flickId: String,
    @SerializedName("posterVertical")
    val posterVertical: String,
    @SerializedName("releasedTime")
    val releasedTime: Long,
    @SerializedName("season")
    val season: Int,
    @SerializedName("seriesFlickId")
    val seriesFlickId: String,
    @SerializedName("serverNo")
    val serverNo: Int,
    @SerializedName("skipIntro")
    val skipIntro: TimePair,
    @SerializedName("title")
    val title: String
)

data class DtoMovie(
    @SerializedName("audioTracks")
    val audioTracks: List<String>,
    @SerializedName("flickId")
    val flickId: String,
    @SerializedName("isHD")
    val isHD: Boolean,
    @SerializedName("posterHorizontal")
    val posterHorizontal: String,
    @SerializedName("posterVertical")
    val posterVertical: String,
    @SerializedName("releasedTime")
    val releasedTime: Long,
    @SerializedName("screenShots")
    val screenShots: List<String>,
    @SerializedName("serverNo")
    val serverNo: Int,
    @SerializedName("skipIntro")
    val skipIntro: TimePair,
    @SerializedName("title")
    val title: String,
    @SerializedName("trailerUrl")
    val trailerUrl: String
)

data class DtoSeries(
    @SerializedName("flickId")
    val flickId: String,
    @SerializedName("posterHorizontal")
    val posterHorizontal: String,
    @SerializedName("posterVertical")
    val posterVertical: String,
    @SerializedName("releasedTime")
    val releasedTime: Long,
    @SerializedName("screenShots")
    val screenShots: List<String>,
    @SerializedName("seasonCount")
    val seasonCount: Int,
    @SerializedName("title")
    val title: String
)