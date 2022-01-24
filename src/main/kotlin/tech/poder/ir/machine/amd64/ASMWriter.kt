package tech.poder.ir.machine.amd64

import tech.poder.ir.machine.HexNumber
import tech.poder.ir.machine.Pointer
import tech.poder.ir.machine.asm.DBEntry
import tech.poder.ir.machine.asm.ExternalSection
import tech.poder.ir.machine.asm.InternalSection
import tech.poder.ir.machine.asm.Section
import tech.poder.proto.BitOutputStream

/**
 * @author Moocow9m
 * @date 2021/1/23
 * @description: Assembly output for testing and comparing against CPUWriter
 */
data class ASMWriter(val out: BitOutputStream, val forWindows: Boolean = false) {
	init {
		out.write(header)
	}
	companion object {
		private val newLine = "\n".encodeToByteArray()[0]
		private val ret = "ret\n".encodeToByteArray()
		private val header = "DEFAULT REL\n".encodeToByteArray()
		val ARG_ONE = RegisterName.RDI
		val ARG_TWO = RegisterName.RSI
		val ARG_THREE = RegisterName.RDX
		val ARG_FOUR = RegisterName.RCX
		val ARG_FIVE = RegisterName.R8
		val ARG_SIX = RegisterName.R9

		val RETURN_VALUE = RegisterName.RAX

	}

	private fun size(of: RegisterSize): String {
		return when (of) {
			RegisterSize.I8 -> "BYTE"
			RegisterSize.I16 -> "WORD"
			RegisterSize.I32 -> "DWORD"
			RegisterSize.I64 -> "QWORD"
			else -> error("Unknown register size: $of")
		}
	}

	private fun sectionName(sec: Section): String {
		return if (forWindows) {
			if (sec.name != "_start") {
				"_${sec.name}"
			} else {
				"_start"
			}
		} else {
			sec.name
		}
	}

	private fun resolve(input: Any, size: RegisterSize): String {
		return when (input) {
			is Pointer, is Number, is HexNumber -> {
				input.toString()
			}
			is DBEntry -> {
				input.pointer.toString()
			}
			is RegisterName -> {
				RegisterName.realName(input, size)
			}
			is String -> {
				input
			}
			else -> error("Unknown value: ${input::class.java.name}")
		}
	}

	fun add(dst: Any, with: Any, size: RegisterSize) {
		val from = resolve(with, size)
		val to = resolve(dst, size)
		out.write("add $to, $from".encodeToByteArray())
		newLine()
	}

	fun db(entry: DBEntry) {
		out.write(entry.toString().encodeToByteArray())
		newLine()
	}

	fun sub(dst: Any, with: Any, size: RegisterSize) {
		val from = resolve(with, size)
		val to = resolve(dst, size)
		out.write("sub $to, $from".encodeToByteArray())
		newLine()
	}

	fun lea(dst: Any, with: Any, size: RegisterSize) {
		val from = resolve(with, size)
		val to = resolve(dst, size)
		out.write("lea $to, $from".encodeToByteArray())
		newLine()
	}

	fun push(with: Any, size: RegisterSize) {
		val from = resolve(with, size)
		out.write("push ${size(size)} $from".encodeToByteArray())
		newLine()
	}

	fun defineSection(sec: Section) {
		out.write("section ${sectionName(sec)}".encodeToByteArray())
		newLine()
	}

	fun defineLabel(sec: Section) {
		if (sec is ExternalSection) {
			out.write("extern ${sectionName(sec)}\n".encodeToByteArray())
		} else {
			out.write("${sectionName(sec)}:\n".encodeToByteArray())
		}
	}

	fun globalize(sec: Section) {
		out.write("global ${sectionName(sec)}\n".encodeToByteArray())
	}

	fun call(sec: Section) {
		if (sec is InternalSection) {
			out.write("call ${sectionName(sec)}\n".encodeToByteArray())
		} else {
			out.write("call ${sectionName(sec)} wrt ..plt\n".encodeToByteArray())
		}
	}

	fun mov(dst: Any, with: Any, size: RegisterSize) {
		val from = resolve(with, size)
		val to = resolve(dst, size)
		out.write("mov $to, $from".encodeToByteArray())
		newLine()
	}

	fun interrupt(code: HexNumber) {
		out.write("int $code".encodeToByteArray())
		newLine()
	}

	fun newLine() {
		out.write(newLine)
	}

	fun ret() {
		out.write(ret)
	}
}