package com.haishinkit.rtmp

import com.haishinkit.rtmp.message.RtmpMessage

enum class RtmpObjectEncoding(
    val rawValue: Short,
    val dataType: Byte,
    val sharedObjectType: Byte,
    val commandType: Byte
) {
    AMF0(
        0,
        RtmpMessage.TYPE_AMF0_DATA,
        RtmpMessage.TYPE_AMF0_SHARED,
        RtmpMessage.TYPE_AMF0_COMMAND
    ),
    AMF3(
        3,
        RtmpMessage.TYPE_AMF3_DATA,
        RtmpMessage.TYPE_AMF3_SHARED,
        RtmpMessage.TYPE_AMF3_COMMAND
    );
}
