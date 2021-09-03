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
                val item = WindowsImage.read(it)
                item.process(it)
                //read(it)
            }
        }
    }

    /*fun read(reader: MemorySegmentBuffer): RawCodeFile {

        val header = readHeader(reader)

        check(header == "PE00") {
            "Header does not match PE format! Got: $header"
        }

        return processImage(parseCoff(reader), reader)
    }


    fun readHeader(reader: MemorySegmentBuffer): String {

        val magic = "${reader.readAsciiChar()}${reader.readAsciiChar()}"

        check(magic == "MZ" || magic == "ZM") {
            "Unknown magic: $magic"
        }

        reader.position = 0x3c
        reader.position = reader.readInt().toLong()

        return "${reader.readAsciiChar()}${reader.readAsciiChar()}${reader.readByte()}${reader.readByte()}"
    }

    fun processImage(windowsImage: WindowsImage, reader: MemorySegmentBuffer): RawCodeFile {

        //val buf = ByteBuffer.allocate(1024).order(ByteOrder.LITTLE_ENDIAN)
        val list = mutableMapOf<Int, RawCode.Unprocessed>()

        val imports = windowsImage.sections.filter { it.name.startsWith(".idata", true) }
        val exports = windowsImage.sections.filter { it.name.startsWith(".edata", true) }
        val import = mutableListOf<String>()
        //val bc = Files.newByteChannel(windowsImage.location)

        val importTables = mutableListOf<ImportTable>()

        val offset =
            if (windowsImage.dataDirs.size > 12) {
                (windowsImage.dataDirs[1].virtualAddress.toLong() - windowsImage.dataDirs[12].virtualAddress.toLong()).coerceAtLeast(0).toUInt()
            }
            else {
                0u
            }

        imports.forEach {
            reader.position = it.pointerToRawData.toLong() + offset.toLong()
            //var remaining = it.sizeOfRawData
            //buf.clear()
            //reAllocate(remaining, buf, bc)
            //remaining -= buf.remaining().toUInt()
            do {
                //remaining = reAllocateIfNeeded(20, remaining, buf, bc)
                importTables.add(
                    ImportTable(
                        reader.readUInt(),
                        reader.readUInt(),
                        reader.readUInt(),
                        reader.readUInt(),
                        reader.readUInt(),
                    )
                )
            } while (!importTables.last().isNull())
            importTables.removeLast()
        }

        val names = mutableListOf<String>()
        importTables.forEach {

            val vAddr = windowsImage.dataDirs[1].virtualAddress
            //println(windowsImage.sections.filter { (it.address..(it.address + it.size)).contains(vAddr) }.size)
            val raw = windowsImage.sections.first { (it.address..(it.address + it.size)).contains(vAddr) }

            if (it.nameRVA != 0u) {

                //buffer.position(3499104)
                reader.position = (it.nameRVA.toLong() - vAddr.toLong()) + raw.pointerToRawData.toLong() + offset.toLong()

                //println(it.nameRVA)
                //println(vAddr)
                //println(raw.pointerToRawData)

                names.add(buildString {

                    var char = reader.readAsciiChar()

                    while (char != '\u0000') {
                        append(char)
                        char = reader.readAsciiChar()
                    }
                })
            }
        }

        if (names.isNotEmpty()) {
            println(names)
        }

        val export = mutableListOf<String>()

        exports.forEach {

        }

        return RawCodeFile(OS.WINDOWS, windowsImage.machine.arch, 0, mutableListOf())
    }

    private fun reAllocateIfNeeded(dataSize: Int, remaining: UInt, buf: ByteBuffer, bc: SeekableByteChannel): UInt {
        return if (buf.remaining() < dataSize) {
            buf.compact()
            reAllocate(remaining, buf, bc)
            remaining - buf.remaining().toUInt()
        } else {
            remaining
        }
    }

    private fun reAllocate(amount: UInt, buf: ByteBuffer, bc: SeekableByteChannel) {
        var remaining = (bc.size() - bc.position()).toInt()
        if (remaining < 0) {
            remaining = buf.remaining()
        }
        var amountLeft = amount.toInt()
        if (amountLeft < 0) {
            amountLeft = buf.remaining()
        }
        buf.limit(buf.remaining().coerceAtMost(remaining).coerceAtMost(amountLeft))
        bc.read(buf)
        buf.flip()
    }

    fun parseCoff(reader: MemorySegmentBuffer): WindowsImage {

        val machineID = reader.readShort()

        // -31132, AMD64
        val machine = checkNotNull(CoffMachine.values().find { it.id == machineID }) {
            "Could not identify machine: ${machineID.toUShort().toString(16)}"
        }

        val amountOfSections = reader.readUShort()

        // Skip unneeded
        reader.skip(12)

        val optionalHeaderSize = reader.readUShort()
        val charFlags = CoffFlag.getFlags(reader.readShort())

        val format = when (val magic = reader.readShort()) {

            0x10B.toShort() -> {
                //PE32 version
                ExeFormat.PE32
            }
            0x20B.toShort() -> {
                //PE32+ version
                ExeFormat.PE32_PLUS
            }

            else -> error("Unknown COFF optional magic: ${magic.toUShort().toString(16)}")
        }

        // Skip unneeded
        reader.skip(2)

        val sizeOfCode = reader.readUInt()
        val sizeOfInitData = reader.readUInt()
        val sizeOfUnInitData = reader.readUInt()
        val entryPoint = reader.readUInt()
        val baseOfCode = reader.readUInt()

        val baseOfData =
            if (format == ExeFormat.PE32) {
                reader.readUInt()
            }
            else {
                0u
            }

        val imageBase =
            if (format == ExeFormat.PE32) {
                reader.readInt().toULong()
            }
            else {
                reader.readULong()
            }

        val sectionAlignment = reader.readUInt()
        val fileAlignment = reader.readUInt()

        // Skip unneeded
        reader.skip(16)

        val sizeOfImage = reader.readUInt()
        val sizeOfHeaders = reader.readUInt()
        val checksum = reader.readUInt() //todo should probably verify this

        val subSystemId = reader.readShort()

        val subSystem = checkNotNull(SubSystem.values().firstOrNull { it.id == subSystemId }) {
            "Could not identify subSystem: ${subSystemId.toUShort().toString(16)}"
        }

        val dllFlags = DLLFlag.getFlags(reader.readShort())

        val stackReserveSize =
            if (format == ExeFormat.PE32) {
                reader.readInt().toULong()
            }
            else {
                reader.readULong()
            }

        val stackCommitSize =
            if (format == ExeFormat.PE32) {
                reader.readInt().toULong()
            }
            else {
                reader.readULong()
            }

        val stackHeapReserveSize =
            if (format == ExeFormat.PE32) {
                reader.readInt().toULong()
            }
            else {
                reader.readULong()
            }

        val stackHeapCommitSize =
            if (format == ExeFormat.PE32) {
                reader.readInt().toULong()
            }
            else {
                reader.readULong()
            }

        // Skip unused value
        reader.skip(4)

        val dirs = Array(reader.readInt()) {
            DataDirectories(reader.readUInt(), reader.readUInt())
        }

        val sections = Array(amountOfSections.toInt()) {

            val name = buildString {
                for (i in 1 until 8) {

                    val char = reader.readAsciiChar()

                    if (char == '\u0000') {
                        reader.skip(8 - i)
                        break
                    }
                    else {
                        append(char)
                    }
                }
            }

            val vSize = reader.readUInt()
            val vAddr = reader.readUInt()
            val sRD = reader.readUInt()
            val pRD = reader.readUInt()
            val pRL = reader.readUInt()

            // Skip deprecated
            reader.skip(4)

            val nRL = reader.readUShort()

            // Skip deprecated
            reader.skip(2)

            val flags = SectionFlags.getFlags(reader.readInt())
            Section(name, vSize, vAddr, sRD, pRD, pRL, nRL, flags)
        }

        return WindowsImage(
            machine,
            charFlags,
            dllFlags,
            format,
            subSystem,
            dirs,
            sections,
            entryPoint,
            baseOfCode,
            baseOfData,
            imageBase
        )
    }*/
}