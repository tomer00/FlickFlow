package com.tomer.myflix.player

import android.media.AudioManager
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.tomer.myflix.R
import com.tomer.myflix.databinding.ActivityExoPlayerBinding
import com.tomer.myflix.ui.views.GestureView
import com.tomer.myflix.ui.views.SeekBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayerActivity : AppCompatActivity(), View.OnClickListener {

    private val b by lazy { ActivityExoPlayerBinding.inflate(layoutInflater) }
    private val vm by viewModels<PlayerViewModel>()

    private val audioMan by lazy { this.getSystemService(AUDIO_SERVICE) as AudioManager }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    //region CLICK LISTENERS

    override fun onClick(v: View) {
        when (v.id) {
            b.btBack.id -> onBackPressed()

            b.btPlay.id -> {
                if (vm.exoPlayer.isPlaying) {
                    vm.exoPlayer.pause()
                    b.btPlay.setImageResource(R.drawable.ic_play)
                } else {
                    vm.exoPlayer.play()
                    b.btPlay.setImageResource(R.drawable.ic_pause)
                }
            }

            b.btPrev.id -> vm.skipBackward(5_000)
            b.btNext.id -> vm.skipForward(15_000)

            b.btScaling.id -> {}
            b.btFullScreen.id -> {}
            else -> {}
        }
    }
    //endregion CLICK LISTENERS

    //region SETUP

    private fun setupObservers() {
        vm.isControls.observe(this) {
            if (it) showUI()
            else hideUI()
        }
        vm.isBuffering.observe(this) {
            b.progBuffering.visibility = if (it) View.VISIBLE else View.GONE
        }
        vm.isPlaying.observe(this) {

        }
        vm.seekBarPosition.observe(this) { durations ->
            b.seekBar.progress = durations.first.takeIf { it.isFinite() } ?: 0f
            b.seekBar.secondaryProgress = durations.second.takeIf { it.isFinite() } ?: 0f
        }
    }

    private fun setupPlayerControls() {
        b.tvName.text = vm.fileName
        b.btBack.setOnClickListener(this)
        b.btPlay.setOnClickListener(this)
        b.btNext.setOnClickListener(this)
        b.btPrev.setOnClickListener(this)

        b.btScaling.setOnClickListener(this)
        b.btFullScreen.setOnClickListener(this)

        b.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekChanged {
            override fun onStartTrackingTouch() {
                vm.showControls()
            }

            override fun onStopTrackingTouch(finalProg: Float) {
                vm.exoPlayer.seekTo(finalProg.times(vm.videoDuration).toLong())
                vm.hideControls(1000)
            }
        })

        b.gestureView.setVideoGestureLis(object : GestureView.VideoGestureListener {
            override fun onSingleTap() {
                if (vm.isControls.value == true)
                    vm.hideControls(0)
                else vm.showControls().also { vm.hideControls() }
            }

            override fun onDoubleTap(isForward: Boolean) {
                if (isForward) vm.skipForward(10_000)
                else vm.skipBackward(10_000)
            }

            override fun onVolume(increase: Boolean) {
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
                val prams = window.attributes
                prams.screenBrightness = bright
                window.attributes = prams
            }

            override fun onLongPress(isDown: Boolean) {
                vm.exoPlayer.setPlaybackSpeed(if (isDown) 2f else vm.playbackSpeed)
                b.ll2xHelper.visibility = if (isDown) View.VISIBLE else View.GONE
            }

            override fun requestVolSync() {
                val vol = getCurrentAndMaxVol()
                b.gestureView.setVolAndBrightNess(vol.first, vol.second, 1f)
            }

        })

    }

    private fun getCurrentAndMaxVol(): Pair<Int, Int> =
        audioMan.getStreamVolume(AudioManager.STREAM_MUSIC) to audioMan.getStreamMaxVolume(
            AudioManager.STREAM_MUSIC
        )
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
    }

    //endregion ANIMATE CONTROLS UI

}