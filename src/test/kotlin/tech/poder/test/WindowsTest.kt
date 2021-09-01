package tech.poder.test

import tech.poder.ir.parsing.windows.WindowsImage
import java.nio.file.FileVisitOption
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.isRegularFile
import kotlin.test.Test

class WindowsTest {

    @Test
    fun readDll() {
        val dlls = Paths.get("testFiles").toAbsolutePath()
        Files.walk(dlls, FileVisitOption.FOLLOW_LINKS).filter {
            val name = it.fileName.toString()
            it.isRegularFile() && (name.endsWith("dll") || name.endsWith("exe"))
        }.forEach {
            val result = WindowsImage.read(it)
        }
    }
}