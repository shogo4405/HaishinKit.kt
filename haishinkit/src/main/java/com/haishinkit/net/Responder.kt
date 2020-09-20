package com.haishinkit.net

interface Responder {
    fun onResult(arguments: List<Any?>)
    fun onStatus(arguments: List<Any?>)
}
