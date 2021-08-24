package tech.poder.ir.std

import tech.poder.ir.instructions.common.Method

object UserInput {
    val readBoolean by lazy {
        Method.create("readBoolean", returns = true) {

        }
    }
}