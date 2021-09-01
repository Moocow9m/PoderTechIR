package tech.poder.test

import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.Test

class WindowsTest {

    @Test
    fun readDll() {
        val dlls = Paths.get("windowsTest").toAbsolutePath()
        Files.list(dlls).filter {
            val name = it.fileName.toString()
            name.endsWith("dll") || name.endsWith("exe")
        }.forEach {

        }
    }
}