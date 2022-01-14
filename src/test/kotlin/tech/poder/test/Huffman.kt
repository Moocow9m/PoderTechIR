package tech.poder.test

import org.junit.jupiter.api.Test
import tech.poder.ir.api.CompiledCode
import tech.poder.ir.util.encode.HuffmanTree
import tech.poder.ir.util.encode.dynamic.DynamicHuffmanLeaf
import tech.poder.ir.util.encode.dynamic.FrequencySorter
import tech.poder.ir.vm.std.Math
import tech.poder.proto.BitInputStream
import tech.poder.proto.BitOutputStream
import tech.poder.proto.Packet
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.deleteExisting
import kotlin.io.path.nameWithoutExtension

class Huffman {
	@Test
	fun createTypeMap() {
		val queue = PriorityQueue(FrequencySorter)
		queue.offer(DynamicHuffmanLeaf(Packet.Types.VUINT, 1000))
		queue.offer(DynamicHuffmanLeaf(Packet.Types.VINT, 950))
		queue.offer(DynamicHuffmanLeaf(Packet.Types.STRING, 900))
		queue.offer(DynamicHuffmanLeaf(Packet.Types.VLONG, 850))
		queue.offer(DynamicHuffmanLeaf(Packet.Types.BOOL, 800))
		queue.offer(DynamicHuffmanLeaf(Packet.Types.ENUM, 750))
		queue.offer(DynamicHuffmanLeaf(Packet.Types.LIST, 700))
		queue.offer(DynamicHuffmanLeaf(Packet.Types.VULONG, 650))
		queue.offer(DynamicHuffmanLeaf(Packet.Types.PACKET, 600))
		queue.offer(DynamicHuffmanLeaf(Packet.Types.BYTE, 550))
		queue.offer(DynamicHuffmanLeaf(Packet.Types.UBYTE, 500))
		queue.offer(DynamicHuffmanLeaf(Packet.Types.VSHORT, 450))
		queue.offer(DynamicHuffmanLeaf(Packet.Types.VUSHORT, 400))
		queue.offer(DynamicHuffmanLeaf(Packet.Types.UNION, 350))
		val finalBin = HuffmanTree.mapToBinary(HuffmanTree.treeToMap(HuffmanTree.fromQueue(queue)))
		var smallest = Int.MAX_VALUE
		var largest = Int.MIN_VALUE
		var average = 0.0
		finalBin.forEach { (_, u) ->
			if (u.size < smallest) smallest = u.size
			if (u.size > largest) largest = u.size
			average += u.size
		}
		average /= finalBin.size
		println("Smallest: $smallest")
		println("Largest: $largest")
		println("Average: $average")
		println("val typesToBin = mapOf(")
		finalBin.forEach { (t, u) ->
			println("\t${t} to listOf(${u.joinToString(", ")}),")
		}
		println(")")
		println("val binToTypes = mapOf(")
		finalBin.forEach { (t, u) ->
			println("\tlistOf(${u.joinToString(", ")}) to ${t},")
		}
		println(")")
		println("const val MAX_BITS: Int = ${largest + 1}")
	}

	@Test
	fun createReadWriteMap() {
		val queue = PriorityQueue(FrequencySorter)
		queue.offer(DynamicHuffmanLeaf(0u, 900))
		queue.offer(DynamicHuffmanLeaf(1u, 850))
		queue.offer(DynamicHuffmanLeaf(2u, 1000))
		queue.offer(DynamicHuffmanLeaf(3u, 800))
		queue.offer(DynamicHuffmanLeaf(4u, 950))
		val finalBin = HuffmanTree.mapToBinary(HuffmanTree.treeToMap(HuffmanTree.fromQueue(queue)))
		var smallest = Int.MAX_VALUE
		var largest = Int.MIN_VALUE
		var average = 0.0
		finalBin.forEach { (_, u) ->
			if (u.size < smallest) smallest = u.size
			if (u.size > largest) largest = u.size
			average += u.size
		}
		average /= finalBin.size
		println("Smallest: $smallest")
		println("Largest: $largest")
		println("Average: $average")
		println("val map0ToBin = mapOf(")
		finalBin.forEach { (t, u) ->
			println("\t${t} to listOf(${u.joinToString(", ")}),")
		}
		println(")")
		println("val binToMap0 = mapOf(")
		finalBin.forEach { (t, u) ->
			println("\tlistOf(${u.joinToString(", ")}) to ${t},")
		}
		println(")")
		println("const val MAX_BITS: Int = ${largest + 1}")
	}

	private val sampleDir: Path = Paths.get("samples").toAbsolutePath()

	private fun createSamples() {
		if (Files.notExists(sampleDir)) sampleDir.toFile().mkdirs()
		Files.walk(sampleDir).forEach {
			if (Files.isRegularFile(it)) {
				it.deleteExisting()
			}
		}
		listOf(VMTest.testCode, Math.mathLib).forEach {
			val nos = BitOutputStream(Files.newOutputStream(Paths.get(sampleDir.toString(), it.name + ".ptbin")))
			it.write(nos)
			nos.close()
		}
	}

	private fun readSamples(): List<CompiledCode> {
		val code = mutableListOf<CompiledCode>()
		Files.walk(sampleDir).forEach { path ->
			if (Files.isRegularFile(path)) {
				Files.newInputStream(path).use {
					val bis = BitInputStream(it)
					code.add(CompiledCode.read(path.nameWithoutExtension, bis))
				}
			}
		}
		return code
	}

	@Test
	fun doSamples() {
		createSamples()
		val result = readSamples()
		println(Packet.frequencyList.toString())
		println(result.toString())
	}
}