package com.haishinkit.codec.util

internal class DefaultFpsController : FpsController {
    override fun advanced(timestamp: Long): Boolean {
        return true
    }

    override fun timestamp(timestamp: Long): Long {
        return timestamp
    }

    override fun clear() {
    }

    companion object {
        var instance = DefaultFpsController()
    }
}
