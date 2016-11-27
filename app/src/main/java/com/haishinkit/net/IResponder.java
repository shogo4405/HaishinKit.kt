package com.haishinkit.net;

import java.util.List;

public interface IResponder {
    public void onResult(List<Object> arguments);
    public void onStatus(List<Object> arguments);
}
