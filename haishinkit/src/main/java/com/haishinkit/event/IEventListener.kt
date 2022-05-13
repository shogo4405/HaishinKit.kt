package com.haishinkit.event

/**
 * The IEventListener interface is the primary method for handling events.
 */
interface IEventListener {
    /**
     * Tell the receiver to handle an event.
     */
    fun handleEvent(event: Event)
}
