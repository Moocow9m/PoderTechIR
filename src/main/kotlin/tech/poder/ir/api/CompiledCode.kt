package tech.poder.ir.api

import tech.poder.proto.BitInputStream
import tech.poder.ptir.PTIR

data class CompiledCode(val name: String, val code: PTIR.Code) {
	companion object {
		fun read(name: String, bis: BitInputStream): CompiledCode {
			return CompiledCode(name, CodeFile.read(bis))
		}
	}
}