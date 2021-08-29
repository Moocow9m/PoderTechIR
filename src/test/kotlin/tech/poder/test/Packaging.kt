package tech.poder.test

import tech.poder.ptir.commands.SysCommand
import tech.poder.ptir.data.base.Package
import tech.poder.ptir.metadata.Visibility
import kotlin.test.Test


class Packaging {
    companion object {
        val package_ = Package("test", Visibility.PUBLIC)
    }

    @Test
    fun linear() {
        val meth = package_.newFloatingMethod("linear", Visibility.PRIVATE) {
            it.push("{\nHello World")
            it.push("\n}")
            it.add()
            it.sysCall(SysCommand.PRINT)
            it.return_()
        }
        println(meth)
    }

    @Test
    fun loop() {
        val meth = package_.newFloatingMethod("loop", Visibility.PRIVATE) {
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

    @Test
    fun ifOnly() {
        val meth = package_.newFloatingMethod("if", Visibility.PRIVATE) {
            val after = it.newLabel()
            it.push("{\n")
            it.sysCall(SysCommand.PRINT)
            it.push(0)
            it.push(0)
            it.ifEquals(after)
            it.push("Hello World")
            it.push("\n")
            it.add()
            it.sysCall(SysCommand.PRINT)
            it.placeLabel(after)
            it.push("}\n")
            it.sysCall(SysCommand.PRINT)
            it.return_()
        }
        println(meth)
    }

    @Test
    fun ifInLoop() {
        val meth = package_.newFloatingMethod("ifInLoop", Visibility.PRIVATE) {
            val jump = it.newLabel()
            val after = it.newLabel()
            it.placeLabel(jump)
            it.push("{\n")
            it.sysCall(SysCommand.PRINT)
            it.push(0)
            it.push(0)
            it.ifEquals(after)
            it.push("Hello World")
            it.push("\n")
            it.add()
            it.sysCall(SysCommand.PRINT)
            it.placeLabel(after)
            it.push("}\n")
            it.sysCall(SysCommand.PRINT)
            it.jmp(jump)
            it.return_()
        }
        println(meth)
    }

    @Test
    fun loopInIf() {
        val meth = package_.newFloatingMethod("loopInIf", Visibility.PRIVATE) {
            val jump = it.newLabel()
            val after = it.newLabel()
            it.push("{\n")
            it.sysCall(SysCommand.PRINT)
            it.push(0)
            it.push(0)
            it.ifEquals(after)
            it.placeLabel(jump)
            it.push("Hello World")
            it.push("\n")
            it.add()
            it.sysCall(SysCommand.PRINT)
            it.jmp(jump)
            it.placeLabel(after)
            it.push("}\n")
            it.sysCall(SysCommand.PRINT)
            it.return_()
        }
        println(meth)
    }

    @Test
    fun ifElse() {
        val meth = package_.newFloatingMethod("ifElse", Visibility.PRIVATE) {
            val afterLabel = it.newLabel()
            val elseLabel = it.newLabel()
            it.push("{\n")
            it.sysCall(SysCommand.PRINT)
            it.push(0)
            it.push(0)
            it.ifEquals(elseLabel)
            it.push("Hello World")
            it.push("\n")
            it.add()
            it.sysCall(SysCommand.PRINT)
            it.jmp(afterLabel)
            it.placeLabel(elseLabel)
            it.push("Hello Kat")
            it.sysCall(SysCommand.PRINT)
            it.placeLabel(afterLabel)
            it.push("}\n")
            it.sysCall(SysCommand.PRINT)
            it.return_()
        }
        println(meth)
    }
}