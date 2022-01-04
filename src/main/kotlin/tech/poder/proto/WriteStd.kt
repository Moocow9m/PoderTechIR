package tech.poder.proto

import java.io.OutputStream

object WriteStd {

    fun writeVarInt(out: OutputStream, int: Int) {
        var value = int
        while (true) {
            if (value and 0x7F == value) {
                out.write(value)
                return
            }
            out.write((value and 0x7F or 0x80))
            value = value ushr 7
        }
    }

    fun writeZigZag(value: Int): Int {
        return (value shl 1) xor (value shr 31)
    }

    fun writeVInt(stream: OutputStream, int: Int) {
        writeVarInt(stream, writeZigZag(int))
    }

    fun writeVUInt(stream: OutputStream, int: UInt) {
        writeVarInt(stream, int.toInt())
    }

    fun writeString(stream: OutputStream, string: String) {
        val bytes = string.encodeToByteArray()
        writeVUInt(stream, bytes.size.toUInt())
        stream.write(bytes)
    }

    fun writePacket(stream: OutputStream, packet: Packet) {
        packet.toBytes(stream)
    }

    fun writeAnyList(stream: OutputStream, list: List<*>) {
        writeVUInt(stream, list.size.toUInt())
        list.forEach {
            writeAny(stream, it!!)
        }
    }

    fun writeList(stream: OutputStream, list: List<*>) {//write shortcut(on read, type is known)
        writeVUInt(stream, list.size.toUInt())
        list.forEach {
            writeAnyNoPrefix(stream, it!!)
        }
    }

    fun writeAnyNoPrefix(stream: OutputStream, any: Any) {//write shortcut(on read, type is known)
        when (any) {
            is UInt -> writeVUInt(stream, any)
            is Packet -> writePacket(stream, any)
            is String -> writeString(stream, any)
            is List<*> -> writeList(stream, any)
            else -> error("Unsupported type: ${any::class.simpleName}")
        }
    }

    fun writeAny(stream: OutputStream, any: Any) {
        when (any) {
            is Enum<*> -> {
                writeVUInt(stream, Packet.Types.ENUM.ordinal.toUInt())
                writeVUInt(stream, any.ordinal.toUInt())
            }
            is UInt -> {
                writeVUInt(stream, Packet.Types.VUINT.ordinal.toUInt())
                writeVUInt(stream, any)
            }
            is Int -> {
                writeVUInt(stream, Packet.Types.VINT.ordinal.toUInt())
                writeVInt(stream, any)
            }
            is Packet -> {
                writeVUInt(stream, Packet.Types.PACKET.ordinal.toUInt())
                writeString(stream, any::class.java.name)
                writePacket(stream, any)
            }
            is String -> {
                writeVUInt(stream, Packet.Types.STRING.ordinal.toUInt())
                writeString(stream, any)
            }
            is List<*> -> {
                writeVUInt(stream, Packet.Types.LIST.ordinal.toUInt())
                writeAnyList(stream, any)
            }
            else -> error("Unsupported type: ${any::class.simpleName}")
        }
    }
}