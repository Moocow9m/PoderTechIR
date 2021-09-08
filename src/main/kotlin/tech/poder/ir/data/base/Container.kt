package tech.poder.ir.data.base

import tech.poder.ir.metadata.Visibility

class Container {
    internal val roots: MutableSet<Package> = mutableSetOf()
    internal var entrypoint: String? = null
    internal val requiredLibs: MutableSet<String> = mutableSetOf()

    fun newPackage(namespace: String, visibility: Visibility = Visibility.PRIVATE): Package {
        val pkg = Package(namespace, visibility)
        roots.add(pkg)
        return pkg
    }

    fun addLib(name: String) {
        requiredLibs.add(name)
    }

    fun entryPoint(method: Method) {
        check(method.parent == null) {
            "Entrypoint cannot have Object parent!"
        }
        check(method.args.size <= 1) {
            "Entrypoint cannot have more than 1 arg! Has ${method.args.size}"
        }
        /*check(method.args.isEmpty() || method.args.first().type is Type.Array) {
            "Entrypoint arg must be array if it exist! Was ${method.args.first().type}"
        }*/
        entryPoint(method.fullName)
    }

    fun entryPoint(namespace: String) {
        entrypoint = namespace
    }

    fun entryPoint(): String? {
        return entrypoint
    }
}
