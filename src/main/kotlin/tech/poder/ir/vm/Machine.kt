package tech.poder.ir.vm

import tech.poder.ir.data.base.linked.LinkedContainer
import tech.poder.ir.data.storage.memory.AllocatedMemory
import tech.poder.ir.data.storage.memory.MemoryAllocator

class Machine(val bits: Bits, val registers: List<RegisterType>, threads: Int) {
	companion object {
		fun fromTemplate(cpu: CPUTemplate): Machine {
			return Machine(cpu.bits, cpu.registers, cpu.threads)
		}

		fun fromTemplate(cpu: CPUTemplate, threads: Int): Machine {
			return Machine(cpu.bits, cpu.registers, threads)
		}
	}

	val fullThreads = threads.toUInt().coerceAtMost(Runtime.getRuntime().availableProcessors().toUInt())
	val programs = mutableMapOf<UInt, AllocatedMemory>()
	val programNames = mutableMapOf<String, UInt>()
	val memory = MemoryAllocator(1024L * 1024L, 256L) //1KB RAM machine, 256 byte blocks!
	val hasLong = bits == Bits.BIT_64
	val hasFPU: Boolean = registers.any { it.dataFormat == RegisterData.FLOATING_POINT }

	fun loadProgram(linkedContainer: LinkedContainer): UInt {
		var id = programNames[linkedContainer.name]
		if (id != null) {
			return id
		}
		val memory = memory.alloc(linkedContainer.size())
		id = programs.size.toUInt()
		programs[id] = memory
		programNames[linkedContainer.name] = id
		return id
	}
}