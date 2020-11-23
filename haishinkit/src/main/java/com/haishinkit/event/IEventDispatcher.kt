package com.haishinkit.event

interface IEventDispatcher {
    fun addEventListener(type: String, listener: IEventListener, useCapture: Boolean)
    fun addEventListener(type: String, listener: IEventListener) {
        addEventListener(type, listener, false)
    }
    fun dispatchEvent(event: Event)
    fun dispatchEventWith(type: String, bubbles: Boolean, data: Any?)
    fun dispatchEventWith(type: String, bubbles: Boolean) {
        dispatchEventWith(type, bubbles, null)
    }
    fun dispatchEventWith(type: String) {
        dispatchEventWith(type, false, null)
    }
    fun removeEventListener(type: String, listener: IEventListener, useCapture: Boolean)
    fun removeEventListener(type: String, listener: IEventListener) {
        removeEventListener(type, listener, false)
    }
}
