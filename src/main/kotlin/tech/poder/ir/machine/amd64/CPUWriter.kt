package tech.poder.ir.machine.amd64

import tech.poder.proto.BitOutputStream

@JvmInline
value class CPUWriter (val out: BitOutputStream) {
	private fun twoByteOpcodePrefix() {
		out.write(0x0F)
	}

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

	fun rValue(register: RegisterName) {
		when (register) {
			RegisterName.RAX, RegisterName.R8 -> {
				out.writeBit(false)
				out.writeBit(false)
				out.writeBit(false)
			}
			RegisterName.RCX, RegisterName.R9 -> {
				out.writeBit(false)
				out.writeBit(false)
				out.writeBit(true)
			}
			RegisterName.RDX, RegisterName.R10 -> {
				out.writeBit(false)
				out.writeBit(true)
				out.writeBit(false)
			}
			RegisterName.RBX, RegisterName.R11 -> {
				out.writeBit(false)
				out.writeBit(true)
				out.writeBit(true)
			}
			RegisterName.RSP, RegisterName.R12 -> {
				System.err.println("[WARN] $register may not be allowed in rValue")
				out.writeBit(true)
				out.writeBit(false)
				out.writeBit(false)
			}
			RegisterName.RBP, RegisterName.R13 -> {
				System.err.println("[WARN] $register may not be allowed in rValue")
				out.writeBit(true)
				out.writeBit(false)
				out.writeBit(true)
			}
			RegisterName.RSI, RegisterName.R14 -> {
				out.writeBit(true)
				out.writeBit(true)
				out.writeBit(false)
			}
			RegisterName.RDI, RegisterName.R15 -> {
				out.writeBit(true)
				out.writeBit(true)
				out.writeBit(true)
			}
		}
	}

	fun add(size: RegisterSize, left: RegisterName, right: RegisterName) {

	}

}