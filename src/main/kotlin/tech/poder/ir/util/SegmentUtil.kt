package tech.poder.ir.util

import jdk.incubator.foreign.MemorySegment
import jdk.incubator.foreign.ResourceScope
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.fileSize

object SegmentUtil {
	fun mapFile(
		location: Path,
		order: ByteOrder = ByteOrder.nativeOrder(),
		mode: FileChannel.MapMode = FileChannel.MapMode.READ_WRITE,
		size: Long,
	): MemorySegmentBuffer {
		Files.deleteIfExists(location)
		val bc = Files.newByteChannel(
			location,
			StandardOpenOption.CREATE,
			StandardOpenOption.TRUNCATE_EXISTING,
			StandardOpenOption.WRITE
		)
		bc.position(size)
		val blankBuffer = ByteBuffer.allocateDirect(0)
		bc.write(blankBuffer)
		bc.close()
		return mapFile(location, order, mode)
	}

	fun mapFile(
		location: Path,
		order: ByteOrder = ByteOrder.nativeOrder(),
		mode: FileChannel.MapMode = FileChannel.MapMode.READ_WRITE
	): MemorySegmentBuffer {
		val scope = ResourceScope.newConfinedScope()
		return MemorySegmentBuffer(
			MemorySegment.mapFile(location, 0, location.fileSize(), mode, scope),
			scope,
			order
		)
	}

	fun allocate(size: Long, order: ByteOrder = ByteOrder.nativeOrder()): MemorySegmentBuffer {
		val scope = ResourceScope.newConfinedScope()
		return MemorySegmentBuffer(
			MemorySegment.allocateNative(size, scope),
			scope,
			order
		)
	}
}