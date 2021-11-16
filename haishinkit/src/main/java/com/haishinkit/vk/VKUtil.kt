package com.haishinkit.vk

object VKUtil {
    init {
        System.loadLibrary("haishinkit")
    }

    external fun isAvailable(): Boolean
    external fun inspectDevices(): String
}
