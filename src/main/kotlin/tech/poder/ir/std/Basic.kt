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

    val userInput by lazy {
        ObjectBuilder("std.user.input") {

        }.build()
    }

}