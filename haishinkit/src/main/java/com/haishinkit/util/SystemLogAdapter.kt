package com.haishinkit.util

class SystemLogAdapter : ILogAdapter {
    override fun v(tag: String, msg: String): Int {
        return println("VERBOSE", tag, msg)
    }

    override fun v(tag: String, msg: String, tr: Throwable): Int {
        return println("VERBOSE", tag, tr.toString())
    }

    override fun d(tag: String, msg: String): Int {
        return println("DEBUG", tag, msg)
    }

    override fun d(tag: String, msg: String, tr: Throwable): Int {
        return println("DEBUG", tag, tr.toString())
    }

    override fun i(tag: String, msg: String): Int {
        return println("INFO", tag, msg)
    }

    override fun i(tag: String, msg: String, tr: Throwable): Int {
        return println("INFO", tag, tr.toString())
    }

    override fun w(tag: String, msg: String): Int {
        return println("WARN", tag, msg)
    }

    override fun w(tag: String, msg: String, tr: Throwable): Int {
        return println("WARN", tag, msg)
    }

    override fun w(tag: String, tr: Throwable): Int {
        return println("WARN", tag, tr.toString())
    }

    override fun e(tag: String, msg: String): Int {
        return println("EMERGENCY", tag, msg)
    }

    override fun e(tag: String, msg: String, tr: Throwable): Int {
        return println("EMERGENCY", tag, msg)
    }

    private fun println(level: String, tag: String, msg: String): Int {
        println("[$level][$tag]$msg")
        return 0
    }
}
