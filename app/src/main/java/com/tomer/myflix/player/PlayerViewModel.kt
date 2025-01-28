package com.tomer.myflix.player

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.annotation.FloatRange
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Tracks
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo
import com.google.android.exoplayer2.trackselection.TrackSelectionOverride
import com.tomer.myflix.R
import com.tomer.myflix.data.models.LinkPair
import com.tomer.myflix.data.models.TimePair
import com.tomer.myflix.ui.models.MoviePlayerModalUi
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.math.roundToLong

@HiltViewModel
class PlayerViewModel
@Inject constructor(
    @ApplicationContext val appContext: Context,
) : ViewModel() {
    @SuppressLint("UnsafeOptInUsageError")
    val trackSelector = DefaultTrackSelector(appContext)
    val exoPlayer = ExoPlayer.Builder(appContext)
        .setTrackSelector(trackSelector)
        .build()

    //region UI STATE

    private val _isControls = MutableLiveData(true)
    val isControls: LiveData<Boolean> = _isControls

    private val _isBuffering = MutableLiveData(true)
    val isBuffering: LiveData<Boolean> = _isBuffering

    private val _isPlaying = MutableLiveData(PlayingState.INITIAL)
    val playerState: LiveData<PlayingState> = _isPlaying

    private val _seekBarPosition = MutableLiveData(0f to 0f)
    val seekBarPosition: LiveData<Pair<Float, Float>> = _seekBarPosition

    private val _timeText = MutableLiveData("00:00" to "00:00")
    val timeText: LiveData<Pair<String, String>> = _timeText

    //region QUALITY SPEED

    private val _isQuality = MutableLiveData<Boolean?>(null)
    val isQuality: LiveData<Boolean?> = _isQuality

    private val _isSpeed = MutableLiveData<Boolean?>(null)
    val isSpeed: LiveData<Boolean?> = _isSpeed

    fun changeQuality() {
        _isQuality.postValue(_isQuality.value?.not() ?: true)
    }

    fun changeSpeed() {
        _isSpeed.postValue(_isSpeed.value?.not() ?: true)
    }

    //endregion QUALITY SPEED

    //region :: SCALE TYPE

    private val listDrs = listOf(R.drawable.ic_expand, R.drawable.ic_delete, R.drawable.ic_play)
    private val _scaleType = MutableLiveData(0)
    val scaleType: LiveData<Int> = _scaleType

    fun setNextScaleType() {
        _scaleType.postValue(_scaleType.value?.plus(1)?.rem(listDrs.size))
        exoPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_DEFAULT
    }

    //endregion :: SCALE TYPE

    var videoDuration = 0L

    var playSpeed = 1f
    var movieModel: MoviePlayerModalUi = MoviePlayerModalUi(
        name = "Bhool Bhuliyya",
        links = listOf(
            LinkPair(
                "360 P",
                "360 p Hindi 320MB",
                "https://github.com/hindu744/qrator744/raw/refs/heads/main/t360.mp4"
            ),
            LinkPair(
                "480 P",
                "480 p Hindi 620MB",
                "https://github.com/hindu744/qrator744/raw/refs/heads/main/t480.mp4"
            ),
            LinkPair(
                "720 P",
                "720 p Hindi 1.2 GB",
                "https://github.com/hindu744/qrator744/raw/refs/heads/main/t720.mp4"
            ),
            LinkPair(
                "1080 P",
                "1080 p Hindi 2.4 GB",
                "https://github.com/hindu744/qrator744/raw/refs/heads/main/t1080.mp4"
            ),
        ),
        introTime = TimePair(12000, 24000),
        "https://images.pexels.com/photos/440731/pexels-photo-440731.jpeg"
    )

    //endregion UI STATE

    private var qua = "360"
    private var totalTImeText = "00:00"


    //region AUDIO TRACKS

    data class TrackInfo(val groupIndex: Int, val trackIndex: Int, val language: String)

    fun selectAudioTrack(trackInfo: TrackInfo?) {
        val mappedTrackInfo = trackSelector.currentMappedTrackInfo ?: return
        val audioTrackInfo = trackInfo ?: run {
            trackSelector.setParameters(
                trackSelector.buildUponParameters().setRendererDisabled(
                    C.TRACK_TYPE_AUDIO, true
                )
            )
            return
        }
        val rendererIndex = getAudioRendererIndex(mappedTrackInfo)
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

    private fun getAudioRendererIndex(mappedTrackInfo: MappedTrackInfo): Int {
        for (rendererIndex in 0 until mappedTrackInfo.rendererCount) {
            if (exoPlayer.getRendererType(rendererIndex) == C.TRACK_TYPE_AUDIO) {
                return rendererIndex
            }
        }
        return C.INDEX_UNSET
    }

    fun getAudioTrackInfo(mappedTrackInfo: MappedTrackInfo): List<TrackInfo> {
        val audioTrackInfoList = mutableListOf<TrackInfo>()
        for (rendererIndex in 0 until mappedTrackInfo.rendererCount) {
            if (mappedTrackInfo.getRendererType(rendererIndex) == C.TRACK_TYPE_AUDIO) {
                val trackGroups = mappedTrackInfo.getTrackGroups(rendererIndex)
                for (groupIndex in 0 until trackGroups.length) {
                    val trackGroup = trackGroups.get(groupIndex)
                    for (trackIndex in 0 until trackGroup.length) {
                        val format = trackGroup.getFormat(trackIndex)
                        var language = ""
                        format.label?.let { language += "$it - " }
                        language += getLanguageName(format.language)
                        Log.d(
                            "TAG--",
                            "getAudioTrackInfo: IS SELE ${
                                trackSelector.parameters.getSelectionOverride(
                                    rendererIndex,
                                    trackGroups
                                )?.containsTrack(trackIndex) == true
                            }"
                        )
                        audioTrackInfoList.add(TrackInfo(groupIndex, trackIndex, language))
                    }
                }
            }
        }
        return audioTrackInfoList
    }

    //endregion AUDIO TRACKS

    //region SUBTITLES


    fun selectSubtitleTrack(trackInfo: TrackInfo?) {

        val mappedTrackInfo = trackSelector.currentMappedTrackInfo ?: return
        val subTitleTrackInfo = trackInfo ?: run {
            trackSelector.setParameters(
                trackSelector.buildUponParameters()
                    .setRendererDisabled(C.TRACK_TYPE_VIDEO, true)
            )
            return
        }
        val rendererIndex = getSubtitleRendererIndex(mappedTrackInfo)
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

        trackSelector.setParameters(parameters)
    }

    private fun getSubtitleRendererIndex(mappedTrackInfo: MappedTrackInfo): Int {
        for (rendererIndex in 0 until mappedTrackInfo.rendererCount) {
            if (exoPlayer.getRendererType(rendererIndex) == C.TRACK_TYPE_TEXT) {
                return rendererIndex
            }
        }
        return C.INDEX_UNSET
    }

    fun getSubtitleTrackInfo(mappedTrackInfo: MappedTrackInfo): List<TrackInfo> {

        val subtitleTrackInfoList = mutableListOf<TrackInfo>()
        for (rendererIndex in 0 until mappedTrackInfo.rendererCount) {
            if (mappedTrackInfo.getRendererType(rendererIndex) == C.TRACK_TYPE_TEXT) {
                val trackGroups = mappedTrackInfo.getTrackGroups(rendererIndex)
                for (groupIndex in 0 until trackGroups.length) {
                    val trackGroup = trackGroups.get(groupIndex)
                    for (trackIndex in 0 until trackGroup.length) {
                        val format = trackGroup.getFormat(trackIndex)
                        var language = ""
                        format.label?.let { language += "$it - " }
                        language += getLanguageName(format.language)
                        subtitleTrackInfoList.add(TrackInfo(groupIndex, trackIndex, language))
                    }
                }
            }
        }
        return subtitleTrackInfoList
    }
    //endregion SUBTITLES


    private var seekBarSyncJob = viewModelScope.launch { }

    init {
        val mediaItem =
//            MediaItem.fromUri("https://this-is.ohnooo.site/d4c7591fcedc9c93e876cef4dd5e4f52/drivebot.sbs/Eagle%20(2024)%20{Hindi-Telugu}%20480p%20HEVC%20WEB-DL%20ESub%20[BollyFlix].mkv")
//            MediaItem.fromUri("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
            MediaItem.fromUri(
                File(
                    appContext.getExternalFilesDir("video"),
                    "video.mkv"
                ).absolutePath
            )
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()

        //region EXO LIS

        exoPlayer.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                error.printStackTrace()
                _isPlaying.postValue(PlayingState.ERROR)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        if (_isPlaying.value == PlayingState.INITIAL) {
                            totalTImeText = exoPlayer.duration.timeTextFromMs()
                            _isPlaying.postValue(PlayingState.LOADED)
                            if (videoDuration < 0L)
                                exoPlayer.seekTo(
                                    videoDuration.times(-1)
                                        .div(1000f)
                                        .times(exoPlayer.duration)
                                        .roundToLong()
                                )
                            videoDuration = exoPlayer.duration

                            hideControls(1_000)
                        }
                        _isBuffering.postValue(false)
                        seekBarSyncJob = viewModelScope.launch {
                            while (isActive) {
                                delay(1000)
                                _seekBarPosition.postValue(
                                    exoPlayer.currentPosition.div(videoDuration.toFloat())
                                            to
                                            exoPlayer.bufferedPosition.div(videoDuration.toFloat())
                                )
                                _timeText.postValue(
                                    exoPlayer.currentPosition.timeTextFromMs()
                                            to
                                            totalTImeText
                                )
                            }
                        }
                    }

                    Player.STATE_ENDED -> _isPlaying.postValue(PlayingState.ENDED)

                    Player.STATE_BUFFERING -> {
                        _isBuffering.postValue(true)
                        seekBarSyncJob.cancel()
                    }

                    else -> {}
                }
            }

            override fun onTracksChanged(tracks: Tracks) {
                super.onTracksChanged(tracks)
                Log.d("TAG--", "onTracksChanged: $tracks")
            }

        })

        //endregion EXO LIS
    }

    fun setQuality(pair: LinkPair) {
        val item = if (pair.disName == "AUTO") {
            val qualityCheck = checkPlayableQuality()
            MediaItem.fromUri(getUrlFromQuality(qualityCheck, movieModel.links))
        } else MediaItem.fromUri(pair.url)
        exoPlayer.setMediaItem(item, exoPlayer.currentPosition)
        exoPlayer.prepare()
    }

    private fun checkPlayableQuality(): Int {
        return 360
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
    }

    fun setPlayState() {
        if (_isPlaying.value!! != PlayingState.PLAYING)
            _isPlaying.postValue(PlayingState.PLAYING)
    }
}