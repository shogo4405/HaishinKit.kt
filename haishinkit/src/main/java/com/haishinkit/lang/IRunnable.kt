package com.haishinkit.lang

interface IRunnable {
    val isRunning: Boolean
    fun startRunning()
    fun stopRunning()
}
