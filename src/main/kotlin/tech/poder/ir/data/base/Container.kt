package tech.poder.ir.data.base

import tech.poder.ir.metadata.Visibility

class Container(val name: String) {
    internal val roots: MutableSet<Package> = mutableSetOf()
    internal var entrypoint: String? = null
    private var resolved = false
    private var mappingCache: Map<String, UInt>? = null
    private var internalMappingCache: Map<String, UInt>? = null

    internal fun getAllMapping(): Map<String, UInt> { //private items will have their name-dropped from binary on link!
        if (internalMappingCache != null) {
            return internalMappingCache!!
        }
        val defMap = getMapping()
        var nextMethodId = 0u
        var nextObjectId = 0u
        var nextFieldId = 0u
        val map = mutableMapOf<String, UInt>()
        defMap.forEach { (name, value) ->
            if (name.contains(Method.methodSeparator)) {
                nextMethodId++
            } else if (name.contains(Object.fieldSeparator)) {
                nextFieldId++
            } else {
                nextObjectId++
            }
            map[name] = value
        }
        roots.filter { it.visibility == Visibility.PRIVATE }.forEach { pkg ->
            pkg.floating.filter { it.visibility == Visibility.PRIVATE }.forEach {
                map[it.fullName] = nextMethodId++
            }
            pkg.objects.filter { it.visibility == Visibility.PRIVATE }.forEach { obj ->
                map[obj.fullName] = nextObjectId++
                obj.methods.filter { it.visibility == Visibility.PRIVATE }.forEach {
                    map[it.fullName] = nextMethodId++
                }
                obj.fields.forEach {
                    map["${obj.fullName}${Object.fieldSeparator}${it.name}"] = nextFieldId++
                }
            }
        }
        internalMappingCache = map
        return map
    }

    fun getMapping(): Map<String, UInt> {
        if (mappingCache != null) {
            return mappingCache!!
        }
        var nextMethodId = 0u
        var nextObjectId = 0u
        var nextFieldId = 0u
        val map = mutableMapOf<String, UInt>()
        roots.filter { it.visibility == Visibility.PUBLIC }.forEach { pkg ->
            pkg.floating.filter { it.visibility == Visibility.PUBLIC }.forEach {
                map[it.fullName] = nextMethodId++
            }
            pkg.objects.filter { it.visibility == Visibility.PUBLIC }.forEach { obj ->
                map[obj.fullName] = nextObjectId++
                obj.methods.filter { it.visibility == Visibility.PUBLIC }.forEach {
                    map[it.fullName] = nextMethodId++
                }
                obj.fields.forEach {
                    map["${obj.fullName}${Object.fieldSeparator}${it.name}"] = nextFieldId++
                }
            }
        }
        mappingCache = map
        return map
    }

    fun isLinked(): Boolean {
        return resolved
    }

    fun link(dependencies: Set<Container>) {
        //this will validate the stack, resolve methods and objects to ids(with container name separating items)
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
