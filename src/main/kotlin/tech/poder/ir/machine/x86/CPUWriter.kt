package tech.poder.ir.machine.x86

import tech.poder.proto.BitOutputStream

@JvmInline
value class CPUWriter (val out: BitOutputStream) {
	private fun twoByteOpcodePrefix() {
		out.write(0x0F)
	}


}