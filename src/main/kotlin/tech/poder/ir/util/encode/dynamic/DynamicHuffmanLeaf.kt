package tech.poder.ir.util.encode.dynamic

import tech.poder.ir.util.encode.HuffmanTree
import tech.poder.ir.util.encode.normal.HuffmanLeaf

data class DynamicHuffmanLeaf(val value: Any, val freq: Int) : DynamicHuffmanTree {
	override fun left(): HuffmanTree {
		throw IllegalAccessError()
	}

	override fun right(): HuffmanTree {
		throw IllegalAccessError()
	}

	override fun hasValue(): Boolean {
		return true
	}

	override fun value(): Any {
		return value
	}

	override fun freq(): Int {
		return freq
	}

	override fun toStatic(): HuffmanTree {
		return HuffmanLeaf(value)
	}
}
