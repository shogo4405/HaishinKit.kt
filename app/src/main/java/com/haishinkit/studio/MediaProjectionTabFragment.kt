package com.haishinkit.studio

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjectionManager
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.haishinkit.metric.FrameCapture
import com.haishinkit.net.NetStream
import com.haishinkit.rtmp.RtmpConnection
import com.haishinkit.rtmp.RtmpStream
import java.nio.ByteBuffer


class MediaProjectionTabFragment : Fragment(), ServiceConnection {
    private var messenger: Messenger? = null

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_mediaprojection, container, false)
        val button = v.findViewById<Button>(R.id.button)
        MediaProjectionService.listener = object : RtmpStream.Listener {
            override fun onCaptureOutput(stream: NetStream, type: Byte, buffer: ByteBuffer, timestamp: Long) {
            }

            override fun onStatics(stream: RtmpStream, connection: RtmpConnection) {
                activity?.runOnUiThread {
                    v.findViewById<TextView>(R.id.fps).text = "${stream.currentFPS}FPS"
                }
            }

            override fun onSetUp(stream: NetStream) {
            }

            override fun onTearDown(stream: NetStream) {
            }
        }
        button.setOnClickListener {
            if (button.text == "Publish") {
                if (messenger == null) {
                    if (Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT) {
                        val mediaProjectionManager = activity?.getSystemService(Service.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CAPTURE)
                    }
                } else {
                    messenger?.send(Message.obtain(null, 0))
                }
                button.text = "Stop"
            } else {
                messenger?.send(Message.obtain(null, 1))
                button.text = "Publish"
            }
        }
        return v
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        activity?.windowManager?.defaultDisplay?.getMetrics(MediaProjectionService.metrics)
        if (Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT) {
            if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
                MediaProjectionService.data = data
                Intent(activity, MediaProjectionService::class.java).also { intent ->
                    if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {
                        activity?.startForegroundService(intent)
                    } else {
                        activity?.startService(intent)
                    }
                    activity?.bindService(intent, this@MediaProjectionTabFragment, Context.BIND_AUTO_CREATE)
                }
                Log.i(toString(), "mediaProjectionManager success")
            }
        }
    }

    override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
        messenger = Messenger(binder)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        messenger = null
    }

    private fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    companion object {
        fun newInstance(): MediaProjectionTabFragment {
            return MediaProjectionTabFragment()
        }

        private val TAG = MediaProjectionTabFragment::class.java.simpleName
        private const val REQUEST_CAPTURE = 1
    }
}
