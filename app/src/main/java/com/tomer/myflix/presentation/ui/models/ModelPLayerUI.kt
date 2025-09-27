package com.tomer.myflix.presentation.ui.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.tomer.myflix.data.local.room.TypeConverterMovie
import com.tomer.myflix.data.models.TimePair

@Entity(tableName = "player_ui")
@TypeConverters(TypeConverterMovie::class)
data class ModelPLayerUI(
    @PrimaryKey(autoGenerate = false)
    val flickId: String,
    val name: String,
    val introTime: TimePair,
    val poster: String,
    val accentCol:Int,

    val playedMs: Long,
    val scaleType: Int,
    var audioTrack: TrackInfo?,
    var videoTrack: TrackInfo?,
    var subtitleTrack: TrackInfo?,
    var speed: Float,
    val seekPosition: Float,
)
