package com.haishinkit.amf.data

final class AsUndefined private constructor() {

    override fun toString(): String {
        return "undefined"
    }

    companion object {
        val instance = AsUndefined()
    }
}
