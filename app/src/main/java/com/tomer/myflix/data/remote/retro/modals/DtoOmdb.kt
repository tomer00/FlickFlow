package com.tomer.myflix.data.remote.retro.modals

import com.google.gson.annotations.SerializedName

data class DtoOmdb(
    @SerializedName("Response")
    val response: String,
    // movie episode series
    @SerializedName("Type")
    val type: String,

    @SerializedName("imdbID")
    val imdbID: String,
    @SerializedName("Actors")
    val actors: String,
    @SerializedName("Country")
    val country: String,
    @SerializedName("Director")
    val director: String,
    @SerializedName("Genre")
    val genre: String,
    @SerializedName("imdbRating")
    val imdbRating: String,
    @SerializedName("Language")
    val language: String,
    @SerializedName("Plot")
    val plot: String,
    @SerializedName("Poster")
    val poster: String,
    @SerializedName("Released")
    val releasedDate: String,
    @SerializedName("Title")
    val title: String,
    @SerializedName("Writer")
    val writer: String,
    @SerializedName("Year")
    val year: String,


    @SerializedName("totalSeasons")
    val seasonCount: String,

    @SerializedName("Episode")
    val episode: String,
    @SerializedName("Season")
    val season: String,
    @SerializedName("seriesID")
    val seriesIMDBId: String?,
)
