package tech.poder.ir.machine

import tech.poder.proto.BitOutputStream

object LinuxBuilder {
	enum class RegistrySize {
		M8,
		M16,
		M32,
		M64,
		M128,
	}
	enum class RegisterName {
		RAX,
		RCX,
		RDX,
		RBX,
		RSP,
		RBP,
		RSI,
		RDI,
		R8,
		R9,
		R10,
		R11,
		R12,
		R13,
		R14,
		R15
	}
	@JvmInline
	value class AMD64(val out: BitOutputStream) {
		fun rex(w: Boolean, r: Boolean, x: Boolean, b: Boolean) {
			//Begin Rex Code
			out.writeBit(false)
			out.writeBit(true)
			out.writeBit(false)
			out.writeBit(false)
			//End Rex Code
			out.writeBit(w)
			out.writeBit(r)
			out.writeBit(x)
			out.writeBit(b)
		}

	}
}