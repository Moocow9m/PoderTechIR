package tech.poder.test

import org.junit.jupiter.api.Test
import tech.poder.ir.api.CodeFile
import tech.poder.ir.api.Variable
import tech.poder.ir.vm.std.Math
import tech.poder.ptir.PTIR
import java.io.ByteArrayOutputStream

internal class VMTest {

	@Test
	fun meow() {

		val codeFile = CodeFile("Meow")

		codeFile.addMethod {
			invoke(PTIR.STDCall.PRINT, args = arrayOf("Meow"))
			val powered = Variable()
			invoke(Math.mathLib, Math.powInt, powered, 2, 3)
		}

		val bytesOut = ByteArrayOutputStream()

		codeFile.write(bytesOut)
		val meow = PTIR.Code.fromBytes(bytesOut.toByteArray().inputStream())

		println(meow)
	}

	/*@Test
	fun helloWorld() {

		val meth = Packaging.package_.newFloatingMethod("helloWorld", Visibility.PRIVATE) {
			it.push("{\n\tHello World")
			it.push("\n}\n")
			it.add()
			it.sysCall(SysCommand.PRINT)
			it.return_()
		}

		Runner().apply {
			loadPackage(Packaging.package_)
			execute(meth.fullName)
		}
	}

	@Test
	fun allocation() {

		val mem = MemoryAllocator(1_073_741_824)
		val frag = mem.alloc(128)

		println(frag)
	}*/
}