package tech.poder.test

import tech.poder.ptir.Machine
import tech.poder.ptir.commands.SysCommand
import tech.poder.ptir.metadata.Visibility
import kotlin.test.Test

class VMTest {
    @Test
    fun helloWorld() {
        val meth = Packaging.package_.newFloatingMethod("helloWorld", Visibility.PRIVATE) {
            it.push("{\n\tHello World")
            it.push("\n}\n")
            it.add()
            it.sysCall(SysCommand.PRINT)
            it.return_()
        }
        val machine = Machine()
        machine.loadPackage(Packaging.package_)
        machine.execute(meth.fullName)
    }
}