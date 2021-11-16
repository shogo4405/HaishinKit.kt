package com.haishinkit.studio

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import com.haishinkit.vk.VKPixelTransform

class PreferenceTagFragment : Fragment(), Choreographer.FrameCallback {
    private lateinit var holder: SurfaceHolder
    private val renderer = VKPixelTransform()
    private val choreographer = Choreographer.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.let {
            Log.d(TAG, "setAssetManager")
            renderer.setAssetManager(it.assets)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var v = inflater.inflate(R.layout.fragment_preference, container, false)

        val surfaceView = v.findViewById<SurfaceView>(R.id.surface_view)
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                Log.d(TAG, "surfaceCreated")
                renderer.surface = holder.surface
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
            }
        })

        val inputSurfaceView = v.findViewById<SurfaceView>(R.id.input_surface_view)
        inputSurfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                Log.d(TAG, "inputSurfaceCreated")
                renderer.inputSurface = holder.surface
                this@PreferenceTagFragment.holder = holder
                this@PreferenceTagFragment.choreographer.postFrameCallback(this@PreferenceTagFragment)
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
            }
        })

        renderer.startRunning()

        return v
    }

    private fun drawFrame(holder: SurfaceHolder) {
        val canvas = holder.lockCanvas(null)
        canvas.drawColor(Color.WHITE)
        val p = Paint()
        p.color = Color.RED
        p.textSize = 30f
        val rect = Rect(0, 0, 100, 100)
        canvas.drawRect(rect, p)
        val currentTimestamp = System.currentTimeMillis()
        canvas.drawText("$currentTimestamp", 0f, rect.height().toFloat() + 30f, p)
        holder.unlockCanvasAndPost(canvas)
    }

    override fun doFrame(frameTimeNanos: Long) {
        drawFrame(holder)
        choreographer.postFrameCallback(this)
    }

    companion object {
        private const val TAG = "PreferenceTagFragment"

        fun newInstance(): PreferenceTagFragment {
            return PreferenceTagFragment()
        }
    }
}

