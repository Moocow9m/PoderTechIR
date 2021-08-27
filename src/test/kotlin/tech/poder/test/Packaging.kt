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
}