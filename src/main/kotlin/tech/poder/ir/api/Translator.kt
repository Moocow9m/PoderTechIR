package tech.poder.ir.api

interface Translator {

    fun translate(input: CodeHolder): CodeHolder
}