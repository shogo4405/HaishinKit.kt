package com.haishinkit.event

interface IEventDispatcher {
    fun addEventListener(type: String, listener: IEventListener, useCapture: Boolean)
    fun addEventListener(type: String, listener: IEventListener)
    fun dispatchEvent(event: Event)
    fun dispatchEventWith(type: String, bubbles: Boolean, data: Any?)
    fun dispatchEventWith(type: String, bubbles: Boolean)
    fun dispatchEventWith(type: String)
    fun removeEventListener(type: String, listener: IEventListener, useCapture: Boolean)
    fun removeEventListener(type: String, listener: IEventListener)
}
