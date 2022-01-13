package tech.poder.test

import org.junit.jupiter.api.Test
import tech.poder.ir.api.CodeFile
import tech.poder.ir.vm.std.Math
import tech.poder.proto.BitInputStream
import tech.poder.proto.BitOutputStream
import java.nio.file.Files
import java.nio.file.Paths

internal class Packaging {

	@Test
	fun generalConstruction() {
		println("Math.Std.Pow: " + Math.mathLib.asCode())

		println("Main: " + VMTest.testCode.asCode())
	}

	@Test
	fun writeReadTest() {
		val tmp = Files.createTempFile(Paths.get("").toAbsolutePath(), "tmp", ".bin")

		Files.deleteIfExists(tmp)

		var nos: BitOutputStream
		listOf(VMTest.testCode, Math.mathLib).forEach {
			nos = BitOutputStream(Files.newOutputStream(tmp))
			it.write(nos)
			nos.close()
			val nis = BitInputStream(Files.newInputStream(tmp))
			val read = CodeFile.read(nis)
			nis.close()
			check(read == it.asCode()) {
				"Read and write are not equal for: ${it.name}"
			}
		}
		Files.deleteIfExists(tmp)
	}
}