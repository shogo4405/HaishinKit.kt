package com.haishinkit.lang

import java.util.concurrent.atomic.AtomicBoolean

interface Running {
    val isRunning: AtomicBoolean
    fun startRunning()
    fun stopRunning()
}
