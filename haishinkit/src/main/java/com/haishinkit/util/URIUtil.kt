package com.haishinkit.util

import java.net.URI

internal object URIUtil {
    fun withoutUserInfo(uri: URI): String {
        val userInfo = uri.userInfo ?: return uri.toString()
        return uri.toString().replace("${uri.rawUserInfo}@", "")
    }
}
