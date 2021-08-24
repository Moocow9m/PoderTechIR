package tech.poder.ir.instructions.common

import tech.poder.ir.instructions.Command

data class Instruction(val opcode: Command, var extra: Array<Any> = emptyArray()) {
    companion object {
        fun create(opcode: Command, vararg args: Any): Instruction {
            return Instruction(opcode, Array(args.size) { args[it] })
        }
    }
}