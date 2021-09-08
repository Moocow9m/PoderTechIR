package tech.poder.ir.data.base

import tech.poder.ir.metadata.Visibility

class Container(val name: String) {
    internal val roots: MutableSet<Package> = mutableSetOf()
    internal var entrypoint: String? = null
    private var validated = false
    private var resolved = false

    fun isValidated(): Boolean {
        return validated
    }

    fun isLinked(): Boolean {
        return resolved
    }

    fun validate() {
        if (validated) {
            return
        }
        //this will assign every method and object to a number for calling, ordered by fullName. may be overridden later to user assigned id
        TODO()
    }

    fun link(dependencies: Set<Container>) {
        if (resolved) {
            return
        }
        if (!validated) {
            validate()
        }
        //this will validate the stack, resolve methods and objects to numeric ids(with container name separating items)
        TODO() //set resolved=true after making sure everything resolves
    }

    fun newPackage(namespace: String, visibility: Visibility = Visibility.PRIVATE): Package {
        val pkg = Package(namespace, visibility)
        roots.add(pkg)
        return pkg
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
