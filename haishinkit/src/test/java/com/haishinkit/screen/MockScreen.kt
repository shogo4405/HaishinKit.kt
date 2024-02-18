package com.haishinkit.screen

import android.content.Context
import android.graphics.Bitmap

class MockScreen(applicationContext: Context) : Screen(applicationContext) {
    override fun readPixels(lambda: (bitmap: Bitmap?) -> Unit) {
    }

    override fun bind(screenObject: ScreenObject) {
    }

    override fun unbind(screenObject: ScreenObject) {
    }
}
