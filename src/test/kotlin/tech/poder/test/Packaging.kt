package tech.poder.test

import org.junit.jupiter.api.Test
import tech.poder.ir.api.CodeFile
import tech.poder.ir.vm.std.Math
import tech.poder.ptir.PTIR
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Paths

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
}