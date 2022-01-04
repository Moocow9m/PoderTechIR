package tech.poder.proto

interface Packet {
    enum class Types {
        ENUM, PACKET, LIST, STRING, UNION, BOOL, VUINT, VINT, UNKNOWN
    }

    fun length(): Int {
        val os = BlankOutputStream()
        toBytes(os)
        return os.amountWritten
    }

    fun toBytes(stream: java.io.OutputStream)
}