package com.haishinkit.events

import org.apache.commons.lang3.builder.ToStringBuilder

open class Event(type: String, bubbles: Boolean, data: Any?) {
    var type: String? = null
        internal set
    var target: IEventDispatcher? = null
        internal set
    var currentTarget: IEventDispatcher? = null
        internal set
    internal var eventPhase = EventPhase.NONE
    var data: Any? = null
        internal set
    var isBubbles = false
        internal set
    internal var propagationStopped = false

    init {
        this.type = type
        this.data = data
        this.isBubbles = bubbles
    }

    fun stopPropagation() {
        propagationStopped = true
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    companion object {
        val RTMP_STATUS = "rtmpStatus"
        var IO_ERROR = "ioError"
    }
}
