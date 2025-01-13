package com.tomer.myflix.player

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel
@Inject constructor(
    @ApplicationContext val appContext: Context,
) : ViewModel() {
    val exoPlayer = ExoPlayer.Builder(appContext).build()

    //region UI STATE

    private val _isControls = MutableLiveData(true)
    val isControls: LiveData<Boolean> = _isControls

    private val _isBuffering = MutableLiveData(true)
    val isBuffering: LiveData<Boolean> = _isBuffering

    private val _isPlaying = MutableLiveData(true)
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _seekBarPosition = MutableLiveData(0f to 0f)
    val seekBarPosition: LiveData<Pair<Float, Float>> = _seekBarPosition


    val fileName = "Bhool Bhulaiyaa 3 (2024) Hindi"
    var videoDuration = 0L

    var playbackSpeed = 1f

    //endregion UI STATE

    init {
        val mediaItem =
//            MediaItem.fromUri("https://pub-9ead93dfc1f64c25bf48fd6072ef2f37.r2.dev/f064577033d56521d63fa187d5951f67/Bhool%20Bhulaiyaa%203%20(2024)%20Hindi%20480p%20WEB-DL%20ESub%20[BollyFlix].mkv")
            MediaItem.fromUri("https://raw.githubusercontent.com/hindu744/qrator744/refs/heads/main/t360.mp4")
//            MediaItem.fromUri("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()

        viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _seekBarPosition.postValue(
                    exoPlayer.currentPosition.div(videoDuration.toFloat())
                            to
                            exoPlayer.bufferedPosition.div(videoDuration.toFloat())
                )
            }
        }

        //region EXO LIS

        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        Log.d("TAG--", "onPlaybackStateChanged: STATE_READY $playbackState")
                        _isBuffering.postValue(false)
                    }

                    Player.STATE_IDLE -> {
                        Log.d("TAG--", "onPlaybackStateChanged: STATE_IDLE $playbackState")
                    }

                    Player.STATE_ENDED -> {
                        Log.d("TAG--", "onPlaybackStateChanged: STATE_ENDED $playbackState")
                    }

                    Player.STATE_BUFFERING -> {
                        Log.d("TAG--", "onPlaybackStateChanged: STATE_BUFFERING $playbackState")
                        _isBuffering.postValue(true)
                    }

                    else -> {
                        Log.d("TAG--", "onPlaybackStateChanged: ELSE $playbackState")
                    }
                }
                if (playbackState == Player.STATE_READY) {
                    videoDuration = exoPlayer.duration
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    // Player is playing
                    Log.d("TAG--", "onIsPlayingChanged: Player is playing")
                } else {
                    // Player is paused or stopped
                    Log.d("TAG--", "onIsPlayingChanged: Player is paused")
                }
            }

        })

        //endregion EXO LIS
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
    }

    private var hidingJob = viewModelScope.launch { delay(100) }
    fun hideControls(delayMs: Long = 2000) {
        if (hidingJob.isActive) hidingJob.cancel()
        hidingJob = viewModelScope.launch {
            delay(delayMs)
            _isControls.postValue(false)
        }
    }

    fun showControls() {
        if (hidingJob.isActive) hidingJob.cancel()
        _isControls.postValue(true)
    }


    fun skipForward(millis: Long) {
        val currentPosition = exoPlayer.currentPosition
        val targetPosition = currentPosition + millis
        if (exoPlayer.duration != androidx.media3.common.C.TIME_UNSET && targetPosition > exoPlayer.duration) exoPlayer.seekTo(
            exoPlayer.duration
        )
        else exoPlayer.seekTo(targetPosition)
    }

    fun skipBackward(millis: Long) {
        val currentPosition = exoPlayer.currentPosition
        (currentPosition - millis).also {
            if (it < 0) {
                exoPlayer.seekTo(0)
            } else {
                exoPlayer.seekTo(it)
            }
        }
    }


}