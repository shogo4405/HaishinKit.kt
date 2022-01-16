package com.haishinkit.studio

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import com.haishinkit.graphics.VkPixelTransform
import java.lang.Exception

class PreferenceTagFragment : Fragment(), Choreographer.FrameCallback {
    private lateinit var holderA: SurfaceHolder
    private lateinit var holderB: SurfaceHolder
    private lateinit var surfaceHolderA: SurfaceHolder
    private lateinit var surfaceHolderB: SurfaceHolder
    private var renderer: VkPixelTransform? = VkPixelTransform()
    private val choreographer = Choreographer.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "VkPixelTransform::isSupported() = ${VkPixelTransform.isSupported()}")
        context?.let {
            Log.d(TAG, "setAssetManager")
            renderer?.assetManager = it.assets
        }
    }

    override fun onDestroy() {
        Log.i("Hello", "onDes")
        super.onDestroy()
        renderer = null
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_preference, container, false)

        val surfaceViewA = v.findViewById<SurfaceView>(R.id.surface_view_a)
        surfaceViewA.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                Log.d(TAG, "surfaceCreated")
                surfaceHolderA = holder
                renderer?.surface = holder.surface
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

        val surfaceViewB = v.findViewById<SurfaceView>(R.id.surface_view_b)
        surfaceViewB.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                Log.d(TAG, "surfaceCreated")
                surfaceHolderB = holder
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

        val inputSurfaceViewA = v.findViewById<SurfaceView>(R.id.input_surface_view_a)
        inputSurfaceViewA.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                Log.d(TAG, "inputSurfaceCreated")
                renderer?.inputSurface = holder.surface
                this@PreferenceTagFragment.holderA = holder
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

        val inputSurfaceViewB = v.findViewById<SurfaceView>(R.id.input_surface_view_b)
        inputSurfaceViewB.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                Log.d(TAG, "inputSurfaceCreated")
                this@PreferenceTagFragment.holderB = holder
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

        val button = v.findViewById<Button>(R.id.button)
        button.setOnClickListener { _ ->
            when (button.text) {
                "RED" -> {
                    button.text = "BLUE"
                    renderer?.inputSurface = inputSurfaceViewB.holder.surface
                }
                "BLUE" -> {
                    button.text = "NULL"
                    renderer?.inputSurface = null
                }
                "NULL" -> {
                    button.text = "RED"
                    renderer?.inputSurface = inputSurfaceViewA.holder.surface
                }
            }
        }

        val button2 = v.findViewById<Button>(R.id.button2)
        button2.setOnClickListener { _ ->
            when (button2.text) {
                "LEFT" -> {
                    button2.text = "RIGHT"
                    renderer?.surface = surfaceViewB.holder.surface
                }
                "RIGHT" -> {
                    button2.text = "NULL"
                    renderer?.surface = null
                }
                "NULL" -> {
                    button2.text = "LEFT"
                    renderer?.surface = surfaceViewA.holder.surface
                }
            }
        }

        return v
    }

    override fun doFrame(frameTimeNanos: Long) {
        try {
            drawFrame(holderA, Color.RED)
            drawFrame(holderB, Color.BLUE)
            // renderer?.frameAvailable(null)
        } catch (e: Exception) {
        }
        choreographer.postFrameCallback(this)
    }

    private fun drawFrame(holder: SurfaceHolder, color: Int) {
        val canvas = holder.lockCanvas(null)
        canvas.drawColor(Color.WHITE)
        val p = Paint()
        p.color = color
        p.textSize = 30f
        val rect = Rect(0, 0, 100, 100)
        canvas.drawRect(rect, p)
        val currentTimestamp = System.currentTimeMillis()
        canvas.drawText("$currentTimestamp", 0f, rect.height().toFloat() + 30f, p)
        holder.unlockCanvasAndPost(canvas)
    }

    companion object {
        private const val TAG = "PreferenceTagFragment"

        fun newInstance(): PreferenceTagFragment {
            return PreferenceTagFragment()
        }
    }
}

