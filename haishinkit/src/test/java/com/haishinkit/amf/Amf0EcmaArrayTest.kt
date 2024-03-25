package com.haishinkit.amf

import junit.framework.TestCase

class Amf0EcmaArrayTest : TestCase() {
    fun testSetAndGet() {
        val ecmaArray = Amf0EcmaArray()
        ecmaArray[0.toString()] = "Hello World!!"
        ecmaArray[1.toString()] = "World!!"
        assertEquals("Hello", ecmaArray[0.toString()])
        assertEquals("World!!", ecmaArray[1.toString()])
    }
}
