package tech.poder.ir.parsing.windows

import tech.poder.ir.parsing.generic.OS
import tech.poder.ir.parsing.generic.RawCode
import tech.poder.ir.parsing.generic.RawCodeFile
import tech.poder.ir.parsing.windows.flags.CoffFlag
import tech.poder.ir.parsing.windows.flags.CoffMachine
import tech.poder.ir.parsing.windows.flags.DLLFlag
import tech.poder.ir.parsing.windows.flags.SectionFlags
import tech.poder.ir.parsing.windows.imports.ImportLookupTable
import tech.poder.ir.parsing.windows.imports.ImportTable
import tech.poder.ir.util.MemorySegmentBuffer
import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel

class WindowsImage(
    val machine: CoffMachine,
    val coffFlags: List<CoffFlag>,
    val dllFlags: List<DLLFlag>,
    val format: ExeFormat,
    val subSystem: SubSystem,
    val dataDirs: Array<DataDirectories>,
    val sections: Array<Section>,
    val entryLocation: UInt,
    val baseCodeAddress: UInt,
    val baseDataAddress: UInt,
    val preferredImageBase: ULong
) {
    companion object {
        fun read(reader: MemorySegmentBuffer): WindowsImage {
            reader.position = 0
            return when (val magic = "${reader.readAsciiChar()}${reader.readAsciiChar()}") {
                "MZ", "ZM" -> {
                    reader.position = 0x3c

                    reader.position = reader.readUInt().toLong()
                    val header =
                        "${reader.readAsciiChar()}${reader.readAsciiChar()}${reader.readByte()}${reader.readByte()}"
                    check(header == "PE00") {
                        "Header does not match PE format! Got: $header"
                    }
                    parseCoff(reader)
                }
                else -> {
                    error("Unknown magic: $magic")
                }
            }
        }

        private fun parseCoff(reader: MemorySegmentBuffer): WindowsImage {
            val mId = reader.readShort()
            val machine = CoffMachine.values().firstOrNull { it.id == mId }
            checkNotNull(machine) {
                "Could not identify machine: ${mId.toUShort().toString(16)}"
            }
            val numOfSections = reader.readUShort()
            reader.position += 4//skip unneeded
            //val creationDateLow32 = reader.readInt()

            reader.position += 8 //skip deprecated
            //val debugOffset = reader.readInt() //deprecated, should be 0
            //val symbolCount = reader.readInt() //deprecated, should be 0
            val optionalHeaderSize = reader.readUShort()
            val charFlags = CoffFlag.getFlags(reader.readShort())
            check(optionalHeaderSize > 0u) {
                "Optional header was empty, but is required on Window Images!"
            }
            val format = when (val magic = reader.readShort()) {
                0x10B.toShort() -> {
                    //PE32 version
                    ExeFormat.PE32
                }
                0x20B.toShort() -> {
                    //PE32+ version
                    ExeFormat.PE32_PLUS
                }
                0x107.toShort() -> {
                    //ROM
                    error("ROM is not supported! (yet?)")
                }
                else -> error("Unknown COFF optional magic: ${magic.toUShort().toString(16)}")
            }
            reader.position += 14 //skip unneeded
            //val linkerVersion = "${reader.readUByte()}.${reader.readUByte()}"
            //val sizeOfCode = reader.readUInt()
            //val sizeOfInitData = reader.readUInt()
            //val sizeOfUnInitData = reader.readUInt()
            val entryPoint = reader.readUInt()
            val baseOfCode = reader.readUInt()
            val baseOfData = if (format == ExeFormat.PE32) {
                reader.readUInt()
            } else {
                0u
            }
            val imageBase = if (format == ExeFormat.PE32) {
                reader.readInt().toULong()
            } else {
                reader.readULong()
            }
            reader.position += 8 //skip unneeded
            //val sectionAlignment = reader.readUInt()
            //val fileAlignment = reader.readUInt()
            reader.position += 12 //skip unneeded
            //val osVersion = "${reader.readUShort()}.${reader.readUShort()}"
            //val imageVersion = "${reader.readUShort()}.${reader.readUShort()}"
            //val subSystemVersion = "${reader.readUShort()}.${reader.readUShort()}"
            reader.position += 16 //skip unused value
            //val winVersion = reader.readInt() //should be 0
            //val sizeOfImage = reader.readUInt()
            //val sizeOfHeaders = reader.readUInt()

            //val checksum = reader.readUInt() //todo should probably verify this

            val subSystemId = reader.readShort()
            val subSystem = SubSystem.values().firstOrNull { it.id == subSystemId }
            check(subSystem != null) {
                "Could not identify subSystem: ${subSystemId.toUShort().toString(16)}"
            }
            val dllFlags = DLLFlag.getFlags(reader.readShort())
            if (format == ExeFormat.PE32) { //skip unneeded
                reader.position += 4 * 4
            } else {
                reader.position += 4 * 8
            }
            /*val stackReserveSize = if (format == ExeFormat.PE32) {
                reader.readInt().toULong()
            } else {
                reader.readULong()
            }
            val stackCommitSize = if (format == ExeFormat.PE32) {
                reader.readInt().toULong()
            } else {
                reader.readULong()
            }
            val stackHeapReserveSize = if (format == ExeFormat.PE32) {
                reader.readInt().toULong()
            } else {
                reader.readULong()
            }
            val stackHeapCommitSize = if (format == ExeFormat.PE32) {
                reader.readInt().toULong()
            } else {
                reader.readULong()
            }*/
            reader.position += 4 //skip unused value
            //val loaderFlags = reader.readShort() //should be 0
            val dirs = Array(reader.readInt()) {
                DataDirectories(reader.readUInt(), reader.readUInt())
            }
            val sections = Array(numOfSections.toInt()) {
                val name = StringBuilder()
                var foundNull = false
                repeat(8) {
                    val char = reader.readAsciiChar()
                    if (!foundNull) {
                        if (char == '\u0000') {
                            foundNull = true
                        } else {
                            name.append(char)
                        }
                    }
                }
                val vSize = reader.readUInt()
                val vAddr = reader.readUInt()
                val sRD = reader.readUInt()
                val pRD = reader.readUInt()
                val pRL = reader.readUInt()
                reader.position += 4 //skip deprecated
                val nRL = reader.readUShort()
                reader.position += 2 //skip deprecated
                val flags = SectionFlags.getFlags(reader.readInt())
                Section(name.toString(), vSize, vAddr, sRD, pRD, pRL, nRL, flags)
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
        }
    }

    fun process(reader: MemorySegmentBuffer): RawCodeFile {
        val list = mutableMapOf<Int, RawCode.Unprocessed>()

        val imports = if (dataDirs.size > 1 && dataDirs[1].size > 0u) {
            sections.first { (it.address..(it.size + it.address)).contains(dataDirs[1].virtualAddress) }
        } else {
            null
        }
        val exports = if (dataDirs.isNotEmpty() && dataDirs[0].size > 0u) {
            sections.first { (it.address..(it.size + it.address)).contains(dataDirs[0].virtualAddress) }
        } else {
            null
        }

        if (imports != null) {
            val offset = dataDirs[1].virtualAddress - imports.address

            reader.position = imports.pointerToRawData.toLong() + offset.toLong()

            val importTables = mutableListOf<ImportTable>()
            do {
                val lookupTableRVA = reader.readUInt()
                reader.position += 4 //skip unneeded
                //val timeStamp = reader.readUInt()
                val forwardChain = reader.readUInt()
                val nameRVA = reader.readUInt()
                reader.position += 4 //skip duplicate table! Reading lookupTableRVA instead
                //val addressTableRVA = reader.readUInt()
                val position = reader.position

                val name = if (nameRVA == 0u) {
                    ""
                } else {
                    reader.position = (nameRVA - imports.address).toLong() + imports.pointerToRawData.toLong()
                    reader.readCString()
                }
                val tables = if (lookupTableRVA == 0u) {
                    emptyList()
                } else {
                    val tables = mutableListOf<ImportLookupTable>()
                    reader.position =
                        (lookupTableRVA - imports.address).toLong() + imports.pointerToRawData.toLong()
                    do {
                        tables.add(ImportLookupTable.compose(reader, format))
                    } while (tables.last() !is ImportLookupTable.NullLookup)
                    tables.removeLast()
                    tables.map {
                        if (it is ImportLookupTable.UnresolvedHintName) {
                            reader.position = (it.rva - imports.address).toLong() + imports.pointerToRawData.toLong()
                            it.resolve(reader)
                        } else {
                            it
                        }
                    }
                }
                reader.position = position
                importTables.add(
                    ImportTable(
                        tables,
                        forwardChain,
                        name
                    )
                )
            } while (!importTables.last().isNull())
            importTables.removeLast()

            if (importTables.isNotEmpty()) {
                println(importTables.joinToString(", ") { it.name })
            }
        }

        if (exports != null) {
            val offset = dataDirs[0].virtualAddress - exports.address
            reader.position = exports.pointerToRawData.toLong() + offset.toLong()
            reader.position += 12 //skip unneeded
            //val exportFlags = reader.readInt() //Reserved, should be 0
            //val timeStamp = reader.readInt()
            //val userVersion = "${reader.readUShort()}.${reader.readUShort()}"
            val nameRVA = reader.readUInt()
            val ordinalBase = reader.readUInt()
            val numTableEntries = reader.readUInt()
            val numNamePointers = reader.readUInt()
            val tableRVA = reader.readUInt()
            val namePointerRVA = reader.readUInt()
            val ordinalTableRVA = reader.readUInt()

            //TODO
        }

        return RawCodeFile(OS.WINDOWS, machine.arch, 0, mutableListOf())
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
}