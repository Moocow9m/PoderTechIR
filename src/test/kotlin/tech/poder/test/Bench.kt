package tech.poder.test

import tech.poder.ir.parsing.windows.WindowsImage
import tech.poder.ir.util.SegmentUtil
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.io.path.Path
import kotlin.system.measureNanoTime
import kotlin.test.Test

class Bench {

	companion object {
		const val DEFAULT_CYCLES = 1_000_000
	}


	@Test
	fun thing() {

		val path = Path("testFiles", "win", "_OverClockingNvc.dll") //this is too absolute
		val reader = SegmentUtil.mapFile(path, ByteOrder.LITTLE_ENDIAN, FileChannel.MapMode.READ_ONLY)

		Benchmark("Thing").apply {
			bench("OverClockingNvc") {
				WindowsImage.read(reader).processToGeneric(reader)
				reader.position = 0
			}
		}
	}


	class Benchmark(val name: String) {

		inline fun bench(subName: String, warmUpCycles: Int = DEFAULT_CYCLES, benchCycles: Int = DEFAULT_CYCLES, block: () -> Unit) {
			println(measureNS(subName, State.WARMUP, warmUpCycles, block))
			println(measureNS(subName, State.BENCH,  benchCycles,  block))
		}

		inline fun measureNS(subName: String, state: State, cycles: Int, block: () -> Unit): Result {

			var totalTimeNS = 0L

			repeat(cycles) {
				totalTimeNS += measureNanoTime(block)
			}

			return Result(name, subName, state, totalTimeNS, totalTimeNS / cycles)
		}


		data class Result(
			val name: String,
			val subName: String,
			val state: State,
			val totalNS: Long,
			val averageNS: Long
		) {

			override fun toString(): String {
				return (
						"""
                |$name - $subName - $state Average: ${averageNS}ns/op
                """.trimMargin())
			}

		}

		enum class State {
			WARMUP,
			BENCH
		}

	}

}