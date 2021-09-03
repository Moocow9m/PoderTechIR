package tech.poder.test

import tech.poder.ir.parsing.windows.WindowsImage
import tech.poder.ir.util.SegmentUtil
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.nio.file.FileVisitOption
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.isRegularFile
import kotlin.test.Test

internal class WindowsTest {

    @Test
    fun readDll() {

        val dlls = Paths.get("testFiles").toAbsolutePath()

        Files.walk(dlls, FileVisitOption.FOLLOW_LINKS).filter {
            val name = it.fileName.toString()
            it.isRegularFile() && (name.endsWith("dll") || name.endsWith("exe"))
        }.forEach { path ->
            SegmentUtil.mapFile(path, ByteOrder.LITTLE_ENDIAN, FileChannel.MapMode.READ_ONLY).use {
                WindowsImage.read(it)
            }
        }
    }
}