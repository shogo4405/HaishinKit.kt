package com.haishinkit.event

open class Event(type: String, bubbles: Boolean, data: Any?) {
    var type: String? = null
        internal set
    var target: IEventDispatcher? = null
        internal set
    var currentTarget: IEventDispatcher? = null
        internal set
    internal var eventPhase = EVENT_PHASE_NONE
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

    companion object {
        const val RTMP_STATUS = "rtmpStatus"
        const val IO_ERROR = "ioError"

        const val EVENT_PHASE_NONE = 0x00
        const val EVENT_PHASE_CAPTURING = 0x01
        const val EVENT_PHASE_AT_TARGET = 0x02
        const val EVENT_PHASE_BUBBLING = 0x03
    }
}
