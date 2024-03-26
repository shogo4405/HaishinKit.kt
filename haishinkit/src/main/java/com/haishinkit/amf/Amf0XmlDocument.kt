package com.haishinkit.amf

/**
 * An object that represents the AMF0 XML Document type.
 * 2.17 XML Document Type.
 */
data class Amf0XmlDocument(val document: String) {
    override fun toString(): String {
        return document
    }
}
