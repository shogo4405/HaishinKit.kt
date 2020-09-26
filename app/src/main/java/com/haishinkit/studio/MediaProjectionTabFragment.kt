package com.haishinkit.studio

import android.util.Log
import android.app.Activity
import android.app.Service
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.haishinkit.events.Event
import com.haishinkit.events.IEventListener
import com.haishinkit.media.AudioRecordSource
import com.haishinkit.media.MediaProjectionSource
import com.haishinkit.rtmp.RTMPConnection
import com.haishinkit.rtmp.RTMPStream
import com.haishinkit.events.EventUtils

class MediaProjectionTabFragment : Fragment(), IEventListener {
    private lateinit var connection: RTMPConnection
    private lateinit var stream: RTMPStream
    private lateinit var mediaProjectionManager: MediaProjectionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT) {
            connection = RTMPConnection()
            connection.addEventListener(Event.RTMP_STATUS, this)
            stream = RTMPStream(connection)
            stream.attachAudio(AudioRecordSource())
            mediaProjectionManager = activity.getSystemService(Service.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CAPTURE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val metrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(metrics)
        Log.i(javaClass.name, metrics.toString())
        if (Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT) {
            if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
                stream.attachCamera(MediaProjectionSource(
                        mediaProjectionManager.getMediaProjection(resultCode, data),
                        metrics
                ))
                Log.i(toString(), "mediaProjectionManager success")
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater!!.inflate(R.layout.fragment_mediaprojection, container, false)
        val button = v.findViewById<Button>(R.id.button)
        button.setOnClickListener {
            connection.connect(Preference.shared.rtmpURL)
        }
        return v
    }

    override fun onDestroy() {
        super.onDestroy()
        connection.dispose()
    }

    override fun handleEvent(event: Event) {
        val data = EventUtils.toMap(event)
        val code = data["code"].toString()
        if (code == RTMPConnection.Code.CONNECT_SUCCESS.rawValue) {
            stream.publish(Preference.shared.streamName)
        }
    }

    companion object {
        private const val REQUEST_CAPTURE = 1

        fun newInstance(): MediaProjectionTabFragment {
            return MediaProjectionTabFragment()
        }
    }
}
