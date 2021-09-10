package tech.poder.test

import tech.poder.ir.commands.SysCommand
import tech.poder.ir.data.base.Container
import tech.poder.ir.data.base.Method
import tech.poder.ir.data.base.unlinked.UnlinkedContainer
import tech.poder.ir.std.Math
import tech.poder.ir.util.SegmentUtil
import kotlin.test.Test


internal class Packaging {

    @Test
    fun mathCheck() {
        validate(Math.mathLib, Math.pow)
    }

    @Test
    fun linear() {
        val container = Container.newContainer("test")
        val package_ = container.newPackage("Packaging")

        val meth = package_.newFloatingMethod("linear") {
            it.push("{\nHello World")
            it.push("\n}")
            it.add()
            it.sysCall(SysCommand.PRINT)
            it.return_()
        }

        validate(container, meth)
    }

    @Test
    fun loop() {
        val container = Container.newContainer("test")
        val package_ = container.newPackage("Packaging")

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

        validate(container, meth)
    }

    @Test
    fun ifOnly() {
        val container = Container.newContainer("test")
        val package_ = container.newPackage("Packaging")

        val meth = package_.newFloatingMethod("if") {

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

        validate(container, meth)
    }

    @Test
    fun ifInLoop() {
        val container = Container.newContainer("test")
        val package_ = container.newPackage("Packaging")

        val meth = package_.newFloatingMethod("ifInLoop") {

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

        validate(container, meth)
    }

    @Test
    fun loopInIf() {
        val container = Container.newContainer("test")
        val package_ = container.newPackage("Packaging")

        val meth = package_.newFloatingMethod("loopInIf") {

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

        validate(container, meth)
    }

    @Test
    fun ifElse() {
        val container = Container.newContainer("test")
        val package_ = container.newPackage("Packaging")

        val meth = package_.newFloatingMethod("ifElse") {

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
            it.push("Hello Kat\n")
            it.sysCall(SysCommand.PRINT)
            it.placeLabel(afterLabel)
            it.push("}\n")
            it.sysCall(SysCommand.PRINT)
            it.return_()
        }

        validate(container, meth)
    }

    private fun validate(container: UnlinkedContainer, method: Method) {
        SegmentUtil.allocate(container.size()).use {
            container.save(it)
            check(it.remaining() == 0L) {
                "Method did not use full segment!"
            }
        }
        val linked = container.link()
        SegmentUtil.allocate(linked.size()).use {
            linked.save(it)
            check(it.remaining() == 0L) {
                "Packed Method did not use full segment!"
            }
        }
        println("$method -- Unlinked Binary Size: ${container.size()} -- Linked Binary Size: ${linked.size()}")
    }
}