package tech.poder.test

import org.junit.jupiter.api.Test
import tech.poder.ir.api.CodeFile
import tech.poder.ir.vm.std.Math
import tech.poder.ptir.PTIR
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Paths

/*import tech.poder.ir.commands.SysCommand
import tech.poder.ir.data.base.Container
import tech.poder.ir.data.base.Method
import tech.poder.ir.data.base.api.APIContainer
import tech.poder.ir.data.base.unlinked.UnlinkedContainer*/


internal class Packaging {


	@Test
	fun generalConstruction() {

		val codeFile = CodeFile("Meow")

		codeFile.addMethod {
			invoke(PTIR.STDCall.PRINT, args = arrayOf("Meow"))
			val powered = newLocal()
			invoke(Math.mathLib, Math.powInt, powered, 2, 3)
		}

		println("Math.Std.Pow: " + Math.mathLib.asCode())

		println("Main: " + codeFile.asCode())
	}

	@Test
	fun writeReadTest() {
		val codeFile = CodeFile("Meow")

		codeFile.addMethod {
			invoke(PTIR.STDCall.PRINT, args = arrayOf("Meow"))
			val powered = newLocal()
			invoke(Math.mathLib, Math.powInt, powered, 2, 3)
		}
		val tmp = Files.createTempFile(Paths.get("").toAbsolutePath(), "tmp", ".bin")

		Files.deleteIfExists(tmp)

		var nos: OutputStream
		listOf(codeFile, Math.mathLib).forEach {
			nos = Files.newOutputStream(tmp)
			it.write(nos)
			nos.close()
			val nis = Files.newInputStream(tmp)
			val read = PTIR.Code.fromBytes(nis)
			nis.close()
			check(read == it.asCode()) {
				"Read and write are not equal for: ${it.name}"
			}
		}

		Files.deleteIfExists(tmp)

	}

	/*@Test
	fun mathCheck() {
		validate(Math.mathLib, Math.pow)

	}

	@Test
	fun linearPow() {
		val container = Container.newContainer("test")
		val package_ = container.newPackage("Packaging")

		val meth = package_.newFloatingMethod("linearPow") {
			it.push(5L) //todo make method calls that need larger numbers cast up!
			it.push(2L)
			it.invokeMethod(Math.pow)
			it.push("")
			it.add()
			it.sysCall(SysCommand.PRINT)
			it.return_()
		}

		validate(container, meth, setOf(Math.api))
	}

	@Test
	fun linear() {
		val container = Container.newContainer("test")
		val package_ = container.newPackage("Packaging")

		val meth = package_.newFloatingMethod("linear") {
			it.push("{\nHello World")
			it.push("\n}")
			it.add()
			it.sysCall(SysCommand.PRINT)
			it.return_()
		}

		validate(container, meth)
	}

	@Test
	fun loop() {
		val container = Container.newContainer("test")
		val package_ = container.newPackage("Packaging")

		val meth = package_.newFloatingMethod("loop") {

			val jump = it.newLabel()

			it.push("{\n")
			it.sysCall(SysCommand.PRINT)
			it.placeLabel(jump)
			it.push("Hello World")
			it.push("\n")
			it.add()
			it.sysCall(SysCommand.PRINT)
			it.jmp(jump)
			it.push("}\n")
			it.sysCall(SysCommand.PRINT)
			it.return_()
		}

		validate(container, meth)
	}

	@Test
	fun ifOnly() {
		val container = Container.newContainer("test")
		val package_ = container.newPackage("Packaging")

		val meth = package_.newFloatingMethod("if") {

			val after = it.newLabel()

			it.push("{\n")
			it.sysCall(SysCommand.PRINT)
			it.push(0)
			it.push(0)
			it.ifEquals(after)
			it.push("Hello World")
			it.push("\n")
			it.add()
			it.sysCall(SysCommand.PRINT)
			it.placeLabel(after)
			it.push("}\n")
			it.sysCall(SysCommand.PRINT)
			it.return_()
		}

		validate(container, meth)
	}

	@Test
	fun ifInLoop() {
		val container = Container.newContainer("test")
		val package_ = container.newPackage("Packaging")

		val meth = package_.newFloatingMethod("ifInLoop") {

			val jump = it.newLabel()
			val after = it.newLabel()

			it.placeLabel(jump)
			it.push("{\n")
			it.sysCall(SysCommand.PRINT)
			it.push(0)
			it.push(0)
			it.ifEquals(after)
			it.push("Hello World")
			it.push("\n")
			it.add()
			it.sysCall(SysCommand.PRINT)
			it.placeLabel(after)
			it.push("}\n")
			it.sysCall(SysCommand.PRINT)
			it.jmp(jump)
			it.return_()
		}

		validate(container, meth)
	}

	@Test
	fun loopInIf() {
		val container = Container.newContainer("test")
		val package_ = container.newPackage("Packaging")

		val meth = package_.newFloatingMethod("loopInIf") {

			val jump = it.newLabel()
			val after = it.newLabel()

			it.push("{\n")
			it.sysCall(SysCommand.PRINT)
			it.push(0)
			it.push(0)
			it.ifEquals(after)
			it.placeLabel(jump)
			it.push("Hello World")
			it.push("\n")
			it.add()
			it.sysCall(SysCommand.PRINT)
			it.jmp(jump)
			it.placeLabel(after)
			it.push("}\n")
			it.sysCall(SysCommand.PRINT)
			it.return_()
		}

		validate(container, meth)
	}

	@Test
	fun ifElse() {
		val container = Container.newContainer("test")
		val package_ = container.newPackage("Packaging")

		val meth = package_.newFloatingMethod("ifElse") {

			val afterLabel = it.newLabel()
			val elseLabel = it.newLabel()

			it.push("{\n")
			it.sysCall(SysCommand.PRINT)
			it.push(0)
			it.push(0)
			it.ifEquals(elseLabel)
			it.push("Hello World")
			it.push("\n")
			it.add()
			it.sysCall(SysCommand.PRINT)
			it.jmp(afterLabel)
			it.placeLabel(elseLabel)
			it.push("Hello Kat\n")
			it.sysCall(SysCommand.PRINT)
			it.placeLabel(afterLabel)
			it.push("}\n")
			it.sysCall(SysCommand.PRINT)
			it.return_()
		}

		validate(container, meth)
	}

	private fun validate(container: UnlinkedContainer, method: Method, deps: Set<APIContainer> = emptySet()) {
		SegmentUtil.allocate(container.size()).use {
			container.save(it)
			check(it.remaining() == 0L) {
				"Method did not use full segment!"
			}
		}
		val linked = container.linkAndOptimize(deps)
		SegmentUtil.allocate(linked.first.size()).use {
			linked.first.save(it)
			check(it.remaining() == 0L) {
				"API Method did not use full segment!"
			}
		}
		SegmentUtil.allocate(linked.second.size()).use {
			linked.second.save(it)
			check(it.remaining() == 0L) {
				"Bin Method did not use full segment!"
			}
		}
		println("$method--Unlinked Binary Size: ${container.size()}--Linked Binary Size: ${linked.second.size()}--Linked API Size: ${linked.first.size()}")
	}*/
}