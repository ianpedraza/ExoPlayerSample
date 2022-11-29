package com.ianpedraza.exoplayersample.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.ianpedraza.exoplayersample.R
import com.ianpedraza.exoplayersample.databinding.ActivityMainBinding
import com.ianpedraza.exoplayersample.utils.viewBinding

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by viewBinding(ActivityMainBinding::inflate)

    private var exoPlayer: ExoPlayer? = null

    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition = 0L

    private val playbackStateListener: Player.Listener = playbackStateListener()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    private fun initializePlayer() {
        val trackSelector = DefaultTrackSelector(this).apply {
            /** MaxVideoSizeSd -> Standard definition or lower **/
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }

        exoPlayer = ExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .build()
            .also { player ->
                binding.playerView.player = player

                // val mediaItem = MediaItem.fromUri(getString(R.string.media_url_mp4))
                // player.addMediaItem(mediaItem)

                // val secondMediaItem = MediaItem.fromUri(getString(R.string.media_url_mp3))
                // player.addMediaItem(secondMediaItem)

                /**
                 * DASH -> MimeTypes.APPLICATION_MPD
                 * HLS -> MimeTypes.APPLICATION_M3U8
                 * SmoothStreaming -> MimeTypes.APPLICATION_SS
                 **/

                val adaptiveMediaItem = MediaItem.Builder()
                    .setUri(getString(R.string.media_url_dash))
                    .setMimeType(MimeTypes.APPLICATION_MPD)
                    .build()

                player.addMediaItem(adaptiveMediaItem)
            }

        exoPlayer?.apply {
            this.playWhenReady = this@MainActivity.playWhenReady
            seekTo(currentWindow, playbackPosition)
            addListener(playbackStateListener)
            prepare()
        }
    }

    private fun releasePlayer() {
        exoPlayer?.run {
            playbackPosition = this.currentPosition
            currentWindow = this.currentMediaItemIndex
            playWhenReady = this.playWhenReady
            removeListener(playbackStateListener)
            release()
        }
        exoPlayer = null
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.playerView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun showSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(
            window,
            binding.playerView
        ).show(WindowInsetsCompat.Type.systemBars())
    }

    override fun onStart() {
        super.onStart()

        if (Util.SDK_INT >= 24) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()

        if (Util.SDK_INT < 24 || exoPlayer == null) {
            initializePlayer()
            hideSystemUI()
        }
    }

    override fun onPause() {
        super.onPause()

        if (Util.SDK_INT < 24) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()

        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    companion object {
        private const val TAG = "MainActivity"

        private fun playbackStateListener() = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                val stateString: String = when (playbackState) {
                    ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE      -"
                    ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING -"
                    ExoPlayer.STATE_READY -> "ExoPlayer.STATE_READY     -"
                    ExoPlayer.STATE_ENDED -> "ExoPlayer.STATE_ENDED     -"
                    else -> "UNKNOWN_STATE             -"
                }
                Log.d(TAG, "onPlaybackStateChanged: $stateString")
            }
        }
    }
}
