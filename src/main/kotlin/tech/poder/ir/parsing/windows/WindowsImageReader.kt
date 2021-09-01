package tech.poder.ir.parsing.windows

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.SeekableByteChannel
import java.nio.file.Files
import java.nio.file.Path

class WindowsImageReader(
    val machine: CoffMachine,
    val coffFlags: List<CoffFlag>,
    val dllFlags: List<DLLFlag>,
    val format: ExeFormat,
    val subSystem: SubSystem,
    val dataDirs: Array<DataDirectories>,
    val sections: Array<Section>
) {
    companion object {
        fun read(path: Path): WindowsImageReader {
            val file = Files.newByteChannel(path)
            val buffer = ByteBuffer.allocate(1024)
            buffer.order(ByteOrder.LITTLE_ENDIAN)
            buffer.limit(2)
            file.read(buffer)
            buffer.flip()
            val magic = "${buffer.get().toInt().toChar()}${buffer.get().toInt().toChar()}"
            return when (magic) {
                "MZ", "ZM" -> {
                    buffer.clear()
                    file.position(0x3c)
                    buffer.limit(4)
                    file.read(buffer)
                    buffer.flip()
                    file.position(buffer.int.toLong())
                    buffer.clear()
                    buffer.limit(4)
                    file.read(buffer)
                    buffer.flip()
                    val header =
                        "${buffer.get().toInt().toChar()}${buffer.get().toInt().toChar()}${buffer.get()}${buffer.get()}"
                    check(header == "PE00") {
                        "Header does not match PE format! Got: $header"
                    }
                    parseCoff(buffer, file)
                }
                else -> {
                    error("Unknown magic: $path")
                }
            }
        }

        private fun parseCoff(buf: ByteBuffer, bc: SeekableByteChannel): WindowsImageReader {
            buf.clear()
            buf.limit(20)
            bc.read(buf)
            buf.flip()
            val mId = buf.short
            val machine = CoffMachine.values().firstOrNull { it.id == mId }
            check(machine != null) {
                "Could not identify machine: ${mId.toUShort().toString(16)}"
            }
            val numOfSections = buf.short.toUShort()
            val creationDateLow32 = buf.int

            buf.position(buf.position() + 8) //skip deprecated
            //val debugOffset = buf.int //deprecated, should be 0
            //val symbolCount = buf.int //deprecated, should be 0
            val optionalHeaderSize = buf.short
            val charFlags = CoffFlag.getFlags(buf.short)
            check(optionalHeaderSize > 0) {
                "Optional header was empty, but is required on Window Images!"
            }
            buf.clear()
            buf.limit(optionalHeaderSize.toInt())
            bc.read(buf)
            buf.flip()
            val format = when (val magic = buf.short) {
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
            val linkerVersion = "${buf.get().toUByte()}.${buf.get().toUByte()}"
            val sizeOfCode = buf.int.toUInt()
            val sizeOfInitData = buf.int.toUInt()
            val sizeOfUnInitData = buf.int.toUInt()
            val entryPoint = buf.int.toUInt()
            val baseOfCode = buf.int.toUInt()
            val baseOfData = if (format == ExeFormat.PE32) {
                buf.int.toUInt()
            } else {
                0u
            }
            val imageBase = if (format == ExeFormat.PE32) {
                buf.int.toULong()
            } else {
                buf.long.toULong()
            }
            val sectionAlignment = buf.int.toUInt()
            val fileAlignment = buf.int.toUInt()
            val osVersion = "${buf.short.toUShort()}.${buf.short.toUShort()}"
            val imageVersion = "${buf.short.toUShort()}.${buf.short.toUShort()}"
            val subSystemVersion = "${buf.short.toUShort()}.${buf.short.toUShort()}"
            buf.position(buf.position() + 4) //skip unused value
            //val winVersion = buf.int //should be 0
            val sizeOfImage = buf.int.toUInt()
            val sizeOfHeaders = buf.int.toUInt()
            val checksum = buf.int.toUInt()
            val subSystemId = buf.short
            val subSystem = SubSystem.values().firstOrNull { it.id == subSystemId }
            check(subSystem != null) {
                "Could not identify subSystem: ${subSystemId.toUShort().toString(16)}"
            }
            val dllFlags = DLLFlag.getFlags(buf.short)
            val stackReserveSize = if (format == ExeFormat.PE32) {
                buf.int.toULong()
            } else {
                buf.long.toULong()
            }
            val stackCommitSize = if (format == ExeFormat.PE32) {
                buf.int.toULong()
            } else {
                buf.long.toULong()
            }
            val stackHeapReserveSize = if (format == ExeFormat.PE32) {
                buf.int.toULong()
            } else {
                buf.long.toULong()
            }
            val stackHeapCommitSize = if (format == ExeFormat.PE32) {
                buf.int.toULong()
            } else {
                buf.long.toULong()
            }
            buf.position(buf.position() + 4) //skip unused value
            //val loaderFlags = buf.short //should be 0
            val dirs = Array(buf.int) {
                DataDirectories(buf.int.toUInt(), buf.int.toUInt())
            }
            val sections = Array(numOfSections.toInt()) {
                buf.clear()
                buf.limit(40)
                bc.read(buf)
                buf.flip()
                val name = StringBuilder()
                var foundNull = false
                repeat(8) {
                    val char = buf.get()
                    if (!foundNull) {
                        if (char == 0.toByte()) {
                            foundNull = true
                        } else {
                            name.append(char.toInt().toChar())
                        }
                    }
                }
                val vSize = buf.int.toUInt()
                val vAddr = buf.int.toUInt()
                val sRD = buf.int.toUInt()
                val pRD = buf.int.toUInt()
                val pRL = buf.int.toUInt()
                buf.position(buf.position() + 4)//skip deprecated
                val nRL = buf.short.toUShort()
                buf.position(buf.position() + 2)//skip deprecated
                val flags = SectionFlags.getFlags(buf.int)
                Section(name.toString(), vSize, vAddr, sRD, pRD, pRL, nRL, flags)
            }
            return WindowsImageReader(machine, charFlags, dllFlags, format, subSystem, dirs, sections)
        }
    }
}