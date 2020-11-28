package com.haishinkit.util

import java.io.File

internal object FileUtil {
    fun delete(file: File) {
        if (!file.exists()) return
        if (file.isDirectory) {
            file.listFiles()?.forEach { child ->
                delete(child)
            }
        }
        file.delete()
    }
}
