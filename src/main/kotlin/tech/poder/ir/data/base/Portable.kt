package tech.poder.ir.data.base

import tech.poder.ir.metadata.Visibility

class Portable {
    internal val roots: MutableSet<Package> = mutableSetOf()
    internal var entrypoint: String? = null
    fun newPackage(namespace: String, visibility: Visibility = Visibility.INTERNAL): Package {
        val pkg = Package(namespace, visibility)
        roots.add(pkg)
        return pkg
    }

    fun entryPoint(namespace: String) {
        entrypoint = namespace
    }
}
