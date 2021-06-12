package com.haishinkit.util

import junit.framework.TestCase
import java.net.URI

class URIUtilTest : TestCase() {
    fun testGetCompositeTime() {
        val uri = URI.create("rtmp://user:password@localhost:443/live")
        assertEquals(URIUtil.withoutUserInfo(uri), "rtmp://localhost:443/live")
    }
}
