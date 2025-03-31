package com.tomer.myflix.player

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.WindowInsets
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.compose.ui.unit.dp
import androidx.core.view.children
import androidx.core.view.isEmpty
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import com.tomer.myflix.R
import com.tomer.myflix.databinding.ActivityExoPlayerBinding
import com.tomer.myflix.databinding.PanelSpeedBinding
import com.tomer.myflix.databinding.RowSidePanelBinding
import com.tomer.myflix.presentation.ui.models.TrackInfo
import com.tomer.myflix.presentation.ui.views.GestureView
import com.tomer.myflix.presentation.ui.views.SeekBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@AndroidEntryPoint
class PlayerActivity : AppCompatActivity(), View.OnClickListener {

    private val b by lazy { ActivityExoPlayerBinding.inflate(layoutInflater) }
    private val vm by viewModels<PlayerViewModel>()

    private val audioMan by lazy { this.getSystemService(AUDIO_SERVICE) as AudioManager }

    //region LIFE CYCLES

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            SystemBarStyle.auto(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            ) { this.isDarkModeEnabled() })

        val movieId = intent.getStringExtra("id") ?: kotlin.run {
            finish()
            return
        }

        vm.setMovieData(movieId)

        enableEdgeToEdge()
        setContentView(b.root)
        b.exoPlayer.player = vm.exoPlayer

        setupPlayerControls()
        setupObservers()
    }


    @OptIn(UnstableApi::class)
    override fun onPause() {
        super.onPause()
        vm.savePlayBackState(b.seekBar.progress)
    }

    override fun onResume() {
        super.onResume()
        val vol = getCurrentAndMaxVol()
        b.gestureView.setVolAndBrightNess(
            vol.first, vol.second,
            window.attributes.screenBrightness.takeIf { it >= 0f } ?: 0.5f)
    }

    override fun onStop() {
        super.onStop()
        vm.exoPlayer.pause()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    //endregion LIFE CYCLES

    //region CLICK LISTENERS

    @OptIn(UnstableApi::class)
    override fun onClick(v: View) {
        when (v.id) {
            b.btBack.id -> onBackPressed()

            b.btPlay.id -> {
                vm.setPlayState()
                if (vm.exoPlayer.isPlaying)
                    vm.exoPlayer.pause()
                else vm.exoPlayer.play()
            }

            b.btPrev.id -> vm.skipBackward(5_000)
            b.btNext.id -> vm.skipForward(15_000)

            b.btScaling.id -> vm.setNextScaleType()
            b.btVideoQuality.id -> if (vm.playerState.value!! == PlayingState.LOADED
                || vm.playerState.value!! == PlayingState.PLAYING
            ) vm.showSidePanel(1)

            b.btAudio.id ->
                if (
                    vm.playerState.value!! == PlayingState.LOADED
                    || vm.playerState.value!! == PlayingState.PLAYING
                ) vm.showSidePanel(2)

            b.btSubtitle.id -> if (vm.playerState.value!! == PlayingState.LOADED
                || vm.playerState.value!! == PlayingState.PLAYING
            ) vm.showSidePanel(3)


            b.btSpeed.id -> {
                vm.changeSpeed()
                vm.hideControls(0)
                if (b.llSpeed.isEmpty()) loadSpeedPanel()
                val llS = b.llSpeed.getChildAt(0) as LinearLayout
                String.format(Locale.getDefault(), "%.2f x", vm.playSpeed)
                    .also { (llS.getChildAt(0) as TextView).text = it }

                val prog = vm.playSpeed.minus(.25f).div(3.75f).times(100).roundToInt()
                (llS.getChildAt(1) as AppCompatSeekBar).progress = prog

            }

            b.btSkip.id -> vm.skipIntro()

            else -> {}
        }
    }

    //endregion CLICK LISTENERS

    //region SETUP

    @OptIn(UnstableApi::class)
    private fun setupObservers() {
        vm.isPlaying.observe(this) { isPlay ->
            b.exoPlayer.keepScreenOn = isPlay
            b.btPlay.setImageResource(
                if (isPlay) R.drawable.ic_pause
                else R.drawable.ic_play
            )
        }
        vm.isControls.observe(this) {
            if (it) showUI()
            else hideUI()
        }
        vm.isBuffering.observe(this) {
            b.progBuffering.visibility = if (it) View.VISIBLE else View.GONE
            b.btPlay.visibility = if (!it) View.VISIBLE else View.INVISIBLE
        }
        vm.playerState.observe(this) { state ->
            when (state!!) {
                PlayingState.INITIAL -> {
                    lifecycleScope.launch {
                        val bmp: Bitmap? = try {
                            vm.movieModel.poster.urlToBitmap(this@PlayerActivity)
                        } catch (_: Exception) {
                            null
                        }
                        bmp?.let {
                            b.imgThumb.setImageBitmap(it)
                            val colorAccent = getVibrantCol(it)
                            vm.colAccent = colorAccent
                            b.seekBar.setAccentColor(colorAccent)
                            b.gestureView.setAccentColor(colorAccent)
                            b.chipSpeed.setAccentColor(colorAccent)
                            b.chipSkip.setAccentColor(colorAccent)
                        }
                    }
                }

                PlayingState.LOADED -> {

                }

                PlayingState.ERROR -> {

                }

                PlayingState.PLAYING -> {
                    b.imgThumb.visibility = View.GONE
                    hideUI()
                }

                PlayingState.ENDED -> {

                }
            }
        }
        vm.seekBarPosition.observe(this) { durations ->
            b.seekBar.progress = durations.first.takeIf { it.isFinite() } ?: 0f
            b.seekBar.secondaryProgress = durations.second.takeIf { it.isFinite() } ?: 0f
        }
        vm.timeText.observe(this) { timePair ->
            b.tvTimerCurrent.text = timePair.first
            b.tvTimerTotal.text = timePair.second
        }


        vm.isSidePanel.observe(this) {
            if (it == null) return@observe
            if (it.first == 0) {
                b.sidePanel.animate().apply {
                    val width = b.sidePanel.width.toFloat()
                    translationX(width.plus(width.times(.2f)))
                    duration = 400
                    start()
                }
                b.sidePanel.removeAllViews()
                return@observe
            }
            when (it.first) {
                1 -> loadQualityPanel(it.second)
                2 -> loadAudioPanel(it.second)
                3 -> loadSubPanel(it.second)
            }
            b.sidePanel.animate().apply {
                translationX(0f)
                interpolator = OvershootInterpolator(1.2f)
                start()
            }
        }
        vm.isSpeed.observe(this) {
            if (it == true) b.llSpeed.animate().apply {
                translationX(0f)
                start()
            } else if (it == false) {
                b.llSpeed.animate().apply {
                    val width = b.llSpeed.width.toFloat()
                    translationX(width.plus(width.times(.2f)))
                    start()
                }
                setSpeedUi()
            }
        }
        setSpeedUi()

        vm.isSkip.observe(this) {
//            b.btSkip.visibility = if (it) View.VISIBLE  else View.GONE
            if (it) b.btSkip.animate().apply {
                translationX(0f)
                interpolator = OvershootInterpolator(1.4f)
                start()
            } else {
                b.btSkip.animate().apply {
                    translationX(280.dp.value)
                    start()
                }
                setSpeedUi()
            }
        }

        vm.scaleType.observe(this) {
            when (it) {
                -1 -> b.tvSize.visibility = View.GONE
                0 -> {
                    b.tvSize.visibility = View.VISIBLE
                    b.exoPlayer.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    if (vm.playerState.value == PlayingState.PLAYING || vm.playerState.value == PlayingState.LOADED)
                        "FIT SCREEN".also { s -> b.tvSize.text = s }
                }

                1 -> {
                    b.tvSize.visibility = View.VISIBLE
                    b.exoPlayer.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                    if (vm.playerState.value == PlayingState.PLAYING || vm.playerState.value == PlayingState.LOADED)
                        "STRETCH".also { s -> b.tvSize.text = s }
                }

                2 -> {
                    b.tvSize.visibility = View.VISIBLE
                    b.exoPlayer.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    if (vm.playerState.value == PlayingState.PLAYING || vm.playerState.value == PlayingState.LOADED)
                        "CROP".also { s -> b.tvSize.text = s }
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun setupPlayerControls() {
        b.tvName.text = vm.movieModel.name
        b.btBack.setOnClickListener(this)
        b.btPlay.setOnClickListener(this)
        b.btNext.setOnClickListener(this)
        b.btPrev.setOnClickListener(this)

        b.btAudio.setOnClickListener(this)
        b.btSubtitle.setOnClickListener(this)
        b.btScaling.setOnClickListener(this)

        b.btVideoQuality.setOnClickListener(this)
        b.btSpeed.setOnClickListener(this)
        b.btSkip.setOnClickListener(this)

        b.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekChanged {
            override fun onStartTrackingTouch() {
                vm.showControls()
            }

            override fun onProgressChanged(prog: Float) {
                if (vm.exoPlayer.duration <= 0f) return
                val tempCurrentPos = vm.exoPlayer.duration.times(prog).roundToLong()
                b.tvTimerCurrent.text = tempCurrentPos.timeTextFromMs()
            }

            override fun onStopTrackingTouch(finalProg: Float) {
                if (vm.exoPlayer.duration <= 0f) return
                vm.exoPlayer.seekTo(finalProg.times(vm.exoPlayer.duration).toLong())
                vm.hideControls(1000)
            }
        })

        b.gestureView.setVideoGestureLis(object : GestureView.VideoGestureListener {
            override fun onSingleTap() {
                if (vm.isSidePanel.value?.first != 0) {
                    vm.hideSidePanel()
                    b.sidePanel.removeAllViews()
                    return
                }
                if (vm.isSpeed.value == true) {
                    vm.changeSpeed()
                    return
                }

                if (vm.isControls.value == true)
                    vm.hideControls(0)
                else vm.showControls().also { vm.hideControls() }
            }

            @OptIn(UnstableApi::class)
            override fun onDoubleTap(isForward: Boolean) {
                if (isForward) vm.skipForward(10_000)
                else vm.skipBackward(10_000)
            }

            @SuppressLint("InlinedApi")
            @OptIn(UnstableApi::class)
            override fun onVolume(increase: Boolean) {
                vm.hideControls(0)
                b.gestureView.performHaptic(HapticFeedbackConstants.CONFIRM)
                if (increase)
                    audioMan.adjustVolume(
                        AudioManager.ADJUST_RAISE,
                        AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
                    )
                else
                    audioMan.adjustVolume(
                        AudioManager.ADJUST_LOWER,
                        AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
                    )
            }

            override fun onBrightness(bright: Float) {
                vm.hideControls(0)
                val prams = window.attributes
                prams.screenBrightness = bright
                window.attributes = prams
            }

            override fun onLongPress(isDown: Boolean) {
                vm.exoPlayer.setPlaybackSpeed(if (isDown) 2f else vm.playSpeed)
                b.ll2xHelper.visibility = if (isDown) View.VISIBLE else View.GONE
            }

            override fun requestVolSync() {
                val vol = getCurrentAndMaxVol()
                b.gestureView.setVolAndBrightNess(
                    vol.first,
                    vol.second,
                    window.attributes.screenBrightness.takeIf { it >= 0f } ?: 0.5f
                )
            }

        })

    }

    private fun getCurrentAndMaxVol(): Pair<Int, Int> {
        return audioMan.getStreamVolume(AudioManager.STREAM_MUSIC) to
                audioMan.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    }

    //endregion SETUP

    //region ANIMATE CONTROLS UI

    private fun showUI() {
        b.btSpeed.isClickable = true
        b.btAudio.isClickable = true
        b.btVideoQuality.isClickable = true
        b.btSubtitle.isClickable = true
        b.gestureView.setVisi(true)
        b.seekBar.showAnim()

        b.llTopBar.animate().apply {
            translationY(0f)
            start()
        }
        b.llBottomControls.animate().apply {
            translationY(0f)
            start()
        }
        b.llCenter.visibility = View.VISIBLE
        b.tvTimerCurrent.visibility = View.VISIBLE
        b.tvTimerTotal.visibility = View.VISIBLE
        statusBarVisi(true)
    }

    private fun hideUI() {
        b.btSpeed.isClickable = false
        b.btAudio.isClickable = false
        b.btVideoQuality.isClickable = false
        b.btSubtitle.isClickable = false
        b.gestureView.setVisi(false)
        b.seekBar.hideAnim()

        b.llTopBar.animate().apply {
            translationY(-b.llTopBar.height.toFloat())
            start()
        }
        b.llBottomControls.animate().apply {
            translationY(b.llBottomControls.height.toFloat())
            start()
        }
        b.llCenter.visibility = View.GONE
        b.tvTimerCurrent.visibility = View.GONE
        b.tvTimerTotal.visibility = View.GONE
        statusBarVisi(false)
    }

    //endregion ANIMATE CONTROLS UI

    //region LOAD SIDE VIEWS

    @OptIn(UnstableApi::class)
    private val speedClickLis = View.OnClickListener { v ->
        try {
            vm.changeSpeed()
            vm.setPlaybackSpeed(v.tag.toString().toFloat())
        } catch (_: Exception) {

        }
    }

    @OptIn(UnstableApi::class)
    private fun loadSpeedPanel() {
        val bSpeed = PanelSpeedBinding.inflate(layoutInflater)
        bSpeed.seekSpeed.setOnSeekBarChangeListener(object :
            android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                view: android.widget.SeekBar?,
                progress: Int,
                isUser: Boolean
            ) {
                if (isUser) {
                    val speed = progress.div(100f).times(3.75f).plus(0.25f)
                    vm.setPlaybackSpeed(speed)
                    String.format(Locale.getDefault(), "%.2f x", speed)
                        .also { (bSpeed.root.getChildAt(0) as TextView).text = it }
                }
            }

            override fun onStartTrackingTouch(p0: android.widget.SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: android.widget.SeekBar?) {
            }


        })
        (bSpeed.root.getChildAt(3) as LinearLayout)
            .children.forEach { it.setOnClickListener(speedClickLis) }
        b.llSpeed.addView(bSpeed.root)
    }

    @OptIn(UnstableApi::class)
    private val qualityClicks = View.OnClickListener { v ->
        try {
            val trackInfo = v.tag as Pair<*, *>
            vm.selectVideoTrack(
                trackInfo.second as TrackInfo,
                trackInfo.first as Int
            )
        } catch (_: Exception) {
        }
    }

    @OptIn(UnstableApi::class)
    private fun loadQualityPanel(list: List<TrackInfo>) {
        val newList = mutableListOf<TrackInfo>()
        val isAnySel = run {
            for (i in list.indices) {
                if (list[i].isSelected) return@run true
            }
            return@run false
        }
        newList.add(TrackInfo(-1, -1, "Auto", !isAnySel))
        try {
            newList.addAll(list)
        } catch (_: Exception) {
        }
        newList.forEach { mod ->
            val row = RowSidePanelBinding.inflate(layoutInflater)
            row.tvQuality.text = mod.language
            row.root.tag = (list.size - 1) to mod
            row.root.setOnClickListener(qualityClicks)
            if (mod.isSelected) row.indi.setCardBackgroundColor(vm.colAccent)
            b.sidePanel.addView(row.root)
        }
    }

    //endregion LOAD SIDE VIEWS

    //region GEN DIA
    @OptIn(UnstableApi::class)
    private val audioClickLis = View.OnClickListener { v ->
        try {
            val trackInfo = v.tag as TrackInfo
            if (trackInfo.trackIndex == -1)
                vm.selectAudioTrack(null)
            vm.selectAudioTrack(trackInfo)
        } catch (_: Exception) {
        }
    }

    @OptIn(UnstableApi::class)
    private val subClickLis = View.OnClickListener { v ->
        try {
            val trackInfo = v.tag as TrackInfo
            if (trackInfo.trackIndex == -1)
                vm.selectSubtitleTrack(null)
            vm.selectSubtitleTrack(trackInfo)
        } catch (_: Exception) {
        }
    }

    @OptIn(UnstableApi::class)
    private fun loadAudioPanel(list: List<TrackInfo>) {
        val newList = mutableListOf<TrackInfo>()

        newList.add(TrackInfo(-1, -1, "Disable", !list.any { it.isSelected }))
        try {
            newList.addAll(list)
        } catch (_: Exception) {
        }
        newList.forEach { mod ->
            val row = RowSidePanelBinding.inflate(layoutInflater)
            row.tvQuality.text = mod.language
            row.root.tag = mod
            row.root.setOnClickListener(audioClickLis)
            if (mod.isSelected) row.indi.setCardBackgroundColor(vm.colAccent)
            b.sidePanel.addView(row.root)
        }
    }

    @OptIn(UnstableApi::class)
    private fun loadSubPanel(list: List<TrackInfo>) {
        val newList = mutableListOf<TrackInfo>()
        newList.add(TrackInfo(-1, -1, "Disable", !list.any { it.isSelected }))
        try {
            newList.addAll(list)
        } catch (_: Exception) {
        }
        newList.forEach { mod ->
            val row = RowSidePanelBinding.inflate(layoutInflater)
            row.tvQuality.text = mod.language
            row.root.tag = mod
            row.root.setOnClickListener(subClickLis)
            if (mod.isSelected) row.indi.setCardBackgroundColor(vm.colAccent)
            b.sidePanel.addView(row.root)
        }
    }

    //endregion GEN DIA

    private fun setSpeedUi() {
        fun formatFloat(value: Float): String {
            val df = DecimalFormat("#.## x")
            df.isDecimalSeparatorAlwaysShown = false
            return df.format(value)
        }
        if (vm.playSpeed == 1f) {
            b.chipSpeed.visibility = View.GONE
            b.tvSpeed.visibility = View.GONE
        } else {
            b.chipSpeed.visibility = View.VISIBLE
            b.tvSpeed.visibility = View.VISIBLE
            formatFloat(vm.playSpeed).also {
                b.tvSpeed.text = it
            }
        }
    }

    private fun statusBarVisi(visible: Boolean) {
        if (visible) {
            // Show the status bar
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                window.insetsController?.show(WindowInsets.Type.statusBars())
                return
            }
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            actionBar?.show()
            return
        }
        // Hide the status bar
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
            return
        }
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        actionBar?.hide()
    }
}