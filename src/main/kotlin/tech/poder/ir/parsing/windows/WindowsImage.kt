package tech.poder.ir.parsing.windows

import tech.poder.ir.parsing.generic.OS
import tech.poder.ir.parsing.generic.RawCodeFile
import tech.poder.ir.parsing.windows.exports.Export
import tech.poder.ir.parsing.windows.exports.ExportDirectory
import tech.poder.ir.parsing.windows.exports.ExportEntry
import tech.poder.ir.parsing.windows.flags.CoffFlag
import tech.poder.ir.parsing.windows.flags.CoffMachine
import tech.poder.ir.parsing.windows.flags.DLLFlag
import tech.poder.ir.parsing.windows.flags.SectionFlags
import tech.poder.ir.parsing.windows.imports.ImportLookupTable
import tech.poder.ir.parsing.windows.imports.ImportTable
import tech.poder.ir.util.MemorySegmentBuffer

class WindowsImage(
	val machine: CoffMachine,
	val coffFlags: List<CoffFlag>,
	val dllFlags: List<DLLFlag>,
	val format: ExeFormat,
	val subSystem: SubSystem,
	val dataDirs: Array<DataDirectories>,
	val sections: Array<Section>,
	val entryLocation: UInt,
	val baseOfCode: UInt,
	val exports: ExportDirectory?,
	val imports: List<ImportTable>?
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
			//reader.position += 4 //skip unneeded
			val baseOfCode = reader.readUInt()
			if (format == ExeFormat.PE32) { //skip unneeded
				reader.position += 4
			}
			/*val baseOfData = if (format == ExeFormat.PE32) {
				reader.readUInt()
			} else {
				0u
			}*/
			if (format == ExeFormat.PE32) { //skip unneeded
				reader.position += 4
			} else {
				reader.position += 8
			}
			/*val imageBase = if (format == ExeFormat.PE32) {
				reader.readInt().toULong()
			} else {
				reader.readULong()
			}*/
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
				processExports(dirs, sections, reader),
				processImports(dirs, sections, reader, format)
			)
		}

		private fun processExports(
			dirs: Array<DataDirectories>,
			sections: Array<Section>,
			reader: MemorySegmentBuffer
		): ExportDirectory? {
			val exportSection = if (dirs.isNotEmpty() && dirs[0].size > 0u) {
				resolveVAToSection(dirs[0].virtualAddress, sections)
			} else {
				null
			}

			return if (exportSection != null) {
				val offset = dirs[0].virtualAddress - exportSection.address
				reader.position = exportSection.pointerToRawData.toLong() + offset.toLong()
				reader.position += 12 //skip unneeded
				//val exportFlags = reader.readInt() //Reserved, should be 0
				//val timeStamp = reader.readInt()
				//val userVersion = "${reader.readUShort()}.${reader.readUShort()}"
				val nameRVA = reader.readUInt()
				val ordinalBase = reader.readUInt()
				val numTableEntries = reader.readInt()
				val numNamePointers = reader.readInt()
				val tableRVA = reader.readUInt()
				val namePointerRVA = reader.readUInt()
				val ordinalTableRVA = reader.readUInt()
				reader.position = resolveSection(nameRVA, exportSection)
				val name = reader.readCString()
				reader.position = resolveSection(tableRVA, exportSection)
				val listOfTables = mutableListOf<Export>()
				repeat(numTableEntries) {
					val addr = reader.readUInt()
					listOfTables.add(
						if (addr in dirs[0].range) {
							/*val pos = reader.position
							reader.position = resolveSection(addr, exportSection)
							val methodName = reader.readCString()
							reader.position = pos
							ExportTable.ExportName(methodName)*/
							error("Name RVA not validated yet!")
						} else {
							Export.ExportAddress(addr)
						}
					)
				}
				reader.position = resolveSection(namePointerRVA, exportSection)
				val listOfNames = mutableListOf<String>()
				repeat(numNamePointers) {
					val rva = reader.readUInt()
					val position = reader.position
					reader.position = resolveSection(rva, exportSection)
					listOfNames.add(reader.readCString())
					reader.position = position
				}

				reader.position = resolveSection(ordinalTableRVA, exportSection)
				val listOfExports = mutableListOf<ExportEntry>()
				repeat(numNamePointers) {
					listOfExports.add(ExportEntry(listOfNames[it], listOfTables[it], reader.readUShort()))
				}
				ExportDirectory(name, ordinalBase, listOfExports)
			} else {
				null
			}
		}

		private fun processImports(
			dirs: Array<DataDirectories>,
			sections: Array<Section>,
			reader: MemorySegmentBuffer,
			format: ExeFormat
		): List<ImportTable>? {
			val importSection = if (dirs.size > 1 && dirs[1].size > 0u) {
				resolveVAToSection(dirs[1].virtualAddress, sections)
			} else {
				null
			}

			return if (importSection != null) {
				val offset = dirs[1].virtualAddress - importSection.address

				reader.position = importSection.pointerToRawData.toLong() + offset.toLong()

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
						reader.position = resolveSection(nameRVA, importSection)
						reader.readCString()
					}
					val tables = if (lookupTableRVA == 0u) {
						emptyList()
					} else {
						val tables = mutableListOf<ImportLookupTable>()
						reader.position = resolveSection(lookupTableRVA, importSection)
						do {
							tables.add(ImportLookupTable.compose(reader, format))
						} while (tables.last() !is ImportLookupTable.NullLookup)
						tables.removeLast()
						tables.map {
							if (it is ImportLookupTable.UnresolvedHintName) {
								reader.position = resolveSection(it.rva, importSection)
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
				importTables.ifEmpty { null }
			} else {
				null
			}
		}

		private fun resolveVAToSection(va: UInt, sections: Array<Section>): Section {
			return sections.first { it.range.contains(va) }
		}

		private fun resolveSection(rva: UInt, section: Section): Long {
			return (rva - section.address).toLong() + section.pointerToRawData.toLong()
		}
	}

	fun processToGeneric(reader: MemorySegmentBuffer): RawCodeFile {

		val externalCodeStartPoints = mutableSetOf<Long>()
		val section = resolveVAToSection(baseOfCode, sections)

		val startPoint =
			if (entryLocation != 0u) {
				resolveSection(entryLocation, section)
			} else {
				-1L
			}

		if (startPoint > 0L) {
			externalCodeStartPoints.add(startPoint)
		}

		//Start point is to be called on Load and Unload of DLL
		exports?.exportEntries?.filter { it.entry is Export.ExportAddress }?.forEach {
			externalCodeStartPoints.add(resolveSection((it.entry as Export.ExportAddress).exportRVA, section))
		}

		return RawCodeFile(OS.WINDOWS, machine.arch, 0, mutableListOf())
	}
}