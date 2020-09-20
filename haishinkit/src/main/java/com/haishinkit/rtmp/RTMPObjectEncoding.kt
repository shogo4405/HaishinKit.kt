package com.haishinkit.rtmp

import com.haishinkit.rtmp.messages.RTMPMessage

enum class RTMPObjectEncoding(
    val rawValue: Short,
    val dataType: RTMPMessage.Type,
    val sharedObjectType: RTMPMessage.Type,
    val commandType: RTMPMessage.Type
) {
    AMF0(0, RTMPMessage.Type.AMF0_DATA, RTMPMessage.Type.AMF0_SHARED, RTMPMessage.Type.AMF0_COMMAND),
    AMF3(3, RTMPMessage.Type.AMF3_DATA, RTMPMessage.Type.AMF3_SHARED, RTMPMessage.Type.AMF3_COMMAND);
}
