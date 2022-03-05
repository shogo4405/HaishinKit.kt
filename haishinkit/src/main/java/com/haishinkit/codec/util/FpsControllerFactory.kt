package com.haishinkit.codec.util

internal class FpsControllerFactory {
    fun create(clazz: Class<*>?): FpsController {
        if (clazz == null) {
            return DefaultFpsController.instance
        }
        return try {
            clazz.newInstance() as FpsController
        } catch (e: java.lang.ClassCastException) {
            DefaultFpsController.instance
        }
    }

    companion object {
        val shared = FpsControllerFactory()
    }
}
