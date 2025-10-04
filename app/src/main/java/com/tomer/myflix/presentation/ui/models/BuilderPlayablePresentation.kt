package com.tomer.myflix.presentation.ui.models

import android.graphics.Color
import com.tomer.myflix.data.models.TimePair

data class BuilderPlayablePresentation(
    private var id: String? = null,
    private var name: String? = null,
    private var introTime: TimePair = TimePair(0L, 0L),
    private var poster: String? = null,
    private var colorAccent: Int = Color.RED,
    private var playedMs: Long = 0L,
    private var fitMode: Int = 0,
    private var audioTrack: TrackInfo? = null,
    private var videoTrack: TrackInfo? = null,
    private var subtitleTrack: TrackInfo? = null,
    private var speed: Float = 1.0f,
    private var seekPosition: Float = 0.0f,
) {

    fun id(id: String) = apply { this.id = id }

    fun name(name: String) = apply { this.name = name }

    fun introTime(introTime: TimePair) = apply { this.introTime = introTime }

    fun poster(poster: String) = apply { this.poster = poster }

    fun playedMs(playedMs: Long) = apply { this.playedMs = playedMs }

    fun fitMode(fitMode: Int) = apply { this.fitMode = fitMode }

    fun audioTrack(audioTrack: TrackInfo?) = apply { this.audioTrack = audioTrack }

    fun videoTrack(videoTrack: TrackInfo?) = apply { this.videoTrack = videoTrack }

    fun subtitleTrack(subtitleTrack: TrackInfo?) =
        apply { this.subtitleTrack = subtitleTrack }

    fun speed(speed: Float) = apply { this.speed = speed }

    fun seekPosition(seekPosition: Float) = apply { this.seekPosition = seekPosition }

    fun build(): ModelPLayerUI {
        val id = this.id ?: throw IllegalStateException("id must be set")
        val name = this.name ?: throw IllegalStateException("name must be set")
        val introTime = this.introTime
        val poster = this.poster ?: throw IllegalStateException("poster must be set")

        return ModelPLayerUI(
            flickId = id,
            name = name,
            introTime = introTime,
            poster = poster,
            playedMs = playedMs,
            scaleType = fitMode,
            audioTrack = audioTrack,
            videoTrack = videoTrack,
            subtitleTrack = subtitleTrack,
            speed = speed,
            seekPosition = seekPosition,
            accentCol = colorAccent
        )
    }
}