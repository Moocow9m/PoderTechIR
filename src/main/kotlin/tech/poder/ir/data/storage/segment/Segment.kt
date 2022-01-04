package tech.poder.ir.data.storage.segment

import tech.poder.ir.commands.Command
import tech.poder.ir.data.Type
import tech.poder.ir.data.base.api.APIContainer
import tech.poder.ir.data.base.unlinked.UnlinkedContainer
import tech.poder.ir.data.base.unlinked.UnlinkedMethod
import tech.poder.ir.metadata.NameId
import tech.poder.ir.util.MemorySegmentBuffer
import java.util.*

interface Segment {

	fun eval(
		dependencies: Set<APIContainer>,
		self: UnlinkedContainer,
		method: UnlinkedMethod,
		stack: Stack<Type>,
		currentIndex: Int,
		vars: MutableMap<CharSequence, UInt>,
		type: MutableMap<UInt, Type>,
		depMap: List<NameId>
	): Int

	fun size(): Int

	fun toBulk(storage: MutableList<Command>)

	fun toBin(buffer: MemorySegmentBuffer)

	fun sizeBytes(): Long
}