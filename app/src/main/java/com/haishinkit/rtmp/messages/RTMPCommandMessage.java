package com.haishinkit.rtmp.messages;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.nio.ByteBuffer;

import com.haishinkit.amf.AMF0Deserializer;
import com.haishinkit.amf.AMF0Serializer;
import com.haishinkit.events.Event;
import com.haishinkit.net.IResponder;
import com.haishinkit.rtmp.RTMPConnection;
import com.haishinkit.rtmp.RTMPObjectEncoding;
import com.haishinkit.rtmp.RTMPSocket;

public final class RTMPCommandMessage extends RTMPMessage {
    private static final int CAPACITY = 1024;

    private final RTMPObjectEncoding objectEncoding;
    private String commandName = null;
    private int transactionID = 0;
    private Map<String, Object> commandObject = null;
    private List<Object> arguments = null;

    public RTMPCommandMessage(final RTMPObjectEncoding objectEncoding) {
        super(objectEncoding.getCommandType());
        this.objectEncoding = objectEncoding;
    }

    public RTMPObjectEncoding getObjectEncoding() {
        return objectEncoding;
    }

    public String getCommandName() {
        return commandName;
    }

    public RTMPCommandMessage setCommandName(final String commandName) {
        this.commandName = commandName;
        return this;
    }

    public int getTransactionID() {
        return transactionID;
    }

    public RTMPCommandMessage setTransactionID(final int transactionID) {
        this.transactionID = transactionID;
        return this;
    }

    public Map<String, Object> getCommandObject() {
        return commandObject;
    }

    public RTMPCommandMessage setCommandObject(final Map<String, Object> commandObject) {
        this.commandObject = commandObject;
        return this;
    }

    public List<Object> getArguments() {
        return arguments;
    }

    public RTMPCommandMessage setArguments(final List<Object> arguments) {
        this.arguments = arguments;
        return this;
    }

    @Override
    public ByteBuffer encode(final RTMPSocket socket) {
        ByteBuffer buffer = ByteBuffer.allocate(CAPACITY);
        if (getType().equals(Type.AMF3_COMMAND)) {
            buffer.put((byte) 0x00);
        }
        AMF0Serializer serializer = new AMF0Serializer(buffer);
        serializer.putString(getCommandName());
        serializer.putDouble((double) getTransactionID());
        serializer.putMap(getCommandObject());
        if (getArguments() != null) {
            for (Object object : getArguments()) {
                serializer.putObject(object);
            }
        }
        return buffer;
    }

    @Override
    public RTMPMessage decode(final ByteBuffer buffer) {
        if (buffer == null) {
            throw new IllegalArgumentException();
        }
        int position = buffer.position();
        AMF0Deserializer deserializer = new AMF0Deserializer(buffer);
        setCommandName(deserializer.getString());
        setTransactionID((int) deserializer.getDouble());
        setCommandObject(deserializer.getMap());
        List<Object> arguments = new ArrayList<Object>();
        while (buffer.position() - position != getLength()) {
            arguments.add(deserializer.getObject());
        }
        setArguments(arguments);
        return this;
    }

    public RTMPMessage execute(final RTMPConnection connection) {
        String commandName = getCommandName();
        Map<Integer, IResponder> responders = connection.getResponders();

        if (responders.containsKey(getTransactionID())) {
            IResponder responder = responders.get(getTransactionID());
            switch (commandName) {
                case "_result":
                    responder.onResult(getArguments());
                    return this;
                case "_error":
                    responder.onStatus(getArguments());
                    return this;
            }
            responders.remove(responder);
            return this;
        }

        switch (commandName) {
            case "close":
                connection.close();
                break;
            default:
                connection.dispatchEventWith(Event.RTMP_STATUS, false, arguments.isEmpty() ? null : arguments.get(0));
                break;
        }

        return this;
    }
}
