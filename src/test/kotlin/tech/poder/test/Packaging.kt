package tech.poder.test

import tech.poder.ptir.commands.SysCommand
import tech.poder.ptir.data.base.Package
import kotlin.test.Test


class Packaging {
    companion object {
        val package_ = Package("test")
    }

    @Test
    fun linear() {
        val meth = package_.newFloatingMethod("linear") {
            it.push("Hello World")
            it.push("\n")
            it.add()
            it.sysCall(SysCommand.PRINT)
            it.return_()
        }
        println(meth)
    }

    @Test
    fun loop() {
        val meth = package_.newFloatingMethod("loop") {
            val jump = it.newLabel()
            it.push("{\n")
            it.sysCall(SysCommand.PRINT)
            it.placeLabel(jump)
            it.push("Hello World")
            it.push("\n")
            it.add()
            it.sysCall(SysCommand.PRINT)
            it.jmp(jump)
            it.push("}\n")
            it.sysCall(SysCommand.PRINT)
            it.return_()
        }
        println(meth)
    }
}