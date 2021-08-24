package tech.poder.ir.instructions.common.constants

data class ConstString(val data: String) : Constant {
    override fun toString(): String {
        return data
    }
}
