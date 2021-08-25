package tech.poder.test

import tech.poder.ir.instructions.common.Method
import tech.poder.ir.instructions.common.special.SpecialCalls
import tech.poder.ir.std.Basic
import tech.poder.ir.vm.Machine

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val test1 = Method.create("test1") { builder ->
            builder.push(2)
            builder.push(5)
            builder.invoke(Basic.math.methods.first { it.name == "pow" })
            builder.push("\n")
            builder.add()
            builder.sysCall(SpecialCalls.PRINT)
            builder.return_()
        }
        val libA = Method.create("printHelloKat") {
            it.push("Hello Kat\n")
            it.sysCall(SpecialCalls.PRINT)
        }
        val libB = Method.create("printHelloWorld") {
            it.push("Hello World")
            it.push("\n")
            it.add()
            it.sysCall(SpecialCalls.PRINT)
        }
        val main = Method.create("main") {
            val elseLabel = it.newLabel()
            val afterLabel = it.newLabel()
            it.sysCall(SpecialCalls.RANDOM_INT, -1, 1)
            it.push(0)
            it.ifEquals(elseLabel)
            it.invoke(libA)
            it.jmp(afterLabel)
            it.placeLabel(elseLabel)
            it.invoke(libB)
            it.placeLabel(afterLabel)
            it.return_()
        }
        Machine.loadCode(Basic.math)
        Machine.loadCode(test1, libA, libB, main)
        Machine.execute("static.main")
        Machine.execute("static.test1")
    }
}