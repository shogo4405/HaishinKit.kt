package com.haishinkit.util;

public final class SystemLogAdapter implements ILogAdapter {
    public int v(String tag, String msg) {
        return println("VERBOSE", tag, msg);
    }

    public int v(String tag, String msg, Throwable tr) {
        return println("VERBOSE", tag, tr.toString());
    }

    public int d(String tag, String msg) {
        return println("DEBUG", tag, msg);
    }

    public int d(String tag, String msg, Throwable tr) {
        return println("DEBUG", tag, tr.toString());
    }

    public int i(String tag, String msg) {
        return println("INFO", tag, msg);
    }

    public int i(String tag, String msg, Throwable tr) {
        return println("INFO", tag, tr.toString());
    }

    public int w(String tag, String msg) {
        return println("WARN", tag, msg);
    }

    public int w(String tag, String msg, Throwable tr) {
        return println("WARN", tag, msg);
    }

    public int w(String tag, Throwable tr) {
        return println("WARN", tag, tr.toString());
    }

    public int e(String tag, String msg) {
        return println("EMERGENCY", tag, msg);
    }

    public int e(String tag, String msg, Throwable tr) {
        return println("EMERGENCY", tag, msg);
    }

    private int println(String level, String tag, String msg) {
        System.out.println("[" + level + "][" + tag + "]" + msg);
        return 0;
    }
}
