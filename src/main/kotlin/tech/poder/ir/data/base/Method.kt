package tech.poder.ir.data.base

import tech.poder.ir.data.storage.Instruction
import tech.poder.ir.data.storage.NamedType
import tech.poder.ir.data.storage.Type
import tech.poder.ir.data.storage.segment.Segment
import tech.poder.ir.metadata.Visibility

data class Method internal constructor(
    val package_: Package,
    val parent: Object?,
    val name: String,
    val returnType: Type?,
    val args: Set<NamedType>,
    val visibility: Visibility,
    var localVarSize: Int = 0,
    internal var instructions: Segment
) {
    val fullName by lazy {
        val start = parent?.fullName ?: package_.namespace
        "$start:$name"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Method) return false

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
        return "$visibility $fullName(${args.joinToString(", ")}): ${returnType ?: "VOID"} { Size: ${instructions.size()} }"
    }

    fun toString(tabs: Int): String {
        val tabBuilder = StringBuilder()
        repeat(tabs) {
            tabBuilder.append('\t')
        }
        return "$tabBuilder$visibility $fullName(${args.joinToString(", ")}): ${returnType ?: "VOID"} { Size: ${instructions.size()} }"
    }

    internal fun toBulk(arrayList: ArrayList<Instruction>) {
        instructions.toBulk(arrayList)
    }
}
