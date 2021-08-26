package tech.poder.ptir.data

data class Object(
    val parent: Package,
    val name: String,
    val methods: MutableList<Method>,
    val fields: MutableList<Field>
)
