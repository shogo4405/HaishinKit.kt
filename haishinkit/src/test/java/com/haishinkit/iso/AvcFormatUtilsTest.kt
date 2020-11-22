package com.haishinkit.iso

import junit.framework.TestCase
import org.junit.Assert
import java.nio.ByteBuffer

class AvcFormatUtilsTest : TestCase() {
    fun testPutNALFileFormat() {
        System.out.println("Hello World!!")
        val input = ByteBuffer.allocate(512)
        val output = ByteBuffer.allocate(1024)
        AvcFormatUtils.putNALFileFormat(input, output)
        Assert.assertEquals(input, output)
    }
}
