package com.haishinkit.media

import android.media.AudioTrack
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import android.util.Log
import android.view.Choreographer
import com.haishinkit.BuildConfig
import com.haishinkit.codec.AudioCodec
import com.haishinkit.codec.MediaCodec
import com.haishinkit.codec.VideoCodec
import com.haishinkit.lang.Running
import com.haishinkit.metric.FrameTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.IllegalStateException
import java.nio.ByteBuffer
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext

/**
 * MediaLink class can be used to synchronously play audio and video streams.
 */
class MediaLink(val audio: AudioCodec, val video: VideoCodec) :
    Running,
    CoroutineScope,
    Choreographer.FrameCallback {
    data class Buffer(
        val index: Int,
        val payload: ByteBuffer? = null,
        val timestamp: Long = 0L,
        val sync: Boolean = false
    )

    override val isRunning = AtomicBoolean(false)
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    /**
     * The instance of an AudioTrack.
     */
    var audioTrack: AudioTrack? = null
        set(value) {
            syncMode = if (value == null) {
                field?.stop()
                field?.flush()
                field?.release()
                SYNC_MODE_CLOCK
            } else {
                SYNC_MODE_AUDIO
            }
            field = value
        }

    /**
     * Paused audio playback of a media.
     */
    var paused: Boolean
        get() = audioTrack?.playState == AudioTrack.PLAYSTATE_PAUSED
        set(value) {
            when (audioTrack?.playState) {
                AudioTrack.PLAYSTATE_STOPPED -> {
                }
                AudioTrack.PLAYSTATE_PLAYING -> {
                    if (value) {
                        audioTrack?.pause()
                    }
                }
                AudioTrack.PLAYSTATE_PAUSED -> {
                    if (!value) {
                        audioTrack?.play()
                    }
                }
            }
        }

    /**
     * Video are present.
     */
    var hasVideo = false

    /**
     * Audio are present.
     */
    var hasAudio = false

    private var syncMode = SYNC_MODE_CLOCK
    private var hasKeyframe = false
    private val videoBuffers = LinkedBlockingDeque<Buffer>()
    private val audioBuffers = LinkedBlockingDeque<Buffer>()
    private var choreographer: Choreographer? = null
    private var videoTimestamp = Timestamp(1000L)
    private var handler: Handler? = null
        get() {
            if (field == null) {
                val thread =
                    HandlerThread(javaClass.name, android.os.Process.THREAD_PRIORITY_DISPLAY)
                thread.start()
                field = Handler(thread.looper)
            }
            return field
        }
        set(value) {
            field?.looper?.quitSafely()
            field = value
        }
    @Volatile
    private var keepAlive = true
    private var frameTracker: FrameTracker? = null
        get() {
            if (field == null && BuildConfig.DEBUG) {
                field = FrameTracker()
            }
            return field
        }
    private val audioDuration: Long
        get() {
            val track = audioTrack ?: return 0
            return (track.playbackHeadPosition.toLong() * 1000 / track.sampleRate) * 1000L + audioCorrection
        }
    private var audioCorrection = 0L
    private var audioPlaybackJob: Job? = null

    @Synchronized
    override fun startRunning() {
        if (isRunning.get()) return
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "startRunning()")
        }
        audio.mode = MediaCodec.MODE_DECODE
        audio.startRunning()
        video.mode = MediaCodec.MODE_DECODE
        video.startRunning()
        keepAlive = true
        audioPlaybackJob = launch(coroutineContext) {
            doAudio()
        }
        isRunning.set(true)
    }

    @Synchronized
    override fun stopRunning() {
        if (!isRunning.get()) return
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "stopRunning()")
        }
        hasAudio = false
        hasVideo = false
        hasKeyframe = false
        handler = null
        choreographer?.removeFrameCallback(this)
        choreographer = null
        keepAlive = false
        audioPlaybackJob?.cancel()
        audioPlaybackJob = null
        syncMode = SYNC_MODE_CLOCK
        audioCorrection = 0
        videoTimestamp.clear()
        frameTracker?.clear()
        audio.release(audioBuffers)
        audio.stopRunning()
        video.release(videoBuffers)
        video.stopRunning()
        isRunning.set(false)
    }

    /**
     * Queues the audio data asynchronously for playback.
     */
    fun queueAudio(buffer: Buffer) {
        audioBuffers.add(buffer)
        if (!hasVideo) {
            val track = audioTrack ?: return
            if (track.playbackHeadPosition <= 0) {
                if (track.playState != AudioTrack.PLAYSTATE_PLAYING) {
                    track.play()
                }
                return
            }
        }
    }

    /**
     * Queues the video data asynchronously for playback.
     */
    fun queueVideo(buffer: Buffer) {
        videoBuffers.add(buffer)
        if (choreographer == null) {
            handler?.post {
                choreographer = Choreographer.getInstance()
                choreographer?.postFrameCallback(this)
            }
        }
    }

    override fun doFrame(frameTimeNanos: Long) {
        choreographer?.postFrameCallback(this)
        val duration: Long
        if (syncMode == SYNC_MODE_AUDIO) {
            val track = audioTrack ?: return
            if (track.playbackHeadPosition <= 0) {
                if (track.playState != AudioTrack.PLAYSTATE_PLAYING) {
                    track.play()
                    audioCorrection = videoTimestamp.duration
                }
                return
            }
            duration = audioDuration
        } else {
            videoTimestamp.nanoTime = frameTimeNanos
            duration = videoTimestamp.duration
        }
        try {
            val it = videoBuffers.iterator()
            var frameCount = 0
            while (it.hasNext()) {
                val buffer = it.next()
                if (!hasKeyframe) {
                    hasKeyframe = buffer.sync
                }
                if (buffer.timestamp <= duration) {
                    if (frameCount == 0 && hasKeyframe) {
                        frameTracker?.track(FrameTracker.TYPE_VIDEO, SystemClock.uptimeMillis())
                        video.codec?.releaseOutputBuffer(buffer.index, buffer.timestamp * 1000)
                    } else {
                        video.codec?.releaseOutputBuffer(buffer.index, false)
                    }
                    frameCount++
                    it.remove()
                } else {
                    if (VERBOSE && 2 < frameCount) {
                        Log.d(TAG, "droppedFrame: ${frameCount - 1}")
                    }
                    break
                }
            }
        } catch (e: IllegalStateException) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "", e)
            }
        }
    }

    private fun doAudio() {
        while (keepAlive) {
            try {
                val audio = audioBuffers.take()
                val payload = audio.payload ?: continue
                frameTracker?.track(FrameTracker.TYPE_AUDIO, SystemClock.uptimeMillis())
                while (payload.hasRemaining()) {
                    if (keepAlive) {
                        audioTrack?.write(
                            payload,
                            payload.remaining(),
                            AudioTrack.WRITE_NON_BLOCKING
                        )
                    } else {
                        break
                    }
                }
                this.audio.codec?.releaseOutputBuffer(audio.index, false)
            } catch (e: InterruptedException) {
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "", e)
                }
            } catch (e: IllegalStateException) {
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "", e)
                }
            }
        }
    }

    companion object {
        private const val SYNC_MODE_AUDIO = 0
        private const val SYNC_MODE_VSYNC = 1
        private const val SYNC_MODE_CLOCK = 2

        private const val VERBOSE = true
        private val TAG = MediaLink::class.java.simpleName
    }
}
