package tech.poder.test

import org.junit.jupiter.api.Test
import tech.poder.ir.api.CodeFile
import tech.poder.ir.vm.VirtualMachine
import tech.poder.ir.vm.std.Math
import tech.poder.ptir.PTIR

internal class VMTest {

	companion object {
		val testCode = CodeFile("Meow")
		val helloWorld = testCode.addMethod {
			print("Hello, World!\n")
		}

		val helloWorldWithPow = testCode.addMethod {
			val tmp = newLocal()
			invoke(Math.mathLib, Math.powInt, tmp, 13L, 16L)
			print("13^16 = ")
			add(tmp, tmp, "!\n")
			print(tmp)
		}

	}

	@Test
	fun helloWorld() {
		val code = testCode.asCode()
		//VirtualMachine.loadFile(code) //Not needed as exec also loads the target file
		VirtualMachine.exec(code, helloWorld)
	}

	@Test
	fun helloWorldWithPow() {
		val code = testCode.asCode()
		VirtualMachine.loadFile(Math.code)
		//VirtualMachine.loadFile(code) //Not needed as exec also loads the target file
		VirtualMachine.exec(code, helloWorldWithPow)
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