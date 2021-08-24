package tech.poder.ir.std

import tech.poder.ir.instructions.simple.ObjectBuilder

object Basic {
    val listObj = ObjectBuilder("std.list")
        .createMethod("init") {

        }.build()

}