package tech.poder.test

import jdk.incubator.foreign.MemoryAccess
import jdk.incubator.foreign.MemorySegment
import tech.poder.ir.parsing.generic.OS
import tech.poder.ir.parsing.generic.RawCode
import tech.poder.ir.parsing.generic.RawCodeFile
import tech.poder.ir.parsing.windows.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.nio.channels.SeekableByteChannel
import java.nio.file.FileVisitOption
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.fileSize
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
            thing(it)
        }

        processableFiles.forEach { it.process() }
    }

    // TODO: Use a datainputstream
    fun thing(path: Path) {

        val reader = MemorySegmentReader(
            MemorySegment.mapFile(path, 0, path.fileSize(), FileChannel.MapMode.READ_ONLY),
            ByteOrder.LITTLE_ENDIAN
        )

        println(read(path, reader))
    }

    fun read(path: Path, reader: MemorySegmentReader): RawCodeFile {

        val header = readHeader(reader)

        check(header == "PE00") {
            "Header does not match PE format! Got: $header"
        }

        println(header)

        return processImage(parseCoff(reader, path), reader)
    }


    fun readHeader(reader: MemorySegmentReader): String {

        val magic = "${reader.readAsciiChar()}${reader.readAsciiChar()}"

        check(magic == "MZ" || magic == "ZM") {
            "Unknown magic: $magic"
        }

        reader.position = 0x3c
        reader.position = reader.readInt().toLong()

        return "${reader.readAsciiChar()}${reader.readAsciiChar()}${reader.readByte()}${reader.readByte()}"
    }

    fun processImage(windowsImage: WindowsImage, reader: MemorySegmentReader): RawCodeFile {

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

        println(imports)
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

        println(names)

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

    fun parseCoff(reader: MemorySegmentReader, path: Path): WindowsImage {

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
            imageBase,
            path
        )
    }


    data class MemorySegmentReader(
        val memorySegment: MemorySegment,
        val byteOrder: ByteOrder = ByteOrder.nativeOrder(),
    ) {

        var position = 0L


        fun readByte(): Byte {
            return MemoryAccess.getByteAtOffset(memorySegment, position).apply {
                position += Byte.SIZE_BYTES
            }
        }

        fun readShort(): Short {
            return MemoryAccess.getShortAtOffset(memorySegment, position, byteOrder).apply {
                position += Short.SIZE_BYTES
            }
        }

        fun readInt(): Int {
            return MemoryAccess.getIntAtOffset(memorySegment, position, byteOrder).apply {
                position += Int.SIZE_BYTES
            }
        }

        fun readLong(): Long {
            return MemoryAccess.getLongAtOffset(memorySegment, position, byteOrder).apply {
                position += Long.SIZE_BYTES
            }
        }


        fun readUByte(): UByte {
            return readByte().toUByte()
        }

        fun readUShort(): UShort {
            return readShort().toUShort()
        }

        fun readUInt(): UInt {
            return readInt().toUInt()
        }

        fun readULong(): ULong {
            return readLong().toULong()
        }


        fun readChar(): Char {
            return MemoryAccess.getCharAtOffset(memorySegment, position, byteOrder).apply {
                position += Char.SIZE_BYTES
            }
        }

        fun readAsciiChar(): Char {
            return readByte().toInt().toChar()
        }


        fun skip(bytes: Int) {
            position += bytes
        }

    }
/*
    data class WindowsImage(

    ) {


        companion object {

            private fun readCOFF() {

            }
        }
    }*/

}