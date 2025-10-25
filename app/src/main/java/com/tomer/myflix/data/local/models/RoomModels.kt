package com.tomer.myflix.data.local.models

import android.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import com.tomer.myflix.data.local.room.TypeConverterMovie
import com.tomer.myflix.data.models.TimePair

@Entity(tableName = "movies")
@TypeConverters(TypeConverterMovie::class)
data class ModelMovie(
    @PrimaryKey(autoGenerate = false)
    val flickId: String,
    val imdbId: String,
    val title: String,
    val releaseDate: Long,
    val year: Int,
    val rating: Float,

    val country: String,
    val summary: String,

    val director: String,
    val writer: String,
    val cast: List<String>,

    val runtimeSecs: Int,
    val audioTracks: List<String>,
    val screenShots: List<String>,
    val posterHorizontal: String,
    val posterVertical: String,
    val trailerUrl: String,

    val introTime: TimePair,
    val serverNo: Int,
    val isHd: Boolean,
    val accentCol: Int = Color.RED,
)

@Entity(tableName = "episodes")
@TypeConverters(TypeConverterMovie::class)
data class ModelEpisode(
    @PrimaryKey(autoGenerate = false)
    val flickId: String,
    val seriesFlickId: String,
    val imdbId: String,
    val title: String,
    val posterHorizontal: String,
    val releaseDate: Long,
    val season: Int,
    val episode: Int,

    val runtimeSecs: Int,
    val audioTracks: List<String>,

    val introTime: TimePair,
    val serverNo: Int,
)

@Entity(tableName = "series")
@TypeConverters(TypeConverterMovie::class)
data class ModelSeries(
    @PrimaryKey(autoGenerate = false)
    val flickId: String,
    val imdbId: String,
    val title: String,
    val rating: Float,
    val releaseDate: Long,
    val year: Int,

    val country: String,
    val summary: String,

    val director: String,
    val writer: String,

    val cast: List<String>,
    val screenShots: List<String>,
    val posterHorizontal: String,
    val posterVertical: String,
    val trailerUrl: String,

    val seasonCount: Int,
    val accentCol: Int,
)

@Entity(tableName = "featured")
@TypeConverters(TypeConverterMovie::class)
data class ModelFeatured(
    @SerializedName("id")
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    @SerializedName("flickId")
    val flickId: String,
    @SerializedName("imdbId")
    val imdbId: String,
    @SerializedName("displayName")
    val displayName: String,
    @SerializedName("isMovie")
    val isMovie: Boolean
)

@Entity(tableName = "collection")
@TypeConverters(TypeConverterMovie::class)
data class ModelCollection(
    @SerializedName("flickId")
    @PrimaryKey(autoGenerate = false)
    val flickId: String,
    @SerializedName("moviesFlickIds")
    val moviesFlickIds: List<String>,
    @SerializedName("name")
    val name: String,
    @SerializedName("posterVertical")
    val posterVertical: String,
    @SerializedName("updateSequence")
    val updateSequence: Int
)

@Entity(tableName = "suggestions")
@TypeConverters(TypeConverterMovie::class)
data class ModelSuggestions(
    @SerializedName("flickId")
    @PrimaryKey(autoGenerate = false)
    val flickId: String,
    @SerializedName("data")
    val suggestedItems: List<Pair<String, String>>
)

@Entity(tableName = "last_played")
data class ModelLastPlayed(
    @SerializedName("id")
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @SerializedName("flickId")
    val flickId: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("progress")
    val progress: Float,
    @SerializedName("accentColor")
    val accentColor: Int,
    @SerializedName("isMovie")
    val isMovie: Boolean,
)