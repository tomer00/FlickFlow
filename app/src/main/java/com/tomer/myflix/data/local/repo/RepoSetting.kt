package com.tomer.myflix.data.local.repo

import android.util.Log
import com.tomer.myflix.common.gson
import com.tomer.myflix.data.local.models.ModelLastPlayed
import com.tomer.myflix.data.local.room.DaoFeaturedCollection
import com.tomer.myflix.data.local.room.DaoSettings
import com.tomer.myflix.presentation.ui.models.TrackInfo
import javax.inject.Inject

enum class TrackType {
    VIDEO, AUDIO, SUBTITLE
}

interface RepoSettings {
    fun saveScaleType(scale: Int)
    fun savePlaybackSpeed(speed: Float)
    fun savePlayedMsAndSeekPos(playedMs: Long, seekPosition: Float)
    fun saveTrackInfo(type: TrackType, trackInfo: TrackInfo?)
    fun saveLastPlayed(mod: ModelLastPlayed)
    fun setCurrentId(id: String?)
    fun setSaveMode(mode: Boolean)
}

class RepoSettingsRoom @Inject constructor(
    private val daoSettings: DaoSettings,
    private val daoRepoFeatured: DaoFeaturedCollection
) : RepoSettings {

    private var id: String? = null
    private var canSave = true

    override fun setCurrentId(id: String?) {
        this.id = id
    }

    override fun setSaveMode(mode: Boolean) {
        canSave = mode
    }

    override fun saveScaleType(scale: Int) {
        if (id.isNullOrEmpty()) return
        if (canSave)
            daoSettings.saveScaleType(scale, id!!)
    }

    override fun savePlaybackSpeed(speed: Float) {
        if (id.isNullOrEmpty()) return
        if (canSave)
            daoSettings.savePlaybackSpeed(speed, id!!)
    }

    override fun savePlayedMsAndSeekPos(playedMs: Long, seekPosition: Float) {
        if (id.isNullOrEmpty()) return
        if (canSave)
            daoSettings.savePlayedMsAndSeekPos(playedMs, seekPosition, id!!)
    }

    override fun saveTrackInfo(
        type: TrackType,
        trackInfo: TrackInfo?
    ) {
        if (id.isNullOrEmpty()) return
        if (!canSave) return
        when (type) {
            TrackType.VIDEO -> daoSettings.saveTrackVideo(
                if (trackInfo == null) "" else gson.toJson(
                    trackInfo
                ), id!!
            )

            TrackType.AUDIO -> daoSettings.saveTrackAudio(
                if (trackInfo == null) "" else gson.toJson(
                    trackInfo
                ), id!!
            )

            TrackType.SUBTITLE -> daoSettings.saveTrackSubTitle(
                if (trackInfo == null) "" else gson.toJson(
                    trackInfo
                ), id!!
            )
        }
    }

    override fun saveLastPlayed(mod: ModelLastPlayed) {
        if (id.isNullOrEmpty()) return
        if (!canSave) return
        daoRepoFeatured.deleteLastPlayed(id!!)
        daoRepoFeatured.insertLastPlayed(mod)
    }

}