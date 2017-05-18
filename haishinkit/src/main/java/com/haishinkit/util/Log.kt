package com.haishinkit.util

object Log {
    private var adapter: ILogAdapter = SystemLogAdapter()

    init {
        val runtime = System.getProperty("java.runtime.name")
        if (0 <= runtime.indexOf("Android")) {
            adapter = AndroidLogAdapter()
        }
    }

    fun v(tag: String, msg: String): Int {
        return adapter.v(tag, msg)
    }

    fun v(tag: String, msg: String, tr: Throwable): Int {
        return adapter.v(tag, msg, tr)
    }

    fun d(tag: String, msg: String): Int {
        return adapter.v(tag, msg)
    }

    fun d(tag: String, msg: String, tr: Throwable): Int {
        return adapter.d(tag, msg, tr)
    }

    fun i(tag: String, msg: String): Int {
        return adapter.i(tag, msg)
    }

    fun i(tag: String, msg: String, tr: Throwable): Int {
        return adapter.i(tag, msg, tr)
    }

    fun w(tag: String, msg: String): Int {
        return adapter.w(tag, msg)
    }

    fun w(tag: String, msg: String, tr: Throwable): Int {
        return adapter.w(tag, msg, tr)
    }

    fun w(tag: String, tr: Throwable): Int {
        return adapter.w(tag, tr)
    }

    fun e(tag: String, msg: String): Int {
        return adapter.e(tag, msg)
    }

    fun e(tag: String, msg: String, tr: Throwable): Int {
        return adapter.e(tag, msg, tr)
    }
}