package com.tomer.myflix.player

import android.content.Context
import android.util.Log
import androidx.annotation.FloatRange
import androidx.annotation.OptIn
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.tomer.myflix.data.local.file_cache.CacheInterceptor
import com.tomer.myflix.data.local.repo.RepoMovies
import com.tomer.myflix.data.local.repo.RepoSettings
import com.tomer.myflix.data.local.repo.TrackType
import com.tomer.myflix.presentation.ui.models.DtoPlayerView
import com.tomer.myflix.presentation.ui.models.ModelPLayerUI
import com.tomer.myflix.presentation.ui.models.PlayingType
import com.tomer.myflix.presentation.ui.models.TrackInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import javax.inject.Inject

@UnstableApi
@HiltViewModel
class PlayerViewModel
@Inject constructor(
    @ApplicationContext val appContext: Context,
    private val repoMovies: RepoMovies,
    private val repoSettings: RepoSettings,
) : ViewModel() {
    val trackSelector = DefaultTrackSelector(appContext)
        .apply {
            setParameters(
                buildUponParameters()
                    .setMaxVideoBitrate(Int.MAX_VALUE)
                    .setForceHighestSupportedBitrate(true)
                    .build()
            )
        }
    val cacheInterceptor = CacheInterceptor(appContext)
    val exoPlayer = createExoPlayer()


    private fun createExoPlayer(): ExoPlayer {

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(cacheInterceptor)
            .build()

        val okHttpDataSourceFactory = OkHttpDataSource.Factory(okHttpClient)

        val mediaSourceFactory = DefaultMediaSourceFactory(appContext)
            .setDataSourceFactory(okHttpDataSourceFactory)

        return ExoPlayer.Builder(appContext).apply {
            setTrackSelector(trackSelector)
            setMediaSourceFactory(mediaSourceFactory)
        }.build()
    }
    //region UI STATE

    private val _isSkip = MutableLiveData(false)
    val isSkip: LiveData<Boolean> = _isSkip

    private val _isControls = MutableLiveData(true)
    val isControls: LiveData<Boolean> = _isControls

    private val _isBuffering = MutableLiveData(true)
    val isBuffering: LiveData<Boolean> = _isBuffering

    private val _playState = MutableLiveData(PlayingState.INITIAL)
    val playerState: LiveData<PlayingState> = _playState

    private val _isPlaying = MutableLiveData(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _seekBarPosition = MutableLiveData(0f to 0f)
    val seekBarPosition: LiveData<Pair<Float, Float>> = _seekBarPosition

    private val _timeText = MutableLiveData("00:00" to "00:00")
    val timeText: LiveData<Pair<String, String>> = _timeText

    //region QUALITY SPEED AUDIO SUB
    private val _isSpeed = MutableLiveData<Boolean?>(null)
    val isSpeed: LiveData<Boolean?> = _isSpeed

    private val _isSidePanel = MutableLiveData<Pair<Int, List<TrackInfo>>>(null)
    val isSidePanel: LiveData<Pair<Int, List<TrackInfo>>> = _isSidePanel

    fun changeSpeed() {
        _isSpeed.postValue(_isSpeed.value?.not() != false)
    }

    fun showSidePanel(type: Int) {
        hideControls(0)
        when (type) {
            1 -> {
                _isSidePanel.postValue(
                    1 to getVideoTrackInfo(
                        trackSelector.currentMappedTrackInfo!!,
                        movieModel.videoTrack
                    )
                )
            }

            2 -> {
                _isSidePanel.postValue(
                    2 to getAudioTrackInfo(
                        trackSelector.currentMappedTrackInfo!!,
                        exoPlayer
                    )
                )
            }

            3 -> {
                _isSidePanel.postValue(
                    3 to getSubtitleTrackInfo(
                        trackSelector.currentMappedTrackInfo!!,
                        exoPlayer
                    )
                )
            }
        }
    }

    fun hideSidePanel() {
        _isSidePanel.postValue(0 to listOf())
    }

    //endregion QUALITY SPEED

    //region :: SCALE TYPE
    private val _scaleType = MutableLiveData(0)
    val scaleType: LiveData<Int> = _scaleType

    private var scaleHideJob = viewModelScope.launch { }
    private var currFitType = 0
    fun setNextScaleType() {
        val nextScale = currFitType.plus(1).rem(3)
        _scaleType.postValue(nextScale)
        repoSettings.saveScaleType(nextScale)
        currFitType = nextScale
        scaleHideJob.cancel()
        scaleHideJob = viewModelScope.launch {
            delay(1000)
            _scaleType.postValue(-1)
        }
    }

    //endregion :: SCALE TYPE

    var playSpeed = 1f
    var colAccent = 0
    private var retries = 0

    //endregion UI STATE

    private var seekBarSyncJob = viewModelScope.launch { }


    var movieModel: ModelPLayerUI = getSampleVideoModel()
    fun setMovieData(data: DtoPlayerView) {
        viewModelScope.launch {
            movieModel = repoMovies.getMovieModelPresentation(data.id)
            exoPlayer.apply {
                if (data.type != PlayingType.LINK)
                    setMediaItem(MediaItem.fromUri(getVideoLink(movieModel.flickId)))
                else setMediaItem(MediaItem.fromUri(data.link))
                prepare()
                playWhenReady = true
            }
            _scaleType.postValue(movieModel.scaleType)
            currFitType = movieModel.scaleType
            if (data.type != PlayingType.LINK) {
                cacheInterceptor.setId(data.id)
                repoSettings.setCurrentId(data.id)
            }
            _seekBarPosition.postValue(movieModel.seekPosition to 0f)
        }
    }

    init {

        //region EXO LIS

        exoPlayer.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                error.printStackTrace()
                retries++
                if (retries == 3) {
                    _playState.postValue(PlayingState.ERROR)
                    _isBuffering.postValue(false)
                } else exoPlayer.prepare()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        if (_playState.value == PlayingState.INITIAL) {
                            viewModelScope.launch {
                                delay(1000)
                                movieModel.videoTrack?.let {
                                    Log.d(
                                        "TAG--",
                                        "onPlaybackStateChanged: $it ${movieModel.videoTracks}"
                                    )
                                    selectVideoTrack(it, movieModel.videoTracks)
                                }
                                movieModel.audioTrack?.let { selectAudioTrack(it) }
                                movieModel.subtitleTrack?.let { selectSubtitleTrack(it) }
                            }
                            exoPlayer.seekTo(
                                movieModel.playedMs
                            )
                            _playState.postValue(PlayingState.LOADED)
                                .also {
                                    if (_isControls.value == false)
                                        exoPlayer.play()
                                }
                        }

                        _isBuffering.postValue(false)
                        seekBarSyncJob = createSeekBarSyncJob()
                    }

                    Player.STATE_ENDED -> _playState.postValue(PlayingState.ENDED)

                    Player.STATE_BUFFERING -> {
                        _isBuffering.postValue(true)
                        seekBarSyncJob.cancel()
                    }

                    else -> {}
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                _isPlaying.postValue(isPlaying)
                if (_playState.value != PlayingState.PLAYING) {
                    _playState.postValue(PlayingState.PLAYING)
                    if (exoPlayer.currentPosition >= movieModel.introTime.endTime) return
                    if (exoPlayer.currentPosition < movieModel.introTime.startTime)
                        viewModelScope.launch {
                            delay(movieModel.introTime.startTime)
                            _isSkip.postValue(true)
                            delay(movieModel.introTime.endTime - movieModel.introTime.startTime)
                            _isSkip.postValue(false)
                        }
                    else if (exoPlayer.currentPosition >= movieModel.introTime.startTime) {
                        _isSkip.postValue(true)
                        viewModelScope.launch {
                            delay(movieModel.introTime.endTime - exoPlayer.currentPosition)
                            _isSkip.postValue(false)
                        }
                    }
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


    fun skipIntro() {
        _isSkip.postValue(false)
        exoPlayer.seekTo(movieModel.introTime.endTime)
    }

    fun skipForward(millis: Long) {
        val currentPosition = exoPlayer.currentPosition
        val targetPosition = currentPosition + millis
        if (exoPlayer.duration != C.TIME_UNSET && targetPosition > exoPlayer.duration)
            exoPlayer.seekTo(exoPlayer.duration)
        else exoPlayer.seekTo(targetPosition)
    }

    fun skipBackward(millis: Long) {
        val currentPosition = exoPlayer.currentPosition
        (currentPosition - millis).also {
            if (it < 0) exoPlayer.seekTo(0)
            else exoPlayer.seekTo(it)
        }
    }

    fun setPlaybackSpeed(@FloatRange(0.25, 4.0) speed: Float) {
        playSpeed = speed
        exoPlayer.setPlaybackSpeed(playSpeed)
        repoSettings.savePlaybackSpeed(speed)
    }

    fun setPlayState() {
        if (_playState.value!! != PlayingState.PLAYING)
            _playState.postValue(PlayingState.PLAYING)
    }

    @OptIn(UnstableApi::class)
    fun savePlayBackState(seekPos: Float) {
        repoSettings.savePlayedMsAndSeekPos(
            exoPlayer.currentPosition,
            seekPos
        )
    }

    private fun createSeekBarSyncJob(): Job {
        return viewModelScope.launch {
            while (isActive) {
                _seekBarPosition.postValue(
                    exoPlayer.currentPosition.div(exoPlayer.duration.toFloat())
                            to
                            exoPlayer.bufferedPosition.div(exoPlayer.duration.toFloat())
                )
                _timeText.postValue(
                    exoPlayer.currentPosition.timeTextFromMs()
                            to
                            exoPlayer.duration.timeTextFromMs()
                )
                delay(1000)
            }
        }
    }

    //region TRACK SELECTION

    //region VIDEO TRACKS

    fun selectVideoTrack(trackInfo: TrackInfo, size: Int) {
        Log.d("TAG--", "selectVideoTrack: $trackInfo")
        _isSidePanel.postValue(0 to listOf())
        movieModel.videoTrack = trackInfo
        repoSettings.saveTrackInfo(TrackType.VIDEO, trackInfo)
        val mappedTrackInfo = trackSelector.currentMappedTrackInfo ?: return
        val videoTrackInfo = trackInfo
        if (trackInfo.trackIndex == -1) {
            trackSelector.setParameters(
                trackSelector.buildUponParameters()
                    .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                    .setMaxVideoBitrate(Int.MAX_VALUE)
                    .setForceHighestSupportedBitrate(true)
                    .build()
            )
            return
        }
        val rendererIndex = getVideoRendererIndex(mappedTrackInfo, exoPlayer)
        if (rendererIndex == C.INDEX_UNSET) return

        val trackGroup =
            mappedTrackInfo.getTrackGroups(rendererIndex).get(videoTrackInfo.groupIndex)
        val override =
            TrackSelectionOverride(trackGroup, (size - 1 - videoTrackInfo.trackIndex))

        val parameters = trackSelector.buildUponParameters()
            .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
            .addOverride(override)
            .build()

        trackSelector.setParameters(parameters)
    }

    //endregion VIDEO TRACKS

    //region AUDIO TRACKS

    fun selectAudioTrack(trackInfo: TrackInfo?) {
        _isSidePanel.postValue(0 to listOf())
        movieModel.audioTrack = trackInfo
        repoSettings.saveTrackInfo(TrackType.AUDIO, trackInfo)
        val mappedTrackInfo = trackSelector.currentMappedTrackInfo ?: return
        val audioTrackInfo = trackInfo ?: run {
            trackSelector.setParameters(
                trackSelector.buildUponParameters().setRendererDisabled(
                    C.TRACK_TYPE_AUDIO, true
                )
            )
            return
        }
        if (audioTrackInfo.trackIndex == -1) {
            trackSelector.setParameters(
                trackSelector.buildUponParameters().setRendererDisabled(
                    C.TRACK_TYPE_AUDIO, true
                )
            )
            return
        }
        val rendererIndex = getAudioRendererIndex(mappedTrackInfo, exoPlayer)
        if (rendererIndex == C.INDEX_UNSET) return

        val trackGroup =
            mappedTrackInfo.getTrackGroups(rendererIndex).get(audioTrackInfo.groupIndex)
        val override =
            TrackSelectionOverride(trackGroup, audioTrackInfo.trackIndex)

        val parameters = trackSelector.buildUponParameters()
            .clearOverridesOfType(C.TRACK_TYPE_AUDIO)
            .setRendererDisabled(C.TRACK_TYPE_AUDIO, false)
            .addOverride(override)
            .build()

        trackSelector.setParameters(parameters)
    }

    //endregion AUDIO TRACKS

    //region SUBTITLES


    @OptIn(UnstableApi::class)
    fun selectSubtitleTrack(trackInfo: TrackInfo?) {
        _isSidePanel.postValue(0 to listOf())
        movieModel.subtitleTrack = trackInfo
        repoSettings.saveTrackInfo(TrackType.SUBTITLE, trackInfo)
        val mappedTrackInfo = trackSelector.currentMappedTrackInfo ?: return
        val subTitleTrackInfo = trackInfo ?: run {
            trackSelector.setParameters(
                trackSelector.buildUponParameters()
                    .setRendererDisabled(C.TRACK_TYPE_VIDEO, true)
            )
            return
        }
        if (subTitleTrackInfo.trackIndex == -1) {
            trackSelector.setParameters(
                trackSelector.buildUponParameters()
                    .setRendererDisabled(C.TRACK_TYPE_VIDEO, true)
            )
            return
        }
        val rendererIndex = getSubtitleRendererIndex(mappedTrackInfo, exoPlayer)
        if (rendererIndex == C.INDEX_UNSET) return

        val trackGroup =
            mappedTrackInfo.getTrackGroups(rendererIndex).get(subTitleTrackInfo.groupIndex)
        val override =
            TrackSelectionOverride(trackGroup, subTitleTrackInfo.trackIndex)

        val parameters = trackSelector.buildUponParameters()
            .clearOverridesOfType(C.TRACK_TYPE_TEXT)
            .setRendererDisabled(C.TRACK_TYPE_VIDEO, false)
            .addOverride(override)
            .build()

        Log.d("TAG--", "selectSubtitleTrack: ${trackInfo.language}")

        trackSelector.setParameters(parameters)
    }


    //endregion SUBTITLES

    //endregion TRACK SELECTION

}