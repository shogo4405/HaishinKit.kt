package com.haishinkit.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.haishinkit.event.Event
import com.haishinkit.event.EventUtils
import com.haishinkit.event.IEventListener
import com.haishinkit.graphics.VideoGravity
import com.haishinkit.media.StreamDrawable
import com.haishinkit.rtmp.RtmpConnection
import com.haishinkit.rtmp.RtmpStream

class PlaybackTabFragment : Fragment(), IEventListener {
    private lateinit var connection: RtmpConnection
    private lateinit var stream: RtmpStream
    private lateinit var playbackView: StreamDrawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        connection = RtmpConnection()
        stream = RtmpStream(requireContext(), connection)

        connection.addEventListener(Event.RTMP_STATUS, this)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val v = inflater.inflate(R.layout.fragment_playback, container, false)
        val button = v.findViewById<Button>(R.id.button)
        button.setOnClickListener {
            if (button.text == "Play") {
                connection.connect(Preference.shared.rtmpURL)
                button.text = "Stop"
            } else {
                connection.close()
                button.text = "Play"
            }
        }

        playbackView = if (Preference.useSurfaceView) {
            v.findViewById(R.id.surfaceView)
        } else {
            v.findViewById(R.id.textureView)
        }
        playbackView.videoGravity = VideoGravity.RESIZE_ASPECT
        playbackView.attachStream(stream)

        return v
    }

    override fun onResume() {
        super.onResume()
        stream.receiveVideo = true
    }

    override fun onPause() {
        super.onPause()
        stream.receiveVideo = false
    }

    override fun onDestroy() {
        super.onDestroy()
        connection.dispose()
    }

    override fun handleEvent(event: Event) {
        Log.i("$TAG#handleEvent", event.toString())
        val data = EventUtils.toMap(event)
        val code = data["code"].toString()
        if (code == RtmpConnection.Code.CONNECT_SUCCESS.rawValue) {
            stream.play(Preference.shared.streamName)
        }
    }

    companion object {
        fun newInstance(): PlaybackTabFragment {
            return PlaybackTabFragment()
        }

        private val TAG = PlaybackTabFragment::class.java.simpleName
    }
}
