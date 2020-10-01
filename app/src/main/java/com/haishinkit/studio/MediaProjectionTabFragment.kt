package com.haishinkit.studio

import android.util.Log
import android.app.Activity
import android.app.Service
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.haishinkit.rtmp.RTMPConnection
import com.haishinkit.rtmp.RTMPStream

class MediaProjectionTabFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_mediaprojection, container, false)
        val button = v.findViewById<Button>(R.id.button)
        MediaProjectionService.listener = object : RTMPStream.Listener {
            override fun onStatics(stream: RTMPStream, connection: RTMPConnection) {
                activity.runOnUiThread {
                    v.findViewById<TextView>(R.id.fps).text = "${stream.currentFPS}FPS"
                }
            }
        }
        button.setOnClickListener {
            if (button.text == "Publish") {
                if (Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT) {
                    val mediaProjectionManager = activity.getSystemService(Service.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                    startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CAPTURE)
                }
                button.text = "Stop"
            } else {
                MediaProjectionService.stop(activity)
                button.text = "Publish"
            }
        }
        return v
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        activity.windowManager.defaultDisplay.getMetrics(MediaProjectionService.metrics)
        if (Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT) {
            if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
                MediaProjectionService.data = data
                MediaProjectionService.start(activity)
                Log.i(toString(), "mediaProjectionManager success")
            }
        }
    }

    companion object {
        private const val REQUEST_CAPTURE = 1

        fun newInstance(): MediaProjectionTabFragment {
            return MediaProjectionTabFragment()
        }
    }
}
