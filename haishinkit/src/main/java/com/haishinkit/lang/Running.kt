package com.haishinkit.lang

import java.util.concurrent.atomic.AtomicBoolean

/**
 * An interface that methods for running.
 */
interface Running {
    /**
     * Indicates whether the receiver is running.
     */
    val isRunning: AtomicBoolean

    /**
     * Tells the receiver to start running.
     */
    fun startRunning()

    /**
     * Tells the receiver to stop running.
     */
    fun stopRunning()
}
