package tech.poder.ir.std

import tech.poder.ir.instructions.simple.ObjectBuilder

object Basic {
    val listObj by lazy {
        ObjectBuilder("std.list") {
            it.push(0)
            it.push(10)
            it.push(10)
            it.createArray()
            it.setField("\$list")
            it.setField("\$maxSize")
            it.setField("\$size")
        }.build()
    }

    val math by lazy {
        ObjectBuilder("std.math", false) {}
            .createMethod("pow", 2u, true) {
                it.push(0)
                it.getVar("args")
                it.getArrayItem()
                it.setVar("number")
                it.push(1)
                it.setVar("value")
                it.push(1)
                it.getVar("args")
                it.getArrayItem()
                it.setVar("index")
                val loopBegin = it.newLabel()
                val loopEnd = it.newLabel()
                it.placeLabel(loopBegin)
                it.push(0)
                it.getVar("index")
                it.ifGreaterThan(loopEnd)
                it.getVar("value")
                it.getVar("number")
                it.mul()
                it.setVar("value")
                it.getVar("index")
                it.dec()
                it.setVar("index")
                it.jmp(loopBegin)
                it.placeLabel(loopEnd)
                it.getVar("value")
                it.return_()
            }.build()
    }

}