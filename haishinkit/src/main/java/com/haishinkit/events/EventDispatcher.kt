package com.haishinkit.events

import org.apache.commons.lang3.builder.ToStringBuilder

import java.util.ArrayList
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

open class EventDispatcher(private val target: IEventDispatcher?) : IEventDispatcher {
    private val listeners = ConcurrentHashMap<String, MutableList<IEventListener>>()

    override fun addEventListener(type: String, listener: IEventListener, useCapture: Boolean) {
        val key: String = type + "/" + useCapture.toString()
        listeners.putIfAbsent(key, Collections.synchronizedList(mutableListOf<IEventListener>()))
        listeners.get(key)?.add(listener)
    }

    override fun addEventListener(type: String, listener: IEventListener) {
        addEventListener(type, listener, false)
    }

    override fun dispatchEvent(event: Event) {
        if (event.type == null) {
            throw IllegalArgumentException()
        }

        val targets = ArrayList<IEventDispatcher>()
        targets.add(target ?: this)

        for (target in targets) {
            event.currentTarget = target
            val isTargetPhase = target === event.target

            if (isTargetPhase) {
                event.eventPhase = EventPhase.AT_TARGET
            }

            val isCapturingPhase = (event.eventPhase == EventPhase.CAPTURING).toString()
            val listeners = this.listeners[event.type + "/" + isCapturingPhase]
            if (listeners != null) {
                for (listener in listeners) {
                    listener.handleEvent(event)
                }
                if (event.propagationStopped) {
                    break
                }
            }

            if (isTargetPhase) {
                event.eventPhase = EventPhase.BUBBLING
            }
        }

        event.target = null
        event.currentTarget = null
        event.eventPhase = EventPhase.NONE
        event.propagationStopped = false
    }

    override fun dispatchEventWith(type: String, bubbles: Boolean, data: Any?) {
        dispatchEvent(Event(type, bubbles, data))
    }

    override fun dispatchEventWith(type: String, bubbles: Boolean) {
        dispatchEventWith(type, bubbles, null)
    }

    override fun dispatchEventWith(type: String) {
        dispatchEventWith(type, false)
    }

    override fun removeEventListener(type: String, listener: IEventListener, useCapture: Boolean) {
        val key = type + "/" + useCapture.toString()
        if (!listeners.containsKey(key)) {
            return
        }
        val list = listeners.get(key)!!
        var i = list.size - 1
        while (0 <= i) {
            if (list.get(i) === listener) {
                list.removeAt(i)
                return
            }
            --i
        }
    }

    override fun removeEventListener(type: String, listener: IEventListener) {
        removeEventListener(type, listener, false)
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }
}
