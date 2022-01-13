package tech.poder.ir.util.encode.dynamic

object FrequencySorter : Comparator<DynamicHuffmanTree> {
	override fun compare(o1: DynamicHuffmanTree, o2: DynamicHuffmanTree): Int {
		return o1.freq() - o2.freq()
	}

}
