package tech.poder.ptir

import tech.poder.ir.data.storage.Instruction
import tech.poder.ir.data.storage.memory.MemoryAllocator
import tech.poder.ptir.data.base.Container
import tech.poder.ptir.data.storage.NamedType
import java.util.concurrent.atomic.AtomicBoolean

object VM {
    internal val methods = mutableMapOf<String, List<Instruction>>()
    internal val structs = mutableMapOf<String, List<NamedType>>()
    internal val allocator = MemoryAllocator()
    private var running = AtomicBoolean(true)

    fun loadPortable(portable: Container) {
        portable.roots.forEach { pkg ->
            pkg.floating.forEach {
                val list = mutableListOf<Instruction>()
                it.toBulk(list)
                methods[it.fullName] = list
            }

            pkg.objects.forEach { obj ->
                structs[obj.fullName] = obj.fields
                obj.methods.forEach {
                    val list = ArrayList<Instruction>()
                    it.toBulk(list)
                    methods[it.fullName] = list
                }
            }
        }
    }

    fun execute(entryPoint: String) {

    }
}