package com.haishinkit.studio

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.support.v4.app.Fragment
import android.widget.Button
import com.haishinkit.rtmp.RTMPConnection
import com.haishinkit.rtmp.RTMPStream
import com.haishinkit.events.IEventListener
import com.haishinkit.media.CameraSource
import com.haishinkit.events.Event
import com.haishinkit.events.EventUtils
import com.haishinkit.view.CameraView
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.support.v4.content.ContextCompat
import android.util.Log
import com.haishinkit.media.AudioRecordSource

class CameraTabFragment: Fragment(), IEventListener {
    private var connection: RTMPConnection? = null
    private var stream: RTMPStream? = null
    private var cameraView: CameraView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val permissionCheck = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), 1)
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }
        connection = RTMPConnection()
        stream = RTMPStream(connection!!)
        stream?.attachAudio(AudioRecordSource())

        val manager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        var camera = CameraSource(manager).apply {
            this.open(cameraId)
        }
        stream?.attachCamera(camera)

        connection?.addEventListener("rtmpStatus", this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_camera, container, false)
        val button = v.findViewById<Button>(R.id.button)
        button.setOnClickListener {
            connection?.connect(Preference.shared.rtmpURL)
        }
        cameraView = v.findViewById<CameraView>(R.id.camera)
        cameraView?.attachStream(stream!!)
        return v
    }

    override fun onDestroy() {
        super.onDestroy()
        connection?.dispose()
    }

    override fun handleEvent(event: Event) {
        Log.i(javaClass.name + "#handleEvent", event.toString())
        val data = EventUtils.toMap(event)
        val code = data["code"].toString()
        if (code == RTMPConnection.Code.CONNECT_SUCCESS.rawValue) {
            stream?.publish(Preference.shared.streamName)
        }
    }

    companion object {
        fun newInstance(): CameraTabFragment {
            return CameraTabFragment()
        }
    }
}
