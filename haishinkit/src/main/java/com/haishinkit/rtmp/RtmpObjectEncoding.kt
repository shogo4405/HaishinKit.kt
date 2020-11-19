package com.haishinkit.rtmp

import com.haishinkit.rtmp.messages.RtmpMessage

enum class RtmpObjectEncoding(
    val rawValue: Short,
    val dataType: RtmpMessage.Type,
    val sharedObjectType: RtmpMessage.Type,
    val commandType: RtmpMessage.Type
) {
    AMF0(0, RtmpMessage.Type.AMF0_DATA, RtmpMessage.Type.AMF0_SHARED, RtmpMessage.Type.AMF0_COMMAND),
    AMF3(3, RtmpMessage.Type.AMF3_DATA, RtmpMessage.Type.AMF3_SHARED, RtmpMessage.Type.AMF3_COMMAND);
}
