package tech.poder.ptir.util

import jdk.incubator.foreign.MemorySegment
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.nio.file.Path
import kotlin.io.path.fileSize

object SegmentUtil {
    fun mapFile(
        location: Path,
        order: ByteOrder = ByteOrder.nativeOrder(),
        mode: FileChannel.MapMode = FileChannel.MapMode.READ_WRITE
    ): MemorySegmentBuffer {
        return MemorySegmentBuffer(
            MemorySegment.mapFile(location, 0, location.fileSize(), mode),
            order
        )
    }
}