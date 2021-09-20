package tech.poder.ir.vm.fs

import tech.poder.ir.util.MemorySegmentBuffer
import tech.poder.ir.util.SegmentUtil
import java.nio.ByteOrder
import java.nio.file.Path

class SimpleFs(val path: Path, private val memory: MemorySegmentBuffer) {
    companion object {
        private val blockSize = 512
        private val keySize = 16 //MD5
        fun load(path: Path) {

        }
    }

    //Nodes are ordered by 2 Byte prefix. This allows for 16 Node Types before leaves.... I think...
    //All nodes are appended to! this allows for 0 locks. On append, node is relocated if space is too small!(may cause issues when multiple threads try to add to the same node)
    //Node Type: List with first item being a pointer to next node/leaf, and rest is a null terminated pointer list
    //Leaf Type: 1 Byte Type and Pointer to Record of data
    //Record Type: List with first item being a pointer to next leaf, and rest is a null terminated key list

    constructor(path: Path, size: ULong, sectors: List<Sector>) : this(
        path,
        SegmentUtil.mapFile(path, ByteOrder.LITTLE_ENDIAN, size = size.toLong())
    ) {
        val btreeSize = blockSize / keySize.toDouble()

        val baseSize =
            1L + MemorySegmentBuffer.varSize(blockSize) + MemorySegmentBuffer.varSize(keySize) + sectors.sumOf {
                MemorySegmentBuffer.varSize(it.size).toLong()
            }
        memory.writeVar(blockSize) //For backwards compatibility, write block and key size
        memory.writeVar(keySize)
        sectors.forEach {
            memory.writeVar(it.size)
        }
        memory.writeVar(0) //Null terminator is 0 written as a varInt(byte size is 1)
        memory.write(0L) //Root node is empty!
        //init fs
    }
}