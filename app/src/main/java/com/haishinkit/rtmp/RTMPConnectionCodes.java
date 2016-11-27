package com.haishinkit.rtmp;

import com.haishinkit.lang.IRawValue;

public enum RTMPConnectionCodes implements IRawValue<String> {
    CALL_BAD_VERSION("NetConnection.Call.BadVersion", "error"),
    CALL_FAILED("NetConnection.Call.Failed", "error"),
    CALL_PROHIBITED("NetConnection.Call.Prohibited", "error"),
    CONNECT_APP_SHUTDOWN("NetConnection.Connect.AppShutdown", "status"),
    CONNECT_CLOSED("NetConnection.Connect.Closed", "status"),
    CONNECT_FAILED("NetConnection.Connect.Failed", "error"),
    CONNECT_IDLE_TIME_OUT("NetConnection.Connect.IdleTimeOut", "status"),
    CONNECT_INVALID_APP("NetConnection.Connect.InvalidApp", "error"),
    CONNECT_NETWORK_CHANGE("NetConnection.Connect.NetworkChange", "status"),
    CONNECT_REJECTED("NetConnection.Connect.Rejected", "status"),
    CONNECT_SUCCESS("NetConnection.Connect.Success", "status");

    private final String rawValue;
    private final String level;

    RTMPConnectionCodes(final String rawValue, final String level) {
        this.rawValue = rawValue;
        this.level = level;
    }

    public String getLevel() {
        return level;
    }

    public String rawValue() {
        return rawValue;
    }
}
