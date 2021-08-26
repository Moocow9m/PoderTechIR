package tech.poder.ptir.data

data class Package(val namespace: String, val objects: MutableList<Object>, val floating: MutableList<Method>)
