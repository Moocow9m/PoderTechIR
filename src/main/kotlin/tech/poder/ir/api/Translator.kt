package tech.poder.ir.api

import kotlin.reflect.KClass

interface Translator<I : CodeHolder, O : CodeHolder> {

    val iClass: KClass<out CodeHolder>
    val oClass: KClass<out CodeHolder>

    fun translate(input: I): O

}