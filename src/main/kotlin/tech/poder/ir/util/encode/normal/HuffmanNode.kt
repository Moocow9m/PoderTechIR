package tech.poder.ir.util.encode.normal

import tech.poder.ir.util.encode.HuffmanTree

data class HuffmanNode(val left: HuffmanTree, val right: HuffmanTree) : HuffmanTree {
	override fun left(): HuffmanTree {
		return left
	}

	override fun right(): HuffmanTree {
		return right
	}

	override fun hasValue(): Boolean {
		return false
	}

	override fun value(): Any {
		throw IllegalAccessError()
	}
}
