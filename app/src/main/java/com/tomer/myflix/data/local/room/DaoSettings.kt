package com.tomer.myflix.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tomer.myflix.presentation.ui.models.ModelPLayerUI

@Dao
abstract class DaoSettings {

    @Query("update player_ui set scaleType = :scale where flickId = :id")
    abstract fun saveScaleType(scale: Int, id: String)

    @Query("update player_ui set speed = :speed where flickId = :id")
    abstract fun savePlaybackSpeed(speed: Float, id: String)

    @Query("update player_ui set playedMs = :playedMs , seekPosition = :seekPosition where flickId = :id")
    abstract fun savePlayedMsAndSeekPos(playedMs: Long, seekPosition: Float, id: String)

    @Query("UPDATE player_ui SET videoTrack = :trackInfo where flickId = :id")
    abstract fun saveTrackVideo(trackInfo: String, id: String)

    @Query("UPDATE player_ui SET audioTrack = :trackInfo where flickId = :id")
    abstract fun saveTrackAudio(trackInfo: String, id: String)

    @Query("UPDATE player_ui SET subtitleTrack = :trackInfo where flickId = :id")
    abstract fun saveTrackSubTitle(trackInfo: String, id: String)

    @Query("SELECT * FROM player_ui WHERE flickId = :flickId")
    abstract fun getPlayerUi(flickId: String): ModelPLayerUI?

    @Insert(entity = ModelPLayerUI::class, onConflict = OnConflictStrategy.REPLACE)
    abstract fun savePlayerUI(mod: ModelPLayerUI)

}