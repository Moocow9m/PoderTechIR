package tech.poder.ir.util.encode

import tech.poder.ir.util.encode.dynamic.DynamicHuffmanNode
import tech.poder.ir.util.encode.dynamic.DynamicHuffmanTree
import java.util.*

interface HuffmanTree {
	companion object {
		fun fromQueue(queue: Queue<DynamicHuffmanTree>): HuffmanTree {
			while (queue.size > 1) {
				val first = queue.poll()
				val second = queue.poll()
				val newHuffNode = DynamicHuffmanNode(first, second, first.freq() + second.freq())

				queue.add(newHuffNode)
			}
			return queue.poll().toStatic()
		}

		fun treeToMap(tree: HuffmanTree): Map<Any, String> {
			val map = mutableMapOf<Any, String>()
			tailCall(listOf(listOf(tree, map, "")), ::decomposeInputs)
			return map
		}

		fun mapToBinary(map: Map<Any, String>): Map<Any, BooleanArray> {
			val binaryMap = mutableMapOf<Any, BooleanArray>()
			map.forEach { (key, value) ->
				binaryMap[key] = value.map { it != '0' }.toBooleanArray()
			}
			return binaryMap
		}

		fun invertBinaryMap(map: Map<*, BooleanArray>): Map<BooleanArray, Any> {
			val map2 = mutableMapOf<BooleanArray, Any>()
			map.forEach {
				map2[it.value] = it.key!!
			}
			return map2
		}

		fun invertHuffmanMap(map: Map<*, String>): Map<String, Any> {
			val map2 = mutableMapOf<String, Any>()
			map.forEach {
				map2[it.value] = it.key!!
			}
			return map2
		}

		private tailrec fun tailCall(starter: List<List<Any>>, invokable: (List<Any>) -> List<List<Any>>) {
			val tmp = mutableListOf<List<Any>>()
			starter.forEach {
				tmp.addAll(invokable.invoke(it))
			}
			if (tmp.isEmpty()) {
				return
			}
			tailCall(tmp, invokable)
		}

		private fun decomposeInputs(input: List<Any>): List<List<Any>> {
			return recursive(input[0] as HuffmanTree, input[1] as MutableMap<Any, String>, input[2] as String)
		}

		private fun recursive(
			nodes: HuffmanTree,
			storage: MutableMap<Any, String>,
			prepend: String = ""
		): List<List<Any>> {
			if (nodes.hasValue()) {
				storage[nodes.value()] = prepend
				return emptyList()
			}
			return listOf(
				listOf(nodes.left(), storage, "${prepend}0"), listOf(nodes.right(), storage, "${prepend}1")
			)
		}
	}

	fun left(): HuffmanTree
	fun right(): HuffmanTree
	fun hasValue(): Boolean
	fun value(): Any
}