package com.haishinkit.media.codec

enum class MediaCodec(val rawValue: String) {
    VIDEO_VP8("video/x-vnd.on2.vp8"),
    VIDEO_VP9("video/x-vnd.on2.vp9"),
    VIDEO_AVC("video/avc"),
    VIDEO_HEVC("video/hevc"),
    VIDEO_MP4V("video/mp4v-es"),
    VIDEO_3GPP("video/3gpp"),
    AUDIO_3GPP("audio/3gpp"),
    AUDIO_AMR("audio/amr-wb"),
    AUDIO_MPEG("audio/mpeg"),
    AUDIO_MP4A("audio/mp4a-latm"),
    AUDIO_VORBIS("audio/vorbis"),
    AUDIO_G711A("audio/g711-alaw"),
    AUDIO_G711U("audio/g711-mlaw")
}
