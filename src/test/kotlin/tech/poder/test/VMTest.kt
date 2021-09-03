package tech.poder.test

import tech.poder.ir.Machine
import tech.poder.ir.commands.SysCommand
import tech.poder.ir.data.storage.memory.MemoryAllocator
import tech.poder.ir.metadata.Visibility
import kotlin.test.Test

internal class VMTest {

    @Test
    fun helloWorld() {

        val meth = Packaging.package_.newFloatingMethod("helloWorld", Visibility.PRIVATE) {
            it.push("{\n\tHello World")
            it.push("\n}\n")
            it.add()
            it.sysCall(SysCommand.PRINT)
            it.return_()
        }

        Machine().apply {
            loadPackage(Packaging.package_)
            execute(meth.fullName)
        }
    }

    @Test
    fun allocation() {

        val mem = MemoryAllocator(1_073_741_824)
        val frag = mem.alloc(128)

        println(frag)
    }
}