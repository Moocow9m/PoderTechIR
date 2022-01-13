package tech.poder.ir.util.encode.normal

import tech.poder.ir.util.encode.HuffmanTree

@JvmInline
value class HuffmanLeaf(private val value: Any) : HuffmanTree {
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
}
