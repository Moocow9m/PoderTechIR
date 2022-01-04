package tech.poder.test

import org.junit.jupiter.api.Test
import tech.poder.ir.api.CodeFile
import tech.poder.ir.vm.std.Math
import tech.poder.ptir.PTIR

internal class VMTest {

	@Test
	fun meow() {

		val codeFile = CodeFile("Meow")

		codeFile.addMethod {
			invoke(PTIR.STDCall.PRINT, args = arrayOf("Meow"))
			val powered = newLocal()
			invoke(Math.mathLib, Math.powInt, powered, 2, 3)
		}

		println("Math.Std.Pow: " + Math.mathLib.asCode())

		println("Main: " + codeFile.asCode())
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