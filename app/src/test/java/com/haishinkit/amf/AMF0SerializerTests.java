package com.haishinkit.amf;

import com.haishinkit.util.ByteBufferUtils;

import java.util.Map;
import java.util.HashMap;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import org.junit.Test;

public final class AMF0SerializerTests {
    @Test
    public void main() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        AMF0Serializer serializer = new AMF0Serializer(buffer);
        List<Object> list = new ArrayList<Object>();
        list.add(1);
        list.add("Hello");
        list.add("World");
        list.add(new Date());
        Map<String, Object> commandObject = new HashMap<String, Object>();
        commandObject.put("app", "");
        commandObject.put("flashVer", "MAC10");
        commandObject.put("swfUrl", "http://localhost/hoge.swf");
        commandObject.put("tcUrl", "rtmp://localhost/appName/instanceName/");
        commandObject.put("fpad", false);
        commandObject.put("capabilities", 10);
        commandObject.put("audioCodecs", 10);
        commandObject.put("videoCodecs", 10);
        commandObject.put("videoFunction", 1);
        commandObject.put("pageUrl", "http://localhost/");
        commandObject.put("objectEncoding", 3);
        commandObject.put("list", list);
        commandObject.put("timestamp", new Date());
        serializer.putMap(commandObject);
        buffer.position(0);
        System.out.println(ByteBufferUtils.toHexString(buffer));
        AMF0Deserializer deserializer = new AMF0Deserializer(buffer);
        System.out.print(deserializer.getMap());
    }
}
