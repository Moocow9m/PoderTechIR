package tech.poder.ir.util.encode.dynamic

import tech.poder.ir.util.encode.HuffmanTree

interface DynamicHuffmanTree : HuffmanTree {
	fun freq(): Int

	fun toStatic(): HuffmanTree
}