package tech.poder.ir.api

interface CodeHolder {
    val lang: Language

    fun translate(to: Language)
}