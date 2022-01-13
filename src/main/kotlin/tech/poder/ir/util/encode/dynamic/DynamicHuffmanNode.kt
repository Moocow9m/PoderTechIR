package tech.poder.ir.util.encode.dynamic

import tech.poder.ir.util.encode.HuffmanTree
import tech.poder.ir.util.encode.normal.HuffmanNode

data class DynamicHuffmanNode(val left: DynamicHuffmanTree, val right: DynamicHuffmanTree, val freq: Int) :
	DynamicHuffmanTree {
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

	override fun freq(): Int {
		return freq
	}

	override fun toStatic(): HuffmanTree {
		return HuffmanNode(left.toStatic(), right.toStatic())
	}
}
