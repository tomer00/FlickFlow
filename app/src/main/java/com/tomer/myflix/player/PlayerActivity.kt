package com.tomer.myflix.player

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.media.AudioManager
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.tomer.myflix.R
import com.tomer.myflix.data.models.LinkPair
import com.tomer.myflix.databinding.ActivityExoPlayerBinding
import com.tomer.myflix.databinding.PanelSpeedBinding
import com.tomer.myflix.databinding.TextQualityBinding
import com.tomer.myflix.presentation.ui.views.GestureView
import com.tomer.myflix.presentation.ui.views.SeekBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@AndroidEntryPoint
class PlayerActivity : AppCompatActivity(), View.OnClickListener {

    private val b by lazy { ActivityExoPlayerBinding.inflate(layoutInflater) }
    private val vm by viewModels<PlayerViewModel>()

    private val diaAudio by lazy { genAudioDia() }
    private val diaSubtitle by lazy { genSubDia() }

    private val audioMan by lazy { this.getSystemService(AUDIO_SERVICE) as AudioManager }

    //region LIFE CYCLES

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val model = intent.getStringExtra("data") ?: kotlin.run {
            finish()
            return
        }

        vm.setMovieData(model)

        enableEdgeToEdge()
        setContentView(b.root)
        b.exoPlayer.player = vm.exoPlayer

        setupPlayerControls()
        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        val vol = getCurrentAndMaxVol()
        b.gestureView.setVolAndBrightNess(vol.first, vol.second, 1f)
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
            b.btAudio.id ->
                if (
                    vm.playerState.value!! == PlayingState.LOADED
                    || vm.playerState.value!! == PlayingState.PLAYING
                )
                    diaAudio.show()

            b.btSubtitle.id -> if (vm.playerState.value!! == PlayingState.LOADED
                || vm.playerState.value!! == PlayingState.PLAYING
            )
                diaSubtitle.show()

            b.btSpeed.id -> {
                vm.changeSpeed()
                vm.hideControls(0)
                if (b.llSpeed.childCount == 0) loadSpeedPanel()
                val llS = b.llSpeed.getChildAt(0) as LinearLayout
                String.format(Locale.getDefault(), "%.2f x", vm.playSpeed)
                    .also { (llS.getChildAt(0) as TextView).text = it }

                val prog = vm.playSpeed.minus(.25f).div(3.75f).times(100).roundToInt()
                (llS.getChildAt(1) as AppCompatSeekBar).progress = prog

            }

            b.btQuality.id -> {
                vm.changeQuality()
                vm.hideControls(0)
                if (b.llQuality.childCount == 0) loadQualityPanel()
            }

            else -> {}
        }
    }

    //endregion CLICK LISTENERS

    //region SETUP

    private fun setupObservers() {
        vm.isPlaying.observe(this) { isPlay ->
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
                            vm.movieModel.poster.urlToBitmap(this@PlayerActivity, this)
                        } catch (e: Exception) {
                            null
                        }
                        bmp?.let {
                            b.imgThumb.setImageBitmap(it)
                            val colorAccent = getVibrantCol(it)
                            b.seekBar.setAccentColor(colorAccent)
                            b.gestureView.setAccentColor(colorAccent)
                        }
                    }
                }

                PlayingState.LOADED -> {

                }

                PlayingState.ERROR -> {

                }

                PlayingState.PLAYING -> {
                    b.imgThumb.visibility = View.GONE
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
        vm.isQuality.observe(this) {
            if (it == true) b.llQuality.animate().apply {
                translationX(0f)
                start()
            } else if (it == false) b.llQuality.animate().apply {
                val width = b.llQuality.width.toFloat()
                translationX(width.plus(width.times(.2f)))
                start()
            }
        }
        vm.isSpeed.observe(this) {
            if (it == true) b.llSpeed.animate().apply {
                translationX(0f)
                start()
            } else if (it == false) b.llSpeed.animate().apply {
                val width = b.llSpeed.width.toFloat()
                translationX(width.plus(width.times(.2f)))
                start()
            }.also {
                String.format(Locale.getDefault(), "%.2f x", vm.playSpeed)
                    .also { s -> b.tvSpeed.text = s }
            }
        }
        String.format(Locale.getDefault(), "%.2f x", vm.playSpeed).also { b.tvSpeed.text = it }

        vm.scaleType.observe(this) {
            when (it) {
                0 -> {
                    b.exoPlayer.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    if (vm.playerState.value == PlayingState.PLAYING || vm.playerState.value == PlayingState.LOADED)
                        Toast.makeText(this, "Resize Mode: FIT SCREEN", Toast.LENGTH_SHORT).show()
                }

                1 -> {
                    b.exoPlayer.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                    if (vm.playerState.value == PlayingState.PLAYING || vm.playerState.value == PlayingState.LOADED)
                        Toast.makeText(this, "Resize Mode: STRETCH", Toast.LENGTH_SHORT).show()
                }

                2 -> {
                    b.exoPlayer.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    if (vm.playerState.value == PlayingState.PLAYING || vm.playerState.value == PlayingState.LOADED)
                        Toast.makeText(this, "Resize Mode: CROP", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupPlayerControls() {
        b.tvName.text = vm.movieModel.name
        b.btBack.setOnClickListener(this)
        b.btPlay.setOnClickListener(this)
        b.btNext.setOnClickListener(this)
        b.btPrev.setOnClickListener(this)

        b.btAudio.setOnClickListener(this)
        b.btSubtitle.setOnClickListener(this)
        b.btScaling.setOnClickListener(this)

        b.btQuality.setOnClickListener(this)
        b.btSpeed.setOnClickListener(this)

        b.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekChanged {
            override fun onStartTrackingTouch() {
                vm.showControls()
            }

            override fun onProgressChanged(prog: Float) {
                if (vm.videoDuration <= 0f) return
                val tempCurrentPos = vm.videoDuration.times(prog).roundToLong()
                b.tvTimerCurrent.text = tempCurrentPos.timeTextFromMs()
            }

            override fun onStopTrackingTouch(finalProg: Float) {
                if (vm.videoDuration <= 0L)
                    vm.videoDuration = 1000.times(finalProg).roundToLong().times(-1L)
                else vm.exoPlayer.seekTo(finalProg.times(vm.videoDuration).toLong())
                vm.hideControls(1000)
            }
        })

        b.gestureView.setVideoGestureLis(object : GestureView.VideoGestureListener {
            override fun onSingleTap() {
                if (vm.isQuality.value == true)
                    vm.changeQuality()
                if (vm.isSpeed.value == true)
                    vm.changeSpeed()

                if (vm.isControls.value == true)
                    vm.hideControls(0)
                else vm.showControls().also { vm.hideControls() }
            }

            override fun onDoubleTap(isForward: Boolean) {
                if (isForward) vm.skipForward(10_000)
                else vm.skipBackward(10_000)
            }

            @SuppressLint("InlinedApi")
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
                vm.setPlaybackSpeed(if (isDown) 2f else vm.playSpeed)
                b.ll2xHelper.visibility = if (isDown) View.VISIBLE else View.GONE
            }

            override fun requestVolSync() {
                val vol = getCurrentAndMaxVol()
                b.gestureView.setVolAndBrightNess(vol.first, vol.second, 1f)
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
    }

    private fun hideUI() {
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
    }

    //endregion ANIMATE CONTROLS UI

    //region LOAD SIDE VIEWS

    private val speedClickLis = View.OnClickListener { v ->
        try {
            vm.changeSpeed()
            vm.setPlaybackSpeed(v.tag.toString().toFloat())
        } catch (_: Exception) {

        }
    }

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

    private val qualityClicks = View.OnClickListener { v ->
        try {
            vm.changeQuality()
            val link = v.tag as LinkPair
            vm.setQuality(link)
        } catch (_: Exception) {

        }
    }

    private fun loadQualityPanel() {
        fun addQuality(mod: LinkPair) {
            val tvQuality = TextQualityBinding.inflate(layoutInflater).root
            tvQuality.apply {
                this.tag = mod
                this.text = mod.disName
                this.setOnClickListener(qualityClicks)
            }
            b.llQuality.addView(tvQuality)
        }
        addQuality(LinkPair("AUTO", "", ""))
        vm.movieModel.links.forEach { mod ->
            addQuality(mod)
        }
    }

    //endregion LOAD SIDE VIEWS

    //region GEN DIA

    private fun genAudioDia(): AlertDialog {
        val list = mutableListOf<PlayerViewModel.TrackInfo>()
        list.add(PlayerViewModel.TrackInfo(0, 0, "Disable"))
        try {
            list.addAll(vm.getAudioTrackInfo(vm.trackSelector.currentMappedTrackInfo!!))
        } catch (_: Exception) {
        }
        return AlertDialog.Builder(this).apply {
            setTitle("Available Audio Tracks")
            setItems(list.map { it.language }.toTypedArray()) { dia, which ->
                if (which == 0)
                    vm.selectAudioTrack(null)
                else vm.selectAudioTrack(list.getOrNull(which))
            }
        }.create()
    }


    private fun genSubDia(): AlertDialog {
        val list = mutableListOf<PlayerViewModel.TrackInfo>()
        list.add(PlayerViewModel.TrackInfo(0, 0, "Disable"))
        try {
            list.addAll(vm.getSubtitleTrackInfo(vm.trackSelector.currentMappedTrackInfo!!))
        } catch (_: Exception) {
        }
        return AlertDialog.Builder(this).apply {
            setTitle("Available Subtitles")
            setItems(list.map { it.language }.toTypedArray()) { dia, which ->
                if (which == 0)
                    vm.selectSubtitleTrack(null)
                else vm.selectSubtitleTrack(list.getOrNull(which))
            }
        }.create()
    }


    //endregion GEN DIA

}