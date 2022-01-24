package tech.poder.test

import org.junit.jupiter.api.DisplayName
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
		val windows = true //todo determine if OS is windows or linux!
		val tmp = Paths.get("test.asm").toAbsolutePath()
		Files.deleteIfExists(tmp)
		val nos = BitOutputStream(Files.newOutputStream(tmp))
		val asm = ASMWriter(nos, windows)
		val main = InternalSection("main")
		//val start = InternalSection("_start")
		val data = InternalSection(".data")
		val text = InternalSection(".text")
		val exit = ExternalSection("ExitProcess")
		val initCRT = ExternalSection("_CRT_INIT")
		val printf = ExternalSection("printf")
		val helloString = DBEntry.construct("myString1", "Hello World!")
		val goodbyeString = DBEntry.construct("myString2", "And goodbye!")
		val fmt = DBEntry.construct("fmt", "%s\n")

		writeSection(asm, data) {
			db(helloString)
			db(goodbyeString)
			db(fmt)
		}
		writeSection(asm, text) {
			globalize(main)
			//globalize(start)
			defineLabel(printf)
			defineLabel(main)
			//defineLabel(start)
			if (windows) {
				defineLabel(initCRT)
				defineLabel(exit)
				push(RegisterName.RBP, RegisterSize.I64)
				mov(RegisterName.RBP, RegisterName.RSP, RegisterSize.I64)
				sub(RegisterName.RSP, 32, RegisterSize.I64)
				call(initCRT)
				lea(RegisterName.RDX, helloString, RegisterSize.I64)
				lea(RegisterName.RCX, fmt, RegisterSize.I64)
			} else {
				lea(RegisterName.RSI, helloString, RegisterSize.I64)
				lea(RegisterName.RDI, fmt, RegisterSize.I64)
				mov(RegisterName.RAX, 0, RegisterSize.I32)
			}
			call(printf)

			if (windows) {
				lea(RegisterName.RDX, goodbyeString, RegisterSize.I64)
				lea(RegisterName.RCX, fmt, RegisterSize.I64)
			} else {
				lea(RegisterName.RSI, goodbyeString, RegisterSize.I64)
				lea(RegisterName.RDI, fmt, RegisterSize.I64)
				mov(RegisterName.RAX, 0, RegisterSize.I32)
			}
			call(printf)


			if (windows) {
				mov(RegisterName.RCX, 0, RegisterSize.I32) //Exit code 0
				call(exit)
			} else {
				mov(RegisterName.RAX, 0, RegisterSize.I32) //Exit code 0
				ret()
			}
		}
		nos.close()
		//todo: test
	}

	private fun writeSection(writer: ASMWriter, sec: Section, block: ASMWriter.() -> Unit) {
		writer.defineSection(sec)
		block.invoke(writer)
	}
}