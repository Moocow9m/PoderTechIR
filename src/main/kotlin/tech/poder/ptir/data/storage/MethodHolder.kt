package tech.poder.ptir.data.storage

data class MethodHolder(val fullName: String, val returnType: Type?, val args: Set<NamedType>) {
    init {
        args.forEach {
            val t = it.type
            if (t is Type.Constant) {
                t.constant = false
            }
        }
        if (returnType is Type.Constant) {
            returnType.constant = false
        }
    }
}
