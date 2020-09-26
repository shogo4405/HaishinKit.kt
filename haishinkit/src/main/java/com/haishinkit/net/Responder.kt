package com.haishinkit.net

/**
 * A interface that response for a RTMPConnection.call().
 */
interface Responder {
    fun onResult(arguments: List<Any?>)
    fun onStatus(arguments: List<Any?>)
}
