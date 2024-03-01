package com.haishinkit.media

import android.content.Context
import android.media.MediaMuxer
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.haishinkit.codec.AudioCodec
import com.haishinkit.codec.VideoCodec
import java.io.FileDescriptor

/**
 * An object that writes media data to a file.
 *
 * ## Usages.
 * ### AndroidManifest.xml
 * ```xml
 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
 * ```
 * ### Code
 * ```kotlin
 * if (recorder.isRecording) {
 *   recorder.stopRecording()
 * } else {
 *   recorder.attachStream(stream)
 *   recorder.startRecording(
 *     File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "output.mp4").toString(),
 *     MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
 *   )
 * }
 * ```
 */
@Suppress("UNUSED", "MemberVisibilityCanBePrivate")
class MediaRecorder(applicationContext: Context) {
    /**
     * The isRecording value indicates whether the audio recorder is recording.
     */
    var isRecording = false
        private set

    /**
     * Specifies the video codec settings.
     */
    val videoSetting: VideoCodec.Setting by lazy {
        VideoCodec.Setting(videoCodec)
    }

    /**
     * Specifies the audio codec settings.
     */
    val audioSetting: AudioCodec.Setting by lazy {
        AudioCodec.Setting(audioCodec)
    }

    private var muxer: com.haishinkit.media.MediaMuxer? = null
    private var stream: Stream? = null
    private val audioCodec by lazy { AudioCodec() }
    private val videoCodec by lazy { VideoCodec(applicationContext) }

    /**
     * Attaches the stream.
     */
    fun attachStream(stream: Stream?) {
        this.stream = stream
        videoCodec.pixelTransform.screen = stream?.screen
    }

    /**
     * Starts recording.
     */
    fun startRecording(
        path: String,
        format: Int,
    ) {
        if (muxer != null || stream == null) {
            throw IllegalStateException()
        }
        Log.i(TAG, "Start recordings to $path.")
        muxer = MediaMuxer(stream, MediaMuxer(path, format))
        startRunning()
    }

    /**
     * Starts recording.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun startRecording(
        fd: FileDescriptor,
        format: Int,
    ) {
        if (muxer != null || stream == null) {
            throw IllegalStateException()
        }
        muxer = MediaMuxer(stream, MediaMuxer(fd, format))
        startRunning()
    }

    /**
     * Stops recording.
     */
    fun stopRecording() {
        if (muxer == null) {
            throw IllegalStateException()
        }
        muxer?.stopRunning()
        stopRunning()
        muxer = null
    }

    private fun startRunning() {
        isRecording = true
        stream?.audioSource?.registerAudioCodec(audioCodec)
        audioCodec.listener = muxer
        audioCodec.startRunning()
        videoCodec.listener = muxer
        videoCodec.startRunning()
    }

    private fun stopRunning() {
        stream?.audioSource?.unregisterAudioCodec(audioCodec)
        audioCodec.stopRunning()
        audioCodec.listener = null
        videoCodec.stopRunning()
        videoCodec.listener = null
        isRecording = false
    }

    private companion object {
        private val TAG = MediaRecorder::class.java.simpleName
    }
}
