package com.haishinkit.lang

/**
 * An interface that methods for utilize.
 */
interface Utilize {
    var utilizable: Boolean

    /**
     * Tells the receiver to setUp.
     */
    fun setUp() {
        utilizable = true
    }

    /**
     * Tells the receiver to tearDown.
     */
    fun tearDown() {
        utilizable = false
    }
}
