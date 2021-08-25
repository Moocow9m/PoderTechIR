package tech.poder.test

import tech.poder.ir.instructions.common.Method
import tech.poder.ir.instructions.common.special.SpecialCalls
import tech.poder.ir.std.Basic
import tech.poder.ir.vm.Machine
import java.math.BigDecimal

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

        var warmup = BigDecimal.ZERO
        var bench = BigDecimal.ZERO
        val iterations = 10_000_000L
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
        val testMath = Method.create("test2") { builder ->
            builder.push(1.0)
            builder.push(1.0)
            builder.add()
            builder.push(1.0)
            builder.push(10.0)
            builder.div()
            builder.add()
            builder.push(1000.0)
            builder.push(10.0)
            builder.mul()
            builder.push(10.0)
            builder.push(10.0)
            builder.invoke(Basic.math.methods.first { it.name == "pow" })
            builder.div()
            builder.add()
            builder.push("\n")
            builder.add()
            builder.sysCall(SpecialCalls.PRINT)
        }
        Machine.loadCode(Basic.math)
        Machine.loadCode(test1, testMath, libA, libB, main)
        Machine.execute("static.main")
        Machine.execute("static.test1")
        Machine.execute("static.test2")
        /*repeat(iterations.toInt()) {
            warmup += measureTimeMillis {
                Machine.execute("static.main")
            }.toBigDecimal()
        }
        repeat(iterations.toInt()) {
            bench += measureTimeMillis {
                Machine.execute("static.main")
            }.toBigDecimal()
        }
        println(
            "Iterations:$iterations" +
                    "\n[WARMUP] Time: ${warmup}ms" +
                    "\n[WARMUP] Ops per milli: ${iterations.toBigDecimal().divide(warmup, MathContext.DECIMAL32)}" +
                    "\n[BENCH] Time: ${bench}ms" +
                    "\n[BENCH] Ops per milli: ${iterations.toBigDecimal().divide(bench, MathContext.DECIMAL32)}"
        )*/
    }
}