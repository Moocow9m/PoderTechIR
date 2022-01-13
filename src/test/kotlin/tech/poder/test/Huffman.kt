package tech.poder.test

import org.junit.jupiter.api.Test
import tech.poder.ir.util.encode.HuffmanTree
import tech.poder.ir.util.encode.dynamic.DynamicHuffmanLeaf
import tech.poder.ir.util.encode.dynamic.FrequencySorter
import tech.poder.proto.Packet
import java.util.*

class Huffman {
	@Test
	fun createTypeMap() {
		val queue = PriorityQueue(FrequencySorter)
		queue.offer(DynamicHuffmanLeaf(Packet.Types.VUINT,   1000))
		queue.offer(DynamicHuffmanLeaf(Packet.Types.STRING,  950))
		queue.offer(DynamicHuffmanLeaf(Packet.Types.UNION,   900))
		queue.offer(DynamicHuffmanLeaf(Packet.Types.VLONG,   850))
		queue.offer(DynamicHuffmanLeaf(Packet.Types.VINT,    800))
		queue.offer(DynamicHuffmanLeaf(Packet.Types.BOOL,    750))
		queue.offer(DynamicHuffmanLeaf(Packet.Types.ENUM,    700))
		queue.offer(DynamicHuffmanLeaf(Packet.Types.LIST,    650))
		queue.offer(DynamicHuffmanLeaf(Packet.Types.VULONG,  600))
		queue.offer(DynamicHuffmanLeaf(Packet.Types.PACKET,  550))
		queue.offer(DynamicHuffmanLeaf(Packet.Types.BYTE,    500))
		queue.offer(DynamicHuffmanLeaf(Packet.Types.UBYTE,   450))
		queue.offer(DynamicHuffmanLeaf(Packet.Types.VSHORT,  400))
		queue.offer(DynamicHuffmanLeaf(Packet.Types.VUSHORT, 350))
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
		queue.offer(DynamicHuffmanLeaf(0u, 1000))
		queue.offer(DynamicHuffmanLeaf(1u, 950))
		queue.offer(DynamicHuffmanLeaf(2u, 900))
		queue.offer(DynamicHuffmanLeaf(3u, 850))
		queue.offer(DynamicHuffmanLeaf(4u, 800))
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
}