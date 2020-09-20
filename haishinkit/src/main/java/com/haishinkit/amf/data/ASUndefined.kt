package com.haishinkit.amf.data

final class ASUndefined private constructor() {

    override fun toString(): String {
        return "undefined"
    }

    companion object {
        val instance = ASUndefined()
    }
}
