package tech.poder.ir.commands

import tech.poder.ir.util.MemorySegmentBuffer

interface DebugValue : Command {
	companion object {
		private val debugLineNumber = SimpleValue.last + 1
		private val debugLine = debugLineNumber + 1
	}

	@JvmInline
	value class LineNumber(val line: UInt) : DebugValue {
		override fun id(): Int {
			return debugLineNumber
		}

		override fun sizeBits(): Long {
			return (MemorySegmentBuffer.varSize(id()) + MemorySegmentBuffer.varSize(line.toInt())) * 8L
		}

		override fun toBin(output: MemorySegmentBuffer) {
			output.writeVar(id())
			output.writeVar(line.toInt())
		}
	}

	@JvmInline
	value class Line(val line: CharSequence) : DebugValue {
		override fun id(): Int {
			return debugLine
		}

		override fun sizeBits(): Long {
			return (MemorySegmentBuffer.varSize(id()) + MemorySegmentBuffer.sequenceSize(line)) * 8L
		}

		override fun toBin(output: MemorySegmentBuffer) {
			output.writeVar(id())
			output.writeSequence(line)
		}
	}
}