package tech.poder.test

import org.junit.jupiter.api.DisplayName
import tech.poder.ir.machine.Pointer
import tech.poder.ir.machine.amd64.ASMWriter
import tech.poder.ir.machine.amd64.RegisterName
import tech.poder.ir.machine.amd64.RegisterSize
import tech.poder.ir.machine.asm.DBEntry
import tech.poder.ir.machine.asm.Section
import tech.poder.proto.BitOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.Test

class ASMTest {
	@Test
	@DisplayName("Test HelloWorld ASM (GCC compatible)")
	fun helloWorld() {
		val tmp = Paths.get("test.asm").toAbsolutePath()
		Files.deleteIfExists(tmp)
		val nos = BitOutputStream(Files.newOutputStream(tmp))
		val asmw = ASMWriter(nos)
		val main = Section("main")
		//val start = Section("_start")
		val data = Section(".data")
		val text = Section(".text")
		val printf = Section("printf")
		val helloString = DBEntry.construct("myString1", "Hello World!")
		val goodbyeString = DBEntry.construct("myString2", "...and goodbye!")
		val fmt = DBEntry.construct("fmt", "%s\n")

		writeSection(asmw, 0, data) { tabs ->
			tabs(tabs)
			db(helloString)
			tabs(tabs)
			db(goodbyeString)
			tabs(tabs)
			db(fmt)
		}
		writeSection(asmw, 0, text) { tabs ->
			tabs(tabs)
			asmw.globalize(main)
			//tabs(tabs)
			//asmw.globalize(start)
			tabs(tabs)
			asmw.defineLabel(printf, true)
			tabs(tabs)
			defineLabel(main)
			//tabs(tabs)
			//defineLabel(start)

			tabs(tabs)
			asmw.lea(RegisterName.RSI, helloString, RegisterSize.I64)
			tabs(tabs)
			asmw.lea(RegisterName.RDI, fmt, RegisterSize.I64)
			tabs(tabs)
			mov(RegisterName.RAX, 0, RegisterSize.I32)
			tabs(tabs)
			call(printf)

			tabs(tabs)
			asmw.lea(RegisterName.RSI, goodbyeString, RegisterSize.I64)
			tabs(tabs)
			asmw.lea(RegisterName.RDI, fmt, RegisterSize.I64)
			tabs(tabs)
			mov(RegisterName.RAX, 0, RegisterSize.I32)
			tabs(tabs)
			call(printf)

			tabs(tabs)
			ret()
		}
		nos.close()
		//todo: test
	}

	private fun writeSection(writer: ASMWriter, tabs: Int, sec: Section, block: ASMWriter.(Int) -> Unit) {
		writer.tabs(tabs)
		writer.defineSection(sec)
		block.invoke(writer, tabs + 1)
	}
}