package tech.poder.ir.data.base

import tech.poder.ir.data.Type
import tech.poder.ir.data.base.unlinked.UnlinkedContainer
import tech.poder.ir.util.MemorySegmentBuffer

interface Container {
	companion object {
		fun newContainer(name: String): UnlinkedContainer {
			return UnlinkedContainer(name)
		}
	}

	val name: String

	fun size(): Long

	fun save(buffer: MemorySegmentBuffer)

	fun locateField(obj: UInt, name: String): UInt?
	fun locateMethod(name: String): UInt?
	fun locateObject(name: String): UInt?

	fun locateField(obj: UInt, id: UInt): Type
	fun locateMethod(id: UInt): Method
	fun locateObject(id: UInt): Object
}