package tech.poder.ir.data.storage.segment

import tech.poder.ir.data.base.Method
import tech.poder.ir.data.storage.Instruction
import tech.poder.ir.data.storage.Label
import tech.poder.ir.data.storage.Type
import java.util.*
import kotlin.reflect.KClass

interface Segment {

    fun eval(
        method: Method,
        stack: Stack<Type>,
        currentVars: MutableList<KClass<out Type>?>,
        currentIndex: Int,
        labels: MutableMap<Int, Label>
    ): Int

    fun size(): Int

    fun toBulk(storage: MutableList<Instruction>)

}