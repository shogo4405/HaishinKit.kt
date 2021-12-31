package com.haishinkit.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

internal object ZipUtil {
    private const val ZIP_BUFFER_SIZE = 1024 * 1024

    @RequiresApi(Build.VERSION_CODES.O)
    fun zipFile(
        targetPath: Path,
        destFilePath: Path = Paths.get("$targetPath.zip"),
        zipFileCoding: Charset = Charset.forName("UTF-8")
    ) {
        FileOutputStream(destFilePath.toString()).use { fileOutputStream ->
            ZipOutputStream(
                BufferedOutputStream(fileOutputStream),
                zipFileCoding
            ).use { zipOutStream ->
                for (filePath in Files.walk(targetPath).map { it.normalize() }) {
                    val file = filePath.toFile()
                    val entryPath =
                        "${targetPath.relativize(filePath)}${if (file.isDirectory) "/" else ""}"
                    val zipEntry =
                        ZipEntry(String(entryPath.toByteArray(zipFileCoding), zipFileCoding))
                    zipOutStream.putNextEntry(zipEntry)
                    if (file.isFile) {
                        FileInputStream(file).use { inputStream ->
                            val bytes = ByteArray(ZIP_BUFFER_SIZE)
                            var length: Int
                            while (inputStream.read(bytes).also { length = it } >= 0) {
                                zipOutStream.write(bytes, 0, length)
                            }
                        }
                    }
                    zipOutStream.closeEntry()
                }
            }
        }
    }
}
