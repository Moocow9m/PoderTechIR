package tech.poder.test

import org.junit.jupiter.api.Test
import tech.poder.ir.api.CodeFile
import tech.poder.ir.vm.VirtualMachine
import tech.poder.ir.vm.std.Math
import tech.poder.ptir.PTIR

internal class VMTest {

	@Test
	fun helloWorld() {
		val codeFile = CodeFile("Meow")

		val main = codeFile.addMethod {
			invoke(PTIR.STDCall.PRINT, args = arrayOf("Hello, World!\n"))
		}
		val code = codeFile.asCode()
		println(code)
		//VirtualMachine.loadFile(code) //Not needed as exec also loads the target file
		VirtualMachine.exec(code, main)
	}

	@Test
	fun helloWorldWithPow() {
		val codeFile = CodeFile("Meow")

		val main = codeFile.addMethod {
			val tmp = newLocal()
			invoke(Math.mathLib, Math.powInt, tmp, 10L, 10L)
			invoke(PTIR.STDCall.PRINT, args = arrayOf("10^10 = "))
			add(tmp, tmp, "!\n")
			invoke(PTIR.STDCall.PRINT, args = arrayOf(tmp))
			invoke(PTIR.STDCall.PRINT, args = arrayOf("Hello, World!\n"))
		}
		val code = codeFile.asCode()
		println(code)
		VirtualMachine.loadFile(Math.code)
		//VirtualMachine.loadFile(code) //Not needed as exec also loads the target file
		VirtualMachine.exec(code, main)
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