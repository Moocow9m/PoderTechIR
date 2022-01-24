package tech.poder.test

import org.junit.jupiter.api.DisplayName
import tech.poder.ir.machine.Pointer
import tech.poder.ir.machine.amd64.ASMWriter
import tech.poder.ir.machine.amd64.RegisterName
import tech.poder.ir.machine.amd64.RegisterSize
import tech.poder.ir.machine.asm.DBEntry
import tech.poder.ir.machine.asm.ExternalSection
import tech.poder.ir.machine.asm.InternalSection
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
		val main = InternalSection("main")
		//val start = InternalSection("_start")
		val data = InternalSection(".data")
		val text = InternalSection(".text")
		val printf = ExternalSection("printf")
		val helloString = DBEntry.construct("myString1", "Hello World!")
		val goodbyeString = DBEntry.construct("myString2", "And goodbye!")
		val fmt = DBEntry.construct("fmt", "%s\n")

		writeSection(asmw, data) {
			db(helloString)
			db(goodbyeString)
			db(fmt)
		}
		writeSection(asmw, text) {
			globalize(main)
			//globalize(start)
			defineLabel(printf)
			defineLabel(main)
			//defineLabel(start)

			lea(ASMWriter.ARG_TWO, helloString, RegisterSize.I64)
			lea(ASMWriter.ARG_ONE, fmt, RegisterSize.I64)
			mov(RegisterName.RAX, 0, RegisterSize.I32)
			call(printf)

			lea(ASMWriter.ARG_TWO, goodbyeString, RegisterSize.I64)
			lea(ASMWriter.ARG_ONE, fmt, RegisterSize.I64)
			mov(RegisterName.RAX, 0, RegisterSize.I32)
			call(printf)

			mov(ASMWriter.RETURN_VALUE, 0, RegisterSize.I32) //Exit code 0
			ret()
		}
		nos.close()
		//todo: test
	}

	private fun writeSection(writer: ASMWriter, sec: Section, block: ASMWriter.() -> Unit) {
		writer.defineSection(sec)
		block.invoke(writer)
	}
}