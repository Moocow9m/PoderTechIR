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
        val processableFiles = mutableListOf<WindowsImage>()
        Files.walk(dlls, FileVisitOption.FOLLOW_LINKS).filter {
            val name = it.fileName.toString()
            it.isRegularFile() && (name.endsWith("dll") || name.endsWith("exe"))
        }.forEach {
            processableFiles.add(WindowsImage.read(it))
        }

        processableFiles.filter { it.location.fileName.toString().equals("WdfCoInstaller01011.dll", true) }
            .forEach { it.process() }
    }
}