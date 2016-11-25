package com.haishinkit.util;

public final class Log {
    private static ILogAdapter adapter = new SystemLogAdapter();

    static {
        String runtime = System.getProperty("java.runtime.name");
        if (0 <= runtime.indexOf("Android")) {
            adapter = new AndroidLogAdapter();
        }
    }

    public static int v(String tag, String msg) {
        return adapter.v(tag, msg);
    }

    public static int v(String tag, String msg, Throwable tr) {
        return adapter.v(tag, msg, tr);
    }

    public static int d(String tag, String msg) {
        return adapter.v(tag, msg);
    }

    public static int d(String tag, String msg, Throwable tr) {
        return adapter.d(tag, msg, tr);
    }

    public static int i(String tag, String msg) {
        return adapter.i(tag, msg);
    }

    public static int i(String tag, String msg, Throwable tr) {
        return adapter.i(tag, msg, tr);
    }

    public static int w(String tag, String msg) {
        return adapter.w(tag, msg);
    }

    public static int w(String tag, String msg, Throwable tr) {
        return adapter.w(tag, msg, tr);
    }

    public static int w(String tag, Throwable tr) {
        return adapter.w(tag, tr);
    }

    public static int e(String tag, String msg) {
        return adapter.e(tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        return adapter.e(tag, msg, tr);
    }
}