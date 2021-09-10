package tech.poder.ir.data.base.unlinked

import tech.poder.ir.commands.Command
import tech.poder.ir.data.Type
import tech.poder.ir.data.base.Method
import tech.poder.ir.data.storage.NamedType
import tech.poder.ir.data.storage.segment.MultiSegment
import tech.poder.ir.data.storage.segment.Segment
import tech.poder.ir.metadata.Visibility
import tech.poder.ir.util.MemorySegmentBuffer

data class UnlinkedMethod internal constructor(
    val package_: UnlinkedPackage,
    val parent: UnlinkedObject?,
    val name: String,
    val returnType: Type,
    val args: Set<NamedType>,
    val visibility: Visibility = Visibility.PRIVATE,
    internal var instructions: Segment = MultiSegment()
) : Method {
    companion object {
        const val methodSeparator = ':'
    }

    init {
        check(name.isNotBlank()) {
            "Namespace cannot be blank!"
        }
        check(!name.contains(UnlinkedObject.objectSeparator)) {
            "Namespace cannot contain ${UnlinkedObject.objectSeparator}!"
        }
        check(!name.contains(UnlinkedObject.fieldSeparator)) {
            "Namespace cannot contain ${UnlinkedObject.fieldSeparator}!"
        }
        check(!name.contains(methodSeparator)) {
            "Namespace cannot contain $methodSeparator!"
        }
    }

    val fullName = "${parent?.fullName ?: package_.namespace}$methodSeparator$name"

    override fun size(): Long {
        return 2L + MemorySegmentBuffer.sequenceSize(name) + returnType.size() + MemorySegmentBuffer.varSize(args.size) + args.sumOf { it.size() } + instructions.sizeBytes()
    }

    override fun save(buffer: MemorySegmentBuffer) {
        buffer.write(0.toByte())
        buffer.write(visibility.ordinal.toByte())
        buffer.writeSequence(name)
        returnType.toBin(buffer)
        buffer.writeVar(args.size)
        args.forEach {
            it.toBin(buffer)
        }
        instructions.toBin(buffer)
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UnlinkedMethod) return false

        if (package_ != other.package_) return false
        if (parent != other.parent) return false
        if (name != other.name) return false
        if (returnType != other.returnType) return false
        if (args != other.args) return false

        return true
    }

    override fun hashCode(): Int {
        var result = package_.hashCode()
        result = 31 * result + (parent?.hashCode() ?: 0)
        result = 31 * result + name.hashCode()
        result = 31 * result + returnType.hashCode()
        result = 31 * result + args.hashCode()
        return result
    }

    override fun toString(): String {
        return "$visibility $fullName(${args.joinToString(", ") { it::class.simpleName!! }}): ${returnType::class.simpleName} { Size: ${instructions.size()} }"
    }

    fun toString(tabs: Int): String {
        return "${"\t".repeat(tabs)}$visibility $fullName(${args.joinToString(", ") { it::class.simpleName!! }}): ${returnType::class.simpleName} { Size: ${instructions.size()} }"
    }

    internal fun toBulk(arrayList: MutableList<Command>) {
        instructions.toBulk(arrayList)
    }
}
