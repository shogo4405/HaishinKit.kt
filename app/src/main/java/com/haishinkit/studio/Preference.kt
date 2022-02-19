package com.haishinkit.studio

data class Preference(var rtmpURL: String, var streamName: String) {
    companion object {
        var shared = Preference(
            "rtmp://test:test@192.168.1.6/live",
            "live"
        )

        var useSurfaceView: Boolean = true
    }
}
