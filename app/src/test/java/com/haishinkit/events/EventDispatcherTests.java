package com.haishinkit.events;

import org.junit.Test;
import com.haishinkit.events.IEventListener;
import com.haishinkit.events.EventDispatcher;
import static org.junit.Assert.*;

public final class EventDispatcherTests {

    public final class FailListener implements IEventListener {
        public void handleEvent(final Event event) {
            fail();
        }
    }

    @Test
    public void main() {
        FailListener listener = new FailListener();
        EventDispatcher dispatcher = new EventDispatcher(null);
        dispatcher.addEventListener("test", listener);
        dispatcher.removeEventListener("test", listener);
        dispatcher.dispatchEventWith("test");
    }
}
