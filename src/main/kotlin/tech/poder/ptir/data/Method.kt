package tech.poder.ptir.data

data class Method(
    val package_: Package,
    val parent: Object?,
    val name: String,
    val returnType: Type,
    val instructions: List<Instruction>,
    val args: List<Type>
)
