package tech.poder.ir.machine.amd64

import tech.poder.ir.machine.HexNumber
import tech.poder.ir.machine.Pointer
import tech.poder.ir.machine.asm.DBEntry
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

	fun defineLabel(sec: Section, external: Boolean = false) {
		if (external) {
			out.write("extern ${sectionName(sec)}".encodeToByteArray())
		} else {
			out.write("${sectionName(sec)}:".encodeToByteArray())
		}
		newLine()
	}

	fun globalize(sec: Section) {
		out.write("global ${sectionName(sec)}".encodeToByteArray())
		newLine()
	}

	fun call(sec: Section) {
		out.write("call ${sectionName(sec)} wrt ..plt".encodeToByteArray())
		newLine()
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

	fun tabs(amount: Int) {
		out.write("\t".repeat(amount).encodeToByteArray())
	}

	fun ret() {
		out.write(ret)
	}
}