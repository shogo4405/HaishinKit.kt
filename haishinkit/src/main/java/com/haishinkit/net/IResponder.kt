package com.haishinkit.net

interface IResponder {
    fun onResult(arguments: List<Any?>)
    fun onStatus(arguments: List<Any?>)
}
