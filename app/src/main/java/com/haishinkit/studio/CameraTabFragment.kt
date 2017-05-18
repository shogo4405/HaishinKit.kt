package com.haishinkit.studio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.support.v4.app.Fragment
import android.widget.Button
import com.haishinkit.rtmp.RTMPConnection
import com.haishinkit.rtmp.RTMPStream
import com.haishinkit.events.IEventListener
import com.haishinkit.media.Camera
import com.haishinkit.media.Audio
import com.haishinkit.events.Event
import com.haishinkit.util.EventUtils
import com.haishinkit.view.CameraView

class CameraTabFragment: Fragment(), IEventListener {
    private var connection: RTMPConnection? = null
    private var stream: RTMPStream? = null
    private var cameraView: CameraView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connection = RTMPConnection()
        stream = RTMPStream(connection!!)
        stream?.attachCamera(Camera(android.hardware.Camera.open()))
        stream?.attachAudio(Audio())
        connection?.addEventListener("rtmpStatus", this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater!!.inflate(R.layout.fragment_camera, container, false)
        val button = v.findViewById<Button>(R.id.button)
        button.setOnClickListener {
            connection?.connect("rtmp://192.168.11.15/live")
        }
        cameraView = v.findViewById<CameraView>(R.id.camera)
        cameraView?.attachStream(stream!!)
        return v
    }

    override fun handleEvent(event: Event) {
        val data = EventUtils.toMap(event)
        val code = data["code"].toString()
        if (code == RTMPConnection.Code.CONNECT_SUCCESS.rawValue) {
            stream!!.publish("live")
        }
    }

    companion object {
        fun newInstance(): CameraTabFragment {
            return CameraTabFragment()
        }
    }
}
