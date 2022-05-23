package com.haishinkit.lang

internal fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }
