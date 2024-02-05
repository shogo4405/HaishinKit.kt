package com.haishinkit.screen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.annotation.RawRes
import com.airbnb.lottie.ImageAssetDelegate
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.LottieListener
import com.airbnb.lottie.LottieTask
import java.lang.ref.WeakReference

/**
 * An object that manages offscreen rendering an lottie source.
 */
@Suppress("MemberVisibilityCanBePrivate", "UNUSED")
class LottieScreen(val context: Context) : Image() {
    /**
     * Wrapper for LottieDrawable#isAnimating.
     */
    val isAnimating: Boolean
        get() = lottieDrawable.isAnimating

    /**
     * Wrapper for LottieDrawable#repeatCount.
     */
    var repeatCount: Int
        get() = lottieDrawable.repeatCount
        set(value) {
            lottieDrawable.repeatCount = value
        }

    /**
     * Wrapper for LottieDrawable#imageAssetsFolder.
     */
    var imageAssetsFolder: String?
        get() = lottieDrawable.imageAssetsFolder
        set(value) {
            lottieDrawable.setImagesAssetsFolder(value)
        }

    /**
     * Wrapper for LottieDrawable#speed.
     */
    var speed: Float
        get() = lottieDrawable.speed
        set(value) {
            lottieDrawable.speed = value
        }

    private var canvas = Canvas(bitmap)
    private var cacheComposition = true
    private var animationName: String? = null

    @RawRes
    private var animationResId: Int = 0
    private var composition: LottieComposition? = null
        set(value) {
            field = value
            lottieDrawable.composition = value
            invalidateLayout()
        }
    private var compositionTask: LottieTask<LottieComposition>? = null
        set(value) {
            val result = value?.result
            if (result != null && result.value == composition) return
            field = value?.addListener(loadedListener)?.addFailureListener(failureListener)
        }
    private val lottieDrawable: LottieDrawable by lazy {
        LottieDrawable().apply {
            repeatCount = LottieDrawable.INFINITE
            repeatMode = LottieDrawable.RESTART
        }
    }
    private var failureListener: LottieListener<Throwable>? = null
    private val loadedListener: LottieListener<LottieComposition?> by lazy {
        WeakSuccessListener(
            this
        )
    }

    /**
     * Setter for animation from a file in the raw directory.
     */
    fun setAnimation(@RawRes rawRes: Int) {
        animationResId = rawRes
        animationName = null
        compositionTask = fromRawRes(rawRes)
    }

    /**
     * Wrapper for LottieDrawable#playAnimation().
     */
    fun playAnimation() {
        lottieDrawable.playAnimation()
        invalidateLayout()
    }

    /**
     * Wrapper for LottieDrawable#cancelAnimation().
     */
    fun cancelAnimation() {
        lottieDrawable.cancelAnimation()
    }

    /**
     * Wrapper for LottieDrawable#pauseAnimation().
     */
    fun pauseAnimation() {
        lottieDrawable.pauseAnimation()
    }

    /**
     * Wrapper for LottieDrawable#setImageAssetDelegate.
     */
    fun setImageAssetDelegate(assetDelegate: ImageAssetDelegate) {
        lottieDrawable.setImageAssetDelegate(assetDelegate)
    }

    override fun layout(renderer: Renderer) {
        val composition = composition ?: return
        if (bitmap.width != composition.bounds.width() && bitmap.height != composition.bounds.height()) {
            bitmap =
                Bitmap.createBitmap(
                    composition.bounds.width(),
                    composition.bounds.height(),
                    Bitmap.Config.ARGB_8888
                )
            canvas = Canvas(bitmap)
        }
        bitmap.eraseColor(Color.TRANSPARENT)
        lottieDrawable.draw(canvas)
        super.layout(renderer)
        if (lottieDrawable.isAnimating) {
            invalidateLayout()
        }
    }

    private fun fromRawRes(@RawRes rawRes: Int): LottieTask<LottieComposition>? {
        return if (cacheComposition) LottieCompositionFactory.fromRawRes(
            context, rawRes
        ) else LottieCompositionFactory.fromRawRes(
            context, rawRes, null
        )
    }

    private class WeakSuccessListener(target: LottieScreen) :
        LottieListener<LottieComposition?> {
        private val targetReference = WeakReference(target)

        override fun onResult(result: LottieComposition?) {
            val targetScreen = targetReference.get() ?: return
            targetScreen.composition = result
        }
    }

    private class WeakFailureListener(target: LottieScreen) : LottieListener<Throwable> {
        private val targetReference = WeakReference(target)
        override fun onResult(result: Throwable) {
            val targetScreen = targetReference.get() ?: return
            val listener =
                if (targetScreen.failureListener == null) DEFAULT_FAILURE_LISTENER else targetScreen.failureListener
            listener?.onResult(result)
        }
    }

    companion object {
        private val TAG = LottieScreen::class.java.simpleName

        private val DEFAULT_FAILURE_LISTENER =
            LottieListener<Throwable> { throwable: Throwable? ->
                throw IllegalStateException("Unable to parse composition", throwable)
            }
    }
}
