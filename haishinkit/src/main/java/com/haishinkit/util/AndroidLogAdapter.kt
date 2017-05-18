package com.haishinkit.util

import android.util.Log

class AndroidLogAdapter : ILogAdapter {
    override fun v(tag: String, msg: String): Int {
        return Log.v(tag, msg)
    }

    override fun v(tag: String, msg: String, tr: Throwable): Int {
        return Log.v(tag, msg, tr)
    }

    override fun d(tag: String, msg: String): Int {
        return Log.d(tag, msg)
    }

    override fun d(tag: String, msg: String, tr: Throwable): Int {
        return Log.d(tag, msg, tr)
    }

    override fun i(tag: String, msg: String): Int {
        return Log.i(tag, msg)
    }

    override fun i(tag: String, msg: String, tr: Throwable): Int {
        return Log.i(tag, msg, tr)
    }

    override fun w(tag: String, msg: String): Int {
        return Log.w(tag, msg)
    }

    override fun w(tag: String, msg: String, tr: Throwable): Int {
        return Log.w(tag, msg, tr)
    }

    override fun w(tag: String, tr: Throwable): Int {
        return Log.w(tag, tr)
    }

    override fun e(tag: String, msg: String): Int {
        return Log.e(tag, msg)
    }

    override fun e(tag: String, msg: String, tr: Throwable): Int {
        return Log.e(tag, msg, tr)
    }
}
