package tech.poder.test

internal class WindowsTest {

    /*@Test
    fun readDll() {

        val dlls = Paths.get("testFiles").toAbsolutePath()

        Files.walk(dlls, FileVisitOption.FOLLOW_LINKS).filter {
            val name = it.fileName.toString()
            it.isRegularFile() && (name.endsWith("dll") || name.endsWith("exe"))
        }.forEach { path ->
            SegmentUtil.mapFile(path, ByteOrder.LITTLE_ENDIAN, FileChannel.MapMode.READ_ONLY).use {
                WindowsImage.read(it).processToGeneric(it)
            }
        }
    }*/
}